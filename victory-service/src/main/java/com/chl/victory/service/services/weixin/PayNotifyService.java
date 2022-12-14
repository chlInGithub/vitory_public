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

        // ?????????????????????????????????
        String out_trade_no = map.get("out_trade_no");
        if (StringUtils.isBlank(out_trade_no)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }
        Long orderId = NumberUtils.toLong(out_trade_no, 0L);
        if (0 == orderId) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }
        String key = CacheKeyPrefix.WEIXIN_NOTIFY_PAY_EXIST_OF_OUT_TRADE_NO;
        boolean sIsMember = ServiceManager.cacheService.sIsMember(key, out_trade_no);
        if (sIsMember) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"???????????????????????????");
        }
        ServiceManager.cacheService.sAdd(key, out_trade_no);
        ServiceManager.cacheService.expire(key, 10);

        BeanUtilsHashMapper<PayNotify> payNotifyBeanUtilsHashMapper = new BeanUtilsHashMapper<>(PayNotify.class);
        PayNotify payNotify = payNotifyBeanUtilsHashMapper.fromHash(map);

        try {
            ValidationUtil.validate(payNotify);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }

        // openId ?????? user?
        String notifyOpenId = payNotify.getOpenid();
        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setOpenId(notifyOpenId);
        shopMemberQuery.setShopId(shopId);
        List<ShopMemberDO> selectMemResult = null;
        try {
            selectMemResult = ServiceManager.memberService.selectMem(shopMemberQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"??????????????????");
        }
        if (CollectionUtils.isEmpty(selectMemResult) || selectMemResult.get(0) == null) {
            // "???????????????"
            return ServiceResult.success();
        }
        ShopMemberDO shopMemberDO = selectMemResult.get(0);
        Long buyerId = shopMemberDO.getId();

        // ?????? ?????? ??????ID ????????????????
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setBuyerId(buyerId);
        orderServiceQuery.setStatus(OrderStatusEnum.newed.getCode().byteValue());

        String orderDetailCache = orderLocalService.getOrderDetailCache(shopId, buyerId, orderId);
        if (StringUtils.isBlank(orderDetailCache)) {
            ServiceResult<Integer> countOrder = orderService.countOrder(orderServiceQuery);
            if (!countOrder.getSuccess()) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"??????????????????");
            }
            if (countOrder.getData() < 1) {
                return ServiceResult.success();
            }
        }

        OrderDTO orderDTO;
        try {
            orderDTO = orderService.selectMainOrder(orderServiceQuery);
        } catch (BusServiceException e) {
            if (e.getMessage().contains("???????????????")) {
                // "???????????????"
                return ServiceResult.success();
            }
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"??????????????????");
        }
        if (orderDTO == null) {
            return ServiceResult.success();
        }

        Integer notifyTotalFee = payNotify.getTotal_fee();
        Integer realFee = orderDTO.getRealFee().movePointRight(2).intValue();
        if (!realFee.equals(notifyTotalFee)) {
            // "?????????????????????"
            return ServiceResult.success();
        }

        String context = "{desc:'????????????payNotify', endTime:'" + payNotify.getTime_end() + "', transactionId:'" + payNotify
                .getTransaction_id() + "', content:'" + payNotify + "'}";
        ServiceResult serviceResult = orderService.confirmPayedWithNxLock(orderId, shopId, 0L, context);
        if (!serviceResult.getFailType().equals(ServiceFailTypeEnum.BUS_NOT_GET_LOCK)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????????????????");
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

        /*  ???1???????????????A???base64????????????????????????B
            ???2????????????key???md5?????????32?????????key* ( key?????????????????????????????????(pay.weixin.qq.com)-->????????????-->API??????-->???????????? )
            ???3??????key*????????????B???AES-256-ECB?????????PKCS7Padding???*/
        String req_info = map.get("req_info");
        if (StringUtils.isBlank(req_info)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }
        String keyMD5;
        try {
            keyMD5 = WXPayUtil.MD5(result.getPayConfig().getApiKey()).toLowerCase();
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"md5??????");
        }

        // ???????????? https://blog.csdn.net/qq_25958497/article/details/87937020
        String decodedReqInfo;
        try {
            decodedReqInfo = WeixinMiniProgramUtil.getRefundNotifyContent(keyMD5, req_info);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }
        if (StringUtils.isBlank(decodedReqInfo)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }

        Map<String, String> refundNotifyMap = null;
        try {
            refundNotifyMap = WXPayUtil.xmlToMap(decodedReqInfo);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"xmlToMap??????");
        }
        if (CollectionUtils.isEmpty(refundNotifyMap)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }

        // ?????????????????????????????????
        String out_refund_no = refundNotifyMap.get("out_refund_no");
        Long refundId = NumberUtils.toLong(out_refund_no, 0L);
        if (0 == refundId) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }
        String out_trade_no = refundNotifyMap.get("out_trade_no");
        if (StringUtils.isBlank(out_trade_no)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }
        Long orderId = NumberUtils.toLong(out_trade_no, 0L);
        if (0 == orderId) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }

        String key = CacheKeyPrefix.WEIXIN_NOTIFY_REFUND_EXIST_OF_REFUND_ID;
        boolean sIsMember = ServiceManager.cacheService.sIsMember(key, out_refund_no);
        if (sIsMember) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"???????????????????????????");
        }
        ServiceManager.cacheService.sAdd(key, out_refund_no);
        ServiceManager.cacheService.expire(key, 10);

        BeanUtilsHashMapper<RefundNotify> payNotifyBeanUtilsHashMapper = new BeanUtilsHashMapper<>(RefundNotify.class);
        RefundNotify payNotify = payNotifyBeanUtilsHashMapper.fromHash(refundNotifyMap);

        try {
            ValidationUtil.validate(payNotify);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"????????????");
        }


        // ?????? ??????ID ?????????ID ??????????????????????
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setRefundId(refundId);
        orderServiceQuery.setStatus(OrderStatusEnum.refunding.getCode().byteValue());
        ServiceResult<Integer> countOrder = orderService.countOrder(orderServiceQuery);
        if (!countOrder.getSuccess()) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,"??????????????????");
        }
        if (countOrder.getData() < 1) {
            return ServiceResult.success();
        }

        orderServiceQuery.setNeedRefund(true);
        OrderDTO orderDTO;
        try {
            orderDTO = orderService.selectMainOrder(orderServiceQuery);
        } catch (BusServiceException e) {
            if (e.getMessage().contains("???????????????")) {
                return ServiceResult.success();
            }
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????????????????");
        }
        if (orderDTO == null) {
            return ServiceResult.success();
        }
        Long buyerId = orderDTO.getBuyerId();

        Integer notifyTotalFee = payNotify.getTotal_fee();
        Integer realFee = orderDTO.getRealFee().movePointRight(2).intValue();
        if (!realFee.equals(notifyTotalFee)) {
            // "?????????????????????"
            return ServiceResult.success();
        }

        Integer notifyRefundFee = payNotify.getRefund_fee();
        Integer refundFee = orderDTO.getRefund().getApplyFee().movePointRight(2).intValue();
        if (!refundFee.equals(notifyRefundFee)) {
            // "???????????????????????????"
            return ServiceResult.success();
        }

        // ??????????????????
        String context = "{desc:'??????????????????Notify', content:'" + payNotify + "'}";
        ServiceResult serviceResult;
        if ("SUCCESS".equals(payNotify.getRefund_status())) {
            serviceResult = orderService.confirmRefundedWithNxLock(orderId, refundId, shopId, buyerId, context);
        }
        else if ("REFUNDCLOSE".equals(payNotify.getRefund_status())) {
            try {
                serviceResult = orderService.confirmRefundClosedWithNxLock(orderId, refundId, shopId, buyerId, context, ShopConstants.DEFAULT_OPERATOER);
            } catch (BusServiceException e) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "????????????????????????1");
            }
        }
        else if ("CHANGE".equals(payNotify.getRefund_status())) {
            try {
                serviceResult = orderService.confirmRefundChangdWithNxLock(orderId, refundId, shopId, buyerId, context);
            } catch (BusServiceException e) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "????????????????????????2");
            }
        }else {
            // "????????????????????????"
            return ServiceResult.success();
        }

        if (!serviceResult.getFailType().equals(ServiceFailTypeEnum.BUS_NOT_GET_LOCK)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "????????????????????????");
        }

        return ServiceResult.success();
    }

    private CheckResult checkPayNotify(String notify) {
        Map<String, String> map;
        try {
            map = WXPayUtil.xmlToMap(notify);
        } catch (Exception e) {
            return new CheckResult("????????????");
        }

        String notifyAppId = map.get("appid");
        String notifyMchId = map.get("mch_id");
        String notifySubAppId = map.get("sub_appid");
        String notifySubMchId = map.get("sub_mch_id");

        String appId = StringUtils.isBlank(notifySubAppId) ? notifyAppId : notifySubAppId;
        String mchId = StringUtils.isBlank(notifySubMchId) ? notifyMchId : notifySubMchId;

        // appId ??????shop?
        Long shopId = null;
        try {
            shopId = merchantService.getShopId(appId);
        } catch (BusServiceException e) {
            return new CheckResult("app??????shop??????");
        }
        if (null == shopId) {
            // "????????????"
            return new CheckResult();
        }

        ShopAppDO weixinConfig;
        try {
            weixinConfig = merchantService.getShopAppCache(shopId, appId);
        } catch (Exception e) {
            return new CheckResult("??????????????????????????????");
        }

        PayConfig payConfig;
        try {
            payConfig = merchantService.getPayConfig(weixinConfig);
        } catch (BusServiceException e) {
            // "??????app????????????????????????"
            return new CheckResult();
        }

        if (!weixinConfig.getAppId().equals(appId)) {
            // "appId????????????"
            return new CheckResult();
        }
        if (!payConfig.getMchId().equals(mchId)) {
            // "mchId????????????"
            return new CheckResult();
        }

        PayBaseParam payBaseParam = WeixinMiniProgramService.getPayBaseParam(payConfig, weixinConfig);
        String payKey = WeixinMiniProgramService.getPayKey(payConfig);

        // ??????
        WXPay wxPay;
        try {
            WXPayConfig wxPayConfig = new WXPayConfig();
            wxPayConfig.setAppID(payBaseParam.getAppid());
            wxPayConfig.setMchID(payBaseParam.getMch_id());
            wxPayConfig.setKey(payKey);
            wxPay = new WXPay(wxPayConfig);
        } catch (Exception e) {
            return new CheckResult("????????????");
        }

        boolean signatureValid;
        try {
            signatureValid =  wxPay.isPayResultNotifySignatureValid(map);
        } catch (Exception e) {
            return new CheckResult("??????????????????");
        }
        if (!signatureValid) {
            return new CheckResult("?????????????????????");
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
            return new CheckResult("????????????");
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

        // appId ??????shop?
        Long shopId = null;
        ShopAppDO weixinConfig = null;
        if (!isComponent) {
            try {
                shopId = merchantService.getShopId(appId);
            } catch (BusServiceException e) {
                return new CheckResult("app??????shop??????");
            }
            if (null == shopId) {
                // "????????????"
                return new CheckResult();
            }

            try {
                weixinConfig = merchantService.getShopAppCache(shopId, appId);
            } catch (Exception e) {
                return new CheckResult("??????????????????????????????");
            }

            try {
                payConfig = merchantService.getPayConfig(weixinConfig);
            } catch (BusServiceException e) {
                // "??????app????????????????????????"
                return new CheckResult();
            }

            if (!weixinConfig.getAppId().equals(appId)) {
                // "appId????????????"
                return new CheckResult();
            }
            if (!payConfig.getMchId().equals(mchId)) {
                // "mchId????????????"
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
         * ??????????????? SUCCESS/FAIL ???????????????????????????????????????????????????????????????????????????result_code?????????
         */
        @NotEmpty String return_code;

        String return_msg;

        /*???????????????return_code???SUCCESS?????????????????? */

        /**
         * ??????????????????????????????ID
         */
        @NotEmpty String appid;

        /**
         * ??????????????????????????????
         */
        @NotEmpty String mch_id;

        String sub_appid;

        String sub_mch_id;

        String device_info;

        /**
         * ??????????????? ??????????????????????????????
         */
        @NotEmpty String nonce_str;

        /**
         * ????????????????????????
         */
        @NotEmpty String sign;

        String sign_type;

        /**
         * ???????????? SUCCESS/FAIL
         */
        @NotEmpty String result_code;

        /**
         * ????????????
         */
        String err_code;

        /**
         * ??????????????????
         */
        String err_code_des;


        /* ???????????????return_code ???result_code??????SUCCESS?????????????????? */

        /**
         * ????????????
         */
        @NotEmpty String openid;

        /**
         * ????????????
         */
        String trade_type;

        /**
         * ??????????????????????????????
         */
        @NotNull Integer total_fee;

        /**
         * ?????????????????????
         */
        @NotEmpty String transaction_id;

        /**
         * ???????????????
         */
        @NotEmpty String out_trade_no;

        /**
         * ??????????????????????????????yyyyMMddHHmmss??????2009???12???25???9???10???10????????????20091225091010
         */
        @NotEmpty String time_end;

    }

    @Data
    public static class RefundNotify {

        /**
         * ???????????????
         */
        @NotEmpty String transaction_id;

        /**
         * ???????????????
         */
        @NotEmpty String out_trade_no;

        /**
         * ??????????????????
         */
        @NotEmpty String refund_id;

        /**
         * ??????????????????
         */
        @NotEmpty String out_refund_no;

        /**
         * ??????????????????????????????
         */
        @NotNull Integer total_fee;

        /**
         * ???????????????????????????????????????????????????????????????????????????=????????????-?????????????????????????????????????????????<=???????????????
         */
        Integer settlement_total_fee;

        /**
         * ??????????????????
         */
        @NotNull Integer refund_fee;

        /**
         * ????????????=??????????????????-?????????????????????????????????????????????<=??????????????????
         */
        @NotNull Integer settlement_refund_fee;

        /**
         * SUCCESS-????????????
         * CHANGE-????????????
         * REFUNDCLOSE???????????????
         */
        @NotNull String refund_status;

        /**
         * ?????????????????????????????????????????????2017-12-15 09:46:01
         */
        String success_time;

        /**
         * ??????????????????
         */
        @NotNull String refund_recv_accout;

        /**
         * ??????????????????
         */
        @NotNull String refund_account;

        /**
         * ??????????????????
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
