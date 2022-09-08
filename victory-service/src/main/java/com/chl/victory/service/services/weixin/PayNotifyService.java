package com.chl.victory.service.services.weixin;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.member.ShopMemberDO;
import com.chl.victory.dao.model.merchant.PayConfig;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.dao.query.member.ShopMemberQuery;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.service.utils.WeixinMiniProgramUtil;
import com.chl.victory.service.weixinsdk.WXPay;
import com.chl.victory.service.weixinsdk.WXPayConfig;
import com.chl.victory.service.weixinsdk.WXPayUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.serviceapi.weixin.model.pay.PayBaseParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.chl.victory.service.services.ServiceManager.merchantService;
import static com.chl.victory.service.services.ServiceManager.orderLocalService;
import static com.chl.victory.service.services.ServiceManager.orderService;

/**
 * @author ChenHailong
 * @date 2020/8/25 11:02
 **/
@Service
public class PayNotifyService {
    public ServiceResult payNotify(String notify){
        CheckResult result = checkPayNotify(notify);
        if (CollectionUtils.isEmpty(result.map)) {
            if (StringUtils.isNotBlank(result.getResult())) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,result.getResult());
            }
            return ServiceResult.success();
        }

        Map<String, String> map = result.getMap();
        Long shopId = result.getShopId();

        // 一定周期内不可重复发送
        String out_trade_no = map.get("out_trade_no");
        if (StringUtils.isBlank(out_trade_no)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"参数错误");
        }
        Long orderId = NumberUtils.toLong(out_trade_no, 0L);
        if (0 == orderId) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"参数错误");
        }
        String key = CacheKeyPrefix.WEIXIN_NOTIFY_PAY_EXIST_OF_OUT_TRADE_NO;
        boolean sIsMember = ServiceManager.cacheService.sIsMember(key, out_trade_no);
        if (sIsMember) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"一段时期内重复发送");
        }
        ServiceManager.cacheService.sAdd(key, out_trade_no);
        ServiceManager.cacheService.expire(key, 10);

        BeanUtilsHashMapper<PayNotify> payNotifyBeanUtilsHashMapper = new BeanUtilsHashMapper<>(PayNotify.class);
        PayNotify payNotify = payNotifyBeanUtilsHashMapper.fromHash(map);

        try {
            ValidationUtil.validate(payNotify);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"参数错误");
        }

        // openId 对应 user?
        String notifyOpenId = payNotify.getOpenid();
        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setOpenId(notifyOpenId);
        shopMemberQuery.setShopId(shopId);
        List<ShopMemberDO> selectMemResult = null;
        try {
            selectMemResult = ServiceManager.memberService.selectMem(shopMemberQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"查询用户失败");
        }
        if (CollectionUtils.isEmpty(selectMemResult) || selectMemResult.get(0) == null) {
            // "用户不存在"
            return ServiceResult.success();
        }
        ShopMemberDO shopMemberDO = selectMemResult.get(0);
        Long buyerId = shopMemberDO.getId();

        // 店铺 用户 订单ID 订单未付款?
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setBuyerId(buyerId);
        orderServiceQuery.setStatus(OrderStatusEnum.newed.getCode().byteValue());

        String orderDetailCache = orderLocalService.getOrderDetailCache(shopId, buyerId, orderId);
        if (StringUtils.isBlank(orderDetailCache)) {
            ServiceResult<Integer> countOrder = orderService.countOrder(orderServiceQuery);
            if (!countOrder.getSuccess()) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"查询订单失败");
            }
            if (countOrder.getData() < 1) {
                return ServiceResult.success();
            }
        }

        OrderDTO orderDTO;
        try {
            orderDTO = orderService.selectMainOrder(orderServiceQuery);
        } catch (BusServiceException e) {
            if (e.getMessage().contains("订单没找到")) {
                // "订单没找到"
                return ServiceResult.success();
            }
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"查询订单失败");
        }
        if (orderDTO == null) {
            return ServiceResult.success();
        }

        Integer notifyTotalFee = payNotify.getTotal_fee();
        Integer realFee = orderDTO.getRealFee().movePointRight(2).intValue();
        if (!realFee.equals(notifyTotalFee)) {
            // "订单金额不一致"
            return ServiceResult.success();
        }

        String context = "{desc:'微信支付payNotify', endTime:'" + payNotify.getTime_end() + "', transactionId:'" + payNotify
                .getTransaction_id() + "', content:'" + payNotify + "'}";
        ServiceResult serviceResult = orderService.confirmPayedWithNxLock(orderId, shopId, 0L, context);
        if (!serviceResult.getFailType().equals(ServiceFailTypeEnum.BUS_NOT_GET_LOCK)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"处理确认付款失败");
        }
        return ServiceResult.success();
    }

    public ServiceResult refundNotify(String notify){
        CheckResult result = checkRefundNotify(notify);
        if (CollectionUtils.isEmpty(result.map)) {
            if (StringUtils.isNotBlank(result.getResult())) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,result.getResult());
            }
            return ServiceResult.success();
        }

        Map<String, String> map = result.getMap();
        Long shopId = result.getShopId();

        /*  （1）对加密串A做base64解码，得到加密串B
            （2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )
            （3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）*/
        String req_info = map.get("req_info");
        if (StringUtils.isBlank(req_info)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"数据错误");
        }
        String keyMD5;
        try {
            keyMD5 = WXPayUtil.MD5(result.getPayConfig().getApiKey()).toLowerCase();
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"md5异常");
        }

        // 解密参考 https://blog.csdn.net/qq_25958497/article/details/87937020
        String decodedReqInfo;
        try {
            decodedReqInfo = WeixinMiniProgramUtil.getRefundNotifyContent(keyMD5, req_info);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"解密失败");
        }
        if (StringUtils.isBlank(decodedReqInfo)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"数据错误");
        }

        Map<String, String> refundNotifyMap = null;
        try {
            refundNotifyMap = WXPayUtil.xmlToMap(decodedReqInfo);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"xmlToMap异常");
        }
        if (CollectionUtils.isEmpty(refundNotifyMap)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"数据错误");
        }

        // 一定周期内不可重复发送
        String out_refund_no = refundNotifyMap.get("out_refund_no");
        Long refundId = NumberUtils.toLong(out_refund_no, 0L);
        if (0 == refundId) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"数据错误");
        }
        String out_trade_no = refundNotifyMap.get("out_trade_no");
        if (StringUtils.isBlank(out_trade_no)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"数据错误");
        }
        Long orderId = NumberUtils.toLong(out_trade_no, 0L);
        if (0 == orderId) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"数据错误");
        }

        String key = CacheKeyPrefix.WEIXIN_NOTIFY_REFUND_EXIST_OF_REFUND_ID;
        boolean sIsMember = ServiceManager.cacheService.sIsMember(key, out_refund_no);
        if (sIsMember) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"一段时期内重复发送");
        }
        ServiceManager.cacheService.sAdd(key, out_refund_no);
        ServiceManager.cacheService.expire(key, 10);

        BeanUtilsHashMapper<RefundNotify> payNotifyBeanUtilsHashMapper = new BeanUtilsHashMapper<>(RefundNotify.class);
        RefundNotify payNotify = payNotifyBeanUtilsHashMapper.fromHash(refundNotifyMap);

        try {
            ValidationUtil.validate(payNotify);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"数据错误");
        }


        // 店铺 订单ID 退款单ID 订单退款处理中?
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setRefundId(refundId);
        orderServiceQuery.setStatus(OrderStatusEnum.refunding.getCode().byteValue());
        ServiceResult<Integer> countOrder = orderService.countOrder(orderServiceQuery);
        if (!countOrder.getSuccess()) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"查询订单失败");
        }
        if (countOrder.getData() < 1) {
            return ServiceResult.success();
        }

        orderServiceQuery.setNeedRefund(true);
        OrderDTO orderDTO;
        try {
            orderDTO = orderService.selectMainOrder(orderServiceQuery);
        } catch (BusServiceException e) {
            if (e.getMessage().contains("订单没找到")) {
                return ServiceResult.success();
            }
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "查询订单失败");
        }
        if (orderDTO == null) {
            return ServiceResult.success();
        }
        Long buyerId = orderDTO.getBuyerId();

        Integer notifyTotalFee = payNotify.getTotal_fee();
        Integer realFee = orderDTO.getRealFee().movePointRight(2).intValue();
        if (!realFee.equals(notifyTotalFee)) {
            // "订单金额不一致"
            return ServiceResult.success();
        }

        Integer notifyRefundFee = payNotify.getRefund_fee();
        Integer refundFee = orderDTO.getRefund().getApplyFee().movePointRight(2).intValue();
        if (!refundFee.equals(notifyRefundFee)) {
            // "申请退款金额不一致"
            return ServiceResult.success();
        }

        // 退款结果处理
        String context = "{desc:'微信支付退款Notify', content:'" + payNotify + "'}";
        ServiceResult serviceResult;
        if ("SUCCESS".equals(payNotify.getRefund_status())) {
            serviceResult = orderService.confirmRefundedWithNxLock(orderId, refundId, shopId, buyerId, context);
        }
        else if ("REFUNDCLOSE".equals(payNotify.getRefund_status())) {
            try {
                serviceResult = orderService.confirmRefundClosedWithNxLock(orderId, refundId, shopId, buyerId, context, ShopConstants.DEFAULT_OPERATOER);
            } catch (BusServiceException e) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "处理关闭退款异常1");
            }
        }
        else if ("CHANGE".equals(payNotify.getRefund_status())) {
            try {
                serviceResult = orderService.confirmRefundChangdWithNxLock(orderId, refundId, shopId, buyerId, context);
            } catch (BusServiceException e) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "处理关闭退款异常2");
            }
        }else {
            // "不支持该退款状态"
            return ServiceResult.success();
        }

        if (!serviceResult.getFailType().equals(ServiceFailTypeEnum.BUS_NOT_GET_LOCK)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "处理退款回告失败");
        }

        return ServiceResult.success();
    }

    private CheckResult checkPayNotify(String notify) {
        Map<String, String> map;
        try {
            map = WXPayUtil.xmlToMap(notify);
        } catch (Exception e) {
            return new CheckResult("参数错误");
        }

        String notifyAppId = map.get("appid");
        String notifyMchId = map.get("mch_id");
        String notifySubAppId = map.get("sub_appid");
        String notifySubMchId = map.get("sub_mch_id");

        String appId = StringUtils.isBlank(notifySubAppId) ? notifyAppId : notifySubAppId;
        String mchId = StringUtils.isBlank(notifySubMchId) ? notifyMchId : notifySubMchId;

        // appId 对应shop?
        Long shopId = null;
        try {
            shopId = merchantService.getShopId(appId);
        } catch (BusServiceException e) {
            return new CheckResult("app匹配shop异常");
        }
        if (null == shopId) {
            // "参数错误"
            return new CheckResult();
        }

        ShopAppDO weixinConfig;
        try {
            weixinConfig = merchantService.getShopAppCache(shopId, appId);
        } catch (Exception e) {
            return new CheckResult("查询店铺微信信息失败");
        }

        PayConfig payConfig;
        try {
            payConfig = merchantService.getPayConfig(weixinConfig);
        } catch (BusServiceException e) {
            // "店铺app缺少微信支付信息"
            return new CheckResult();
        }

        if (!weixinConfig.getAppId().equals(appId)) {
            // "appId数据错误"
            return new CheckResult();
        }
        if (!payConfig.getMchId().equals(mchId)) {
            // "mchId数据错误"
            return new CheckResult();
        }

        PayBaseParam payBaseParam = WeixinMiniProgramService.getPayBaseParam(payConfig, weixinConfig);
        String payKey = WeixinMiniProgramService.getPayKey(payConfig);

        // 验签
        WXPay wxPay;
        try {
            WXPayConfig wxPayConfig = new WXPayConfig();
            wxPayConfig.setAppID(payBaseParam.getAppid());
            wxPayConfig.setMchID(payBaseParam.getMch_id());
            wxPayConfig.setKey(payKey);
            wxPay = new WXPay(wxPayConfig);
        } catch (Exception e) {
            return new CheckResult("系统异常");
        }

        boolean signatureValid;
        try {
            signatureValid =  wxPay.isPayResultNotifySignatureValid(map);
        } catch (Exception e) {
            return new CheckResult("报文验签异常");
        }
        if (!signatureValid) {
            return new CheckResult("报文验签不通过");
        }

        CheckResult checkResult = new CheckResult();
        checkResult.setMap(map);
        checkResult.setShopId(shopId);
        checkResult.setAppId(appId);
        checkResult.setWeixinConfig(weixinConfig);
        checkResult.setPayConfig(payConfig);
        return checkResult;
    }
    private CheckResult checkRefundNotify(String notify) {
        Map<String, String> map;
        try {
            map = WXPayUtil.xmlToMap(notify);
        } catch (Exception e) {
            return new CheckResult("参数错误");
        }

        String appId = map.get("appid");
        String mchId = map.get("mch_id");

        boolean isComponent = false;
        PayConfig payConfig = null;
        PayBaseParam payBaseParam;
        String payKey;
        payBaseParam = WeixinMiniProgramService.getPayBaseParam();
        payKey = WeixinMiniProgramService.getPayKey();
        if (payBaseParam.getMch_id().equals(mchId) && payBaseParam.getAppid().equals(appId)) {
            isComponent = true;
            payConfig = new PayConfig();
            payConfig.setApiKey(payKey);
            payConfig.setMchId(payBaseParam.getMch_id());
        }

        // appId 对应shop?
        Long shopId = null;
        ShopAppDO weixinConfig = null;
        if (!isComponent) {
            try {
                shopId = merchantService.getShopId(appId);
            } catch (BusServiceException e) {
                return new CheckResult("app匹配shop异常");
            }
            if (null == shopId) {
                // "参数错误"
                return new CheckResult();
            }

            try {
                weixinConfig = merchantService.getShopAppCache(shopId, appId);
            } catch (Exception e) {
                return new CheckResult("查询店铺微信信息失败");
            }

            try {
                payConfig = merchantService.getPayConfig(weixinConfig);
            } catch (BusServiceException e) {
                // "店铺app缺少微信支付信息"
                return new CheckResult();
            }

            if (!weixinConfig.getAppId().equals(appId)) {
                // "appId数据错误"
                return new CheckResult();
            }
            if (!payConfig.getMchId().equals(mchId)) {
                // "mchId数据错误"
                return new CheckResult();
            }

            payBaseParam = WeixinMiniProgramService.getPayBaseParam(payConfig, weixinConfig);
            payKey = WeixinMiniProgramService.getPayKey(payConfig);
        }

        CheckResult checkResult = new CheckResult();
        checkResult.setMap(map);
        checkResult.setShopId(shopId);
        checkResult.setAppId(appId);
        checkResult.setWeixinConfig(weixinConfig);
        checkResult.setPayConfig(payConfig);
        return checkResult;
    }

    @Data
    public static class PayNotify {

        /**
         * 返回状态码 SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
         */
        @NotEmpty String return_code;

        String return_msg;

        /*以下字段在return_code为SUCCESS的时候有返回 */

        /**
         * 调用接口提交的小程序ID
         */
        @NotEmpty String appid;

        /**
         * 调用接口提交的商户号
         */
        @NotEmpty String mch_id;

        String sub_appid;

        String sub_mch_id;

        String device_info;

        /**
         * 随机字符串 微信返回的随机字符串
         */
        @NotEmpty String nonce_str;

        /**
         * 微信返回的签名值
         */
        @NotEmpty String sign;

        String sign_type;

        /**
         * 业务结果 SUCCESS/FAIL
         */
        @NotEmpty String result_code;

        /**
         * 错误代码
         */
        String err_code;

        /**
         * 错误代码描述
         */
        String err_code_des;


        /* 以下字段在return_code 和result_code都为SUCCESS的时候有返回 */

        /**
         * 用户标识
         */
        @NotEmpty String openid;

        /**
         * 交易类型
         */
        String trade_type;

        /**
         * 订单总金额，单位为分
         */
        @NotNull Integer total_fee;

        /**
         * 微信支付订单号
         */
        @NotEmpty String transaction_id;

        /**
         * 商户订单号
         */
        @NotEmpty String out_trade_no;

        /**
         * 支付完成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010
         */
        @NotEmpty String time_end;

    }

    @Data
    public static class RefundNotify {

        /**
         * 微信订单号
         */
        @NotEmpty String transaction_id;

        /**
         * 商户订单号
         */
        @NotEmpty String out_trade_no;

        /**
         * 微信退款单号
         */
        @NotEmpty String refund_id;

        /**
         * 商户退款单号
         */
        @NotEmpty String out_refund_no;

        /**
         * 订单总金额，单位为分
         */
        @NotNull Integer total_fee;

        /**
         * 当该订单有使用非充值券时，返回此字段。应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
         */
        Integer settlement_total_fee;

        /**
         * 申请退款金额
         */
        @NotNull Integer refund_fee;

        /**
         * 退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额
         */
        @NotNull Integer settlement_refund_fee;

        /**
         * SUCCESS-退款成功
         * CHANGE-退款异常
         * REFUNDCLOSE—退款关闭
         */
        @NotNull String refund_status;

        /**
         * 资金退款至用户帐号的时间，格式2017-12-15 09:46:01
         */
        String success_time;

        /**
         * 退款入账账户
         */
        @NotNull String refund_recv_accout;

        /**
         * 退款资金来源
         */
        @NotNull String refund_account;

        /**
         * 退款发起来源
         */
        @NotNull String refund_request_source;
    }

    @Data
    class CheckResult {

        Map<String, String> map;

        Long shopId;

        String appId;

        String result;

        ShopAppDO weixinConfig;

        PayConfig payConfig;

        public CheckResult(String result) {
            this.result = result;
        }

        public CheckResult() {
        }
    }
}
