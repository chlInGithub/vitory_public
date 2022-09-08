package com.chl.victory.service.services.weixin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.member.ShopMemberDO;
import com.chl.victory.dao.model.merchant.PayConfig;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.dao.model.merchant.ShopDO;
import com.chl.victory.dao.query.member.ShopMemberQuery;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.service.services.weixin.thirdplatform.ComponentService;
import com.chl.victory.service.utils.IPUtil;
import com.chl.victory.service.utils.WeixinMiniProgramUtil;
import com.chl.victory.service.weixinsdk.WXPay;
import com.chl.victory.service.weixinsdk.WXPayConfig;
import com.chl.victory.service.weixinsdk.WXPayConstants;
import com.chl.victory.service.weixinsdk.WXPayUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.common.enums.ThirdPlatformEnum;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.NotExistException;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.exception.WeixinException;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.model.RefundDTO;
import com.chl.victory.serviceapi.weixin.model.WXRequestPaymentParam;
import com.chl.victory.serviceapi.weixin.model.WeixinACodeParam;
import com.chl.victory.serviceapi.weixin.model.WeixinACodeResult;
import com.chl.victory.serviceapi.weixin.model.WeixinAccessToken;
import com.chl.victory.serviceapi.weixin.model.WeixinCode2Session;
import com.chl.victory.serviceapi.weixin.model.WeixinPhone;
import com.chl.victory.serviceapi.weixin.model.WxPhoneDTO;
import com.chl.victory.serviceapi.weixin.model.pay.OrderQueryParam;
import com.chl.victory.serviceapi.weixin.model.pay.OrderQueryResult;
import com.chl.victory.serviceapi.weixin.model.pay.OrderRefundParam;
import com.chl.victory.serviceapi.weixin.model.pay.PayBaseParam;
import com.chl.victory.serviceapi.weixin.model.pay.PayUnifiedOrderParam;
import com.chl.victory.serviceapi.weixin.model.pay.PayUnifiedOrderResult;
import com.chl.victory.serviceapi.weixin.model.pay.RefundQueryResult;
import com.chl.victory.serviceapi.weixin.model.pay.RefundResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.AuthorizerAccessToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.componentService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;
import static com.chl.victory.service.services.ServiceManager.memberService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * @author ChenHailong
 * @date 2019/12/18 15:49
 **/
@Service
@ConfigurationProperties("weixin")
@Slf4j
public class WeixinMiniProgramService {

    private static String[] chars = {};

    final String tradeType = "JSAPI";

    final int weixinAccessTokenTimeoutSecounds = 60 * 60 * 2;

    @Setter
    private String tokenURI;

    @Setter
    private String code2SessionURI;

    @Setter
    private String payUnifiedOrderURI;

    @Setter
    private String notifyPayURI;

    @Setter
    private String notifyRefundURI;

    /**
     * 微信支付证书存放路径
     */
    @Setter
    private String certPath;

    @Setter
    private String getwxacodeunlimit;

    /**
     * 32个字符的随机字符串
     */
    public static String genNonceStr() {
        String nonceStr = WXPayUtil.generateNonceStr();
        return nonceStr;
        /*StringBuilder builder = new StringBuilder();
        builder.append(RandomStringUtils.randomAlphabetic(7)).append(RandomUtils.nextInt(0, 9))
                .append(RandomStringUtils.randomAlphabetic(7)).append(RandomUtils.nextInt(0, 9))
                .append(RandomStringUtils.randomAlphabetic(7)).append(RandomUtils.nextInt(0, 9))
                .append(RandomStringUtils.randomAlphabetic(7)).append(RandomUtils.nextInt(0, 9));
        return builder.toString();*/
    }

    public static PayBaseParam getPayBaseParam() {
        PayBaseParam payBaseParam = new PayBaseParam();
        ComponentService.PayComponentConfig payComponentConfig = componentService.getPayComponentConfig();
        payBaseParam.setAppid(payComponentConfig.getAppId());
        payBaseParam.setMch_id(payComponentConfig.getMchId());
        return payBaseParam;
    }

    public static PayBaseParam getPayBaseParam(@NotNull PayConfig payConfig, @NotNull ShopAppDO shopAppDO) {
        PayBaseParam payBaseParam = new PayBaseParam();
        if (payConfig.isSub()) {
            ComponentService.PayComponentConfig payComponentConfig = componentService.getPayComponentConfig();
            payBaseParam.setAppid(payComponentConfig.getAppId());
            payBaseParam.setMch_id(payComponentConfig.getMchId());
            payBaseParam.setSub_appid(shopAppDO.getAppId());
            payBaseParam.setSub_mch_id(payConfig.getMchId());
        }
        else {
            payBaseParam.setAppid(shopAppDO.getAppId());
            payBaseParam.setMch_id(payConfig.getMchId());
        }
        return payBaseParam;
    }

    public static String getPayKey() {
        return getPayKey(null);
    }

    public static String getPayKey(PayConfig payConfig) {
        ComponentService.PayComponentConfig payComponentConfig = componentService.getPayComponentConfig();
        // TODO 可能需要解密
        return null == payConfig || payConfig.isSub() ? payComponentConfig.getApiKey() : payConfig.getApiKey();
    }

    /**
     * 调用weixinSDK包接口，查询订单是否支付成功
     * @param shopDO
     * @param weixinConfig
     * @param orderDTO
     * @return
     * @throws BusServiceException
     */
    public boolean checkPayedViaWXSDK(@NotNull ShopDO shopDO, @NotNull ShopAppDO weixinConfig,
            @NotNull OrderDTO orderDTO) throws BusServiceException {
        PayConfig payConfig = merchantService.getPayConfig(weixinConfig);

        String outTradeNo = orderDTO.getId().toString();

        PayBaseParam payBaseParam = getPayBaseParam(payConfig, weixinConfig);
        String payKey = getPayKey(payConfig);

        OrderQueryParam param = new OrderQueryParam();
        param.setSub_appid(payBaseParam.getSub_appid());
        param.setSub_mch_id(payBaseParam.getSub_mch_id());
        param.setOut_trade_no(outTradeNo);

        BeanUtilsHashMapper<OrderQueryParam> beanUtilsHashMapper = new BeanUtilsHashMapper<>(OrderQueryParam.class);
        Map<String, String> params = beanUtilsHashMapper.toHash(param);
        params.remove("class");

        WXPayConfig wxPayConfig = new WXPayConfig();
        wxPayConfig.setKey(payKey);
        wxPayConfig.setAppID(payBaseParam.getAppid());
        wxPayConfig.setMchID(payBaseParam.getMch_id());
        Map<String, String> unifiedOrderResult;
        try {
            WXPay wxPay = new WXPay(wxPayConfig);
            unifiedOrderResult = wxPay.orderQuery(params);
        } catch (Exception e) {
            log.error("checkPayed|{}", orderDTO.getId(), e);
            throw new BusServiceException("查询订单微信支付结果失败", e);
        }

        BeanUtilsHashMapper<OrderQueryResult> beanUtilsHashMapper4WeixinPayUnifiedOrderResult = new BeanUtilsHashMapper<>(
                OrderQueryResult.class);
        OrderQueryResult result = beanUtilsHashMapper4WeixinPayUnifiedOrderResult.fromHash(unifiedOrderResult);

        if (!result.isReturnSuccess()) {
            log.warn("checkPayed|{}|{}", orderDTO.getId(), result.getReturn_msg());
            throw new BusServiceException(result.getReturn_msg());
        }

        if (!result.isResultSuccess()) {
            log.warn("checkPayed|{}|{}", orderDTO.getId(), result.getErr_code_des());
            throw new BusServiceException(result.getErr_code_des());
        }

        return result.isPaySuccess();
    }

    /**
     * 调用weixinSDK包接口，查询订单退款情况
     * @param weixinConfig
     * @param orderId
     * @return
     * @throws BusServiceException
     */
    public RefundQueryResult checkRefunViaWXSDK(@NotNull ShopAppDO weixinConfig, @NotNull Long orderId)
            throws BusServiceException {
        PayConfig payConfig = merchantService.getPayConfig(weixinConfig);

        String outTradeNo = orderId.toString();

        PayBaseParam payBaseParam = getPayBaseParam(payConfig, weixinConfig);
        String payKey = getPayKey(payConfig);

        OrderQueryParam param = new OrderQueryParam();
        param.setSub_appid(payBaseParam.getSub_appid());
        param.setSub_mch_id(payBaseParam.getSub_mch_id());
        param.setOut_trade_no(outTradeNo);

        BeanUtilsHashMapper<OrderQueryParam> beanUtilsHashMapper = new BeanUtilsHashMapper<>(OrderQueryParam.class);
        Map<String, String> params = beanUtilsHashMapper.toHash(param);
        params.remove("class");

        WXPayConfig wxPayConfig = new WXPayConfig();
        wxPayConfig.setKey(payKey);
        wxPayConfig.setAppID(payBaseParam.getAppid());
        wxPayConfig.setMchID(payBaseParam.getMch_id());
        Map<String, String> unifiedOrderResult;
        try {
            WXPay wxPay = new WXPay(wxPayConfig);
            unifiedOrderResult = wxPay.refundQuery(params);
        } catch (Exception e) {
            log.error("checkRefund|{}", orderId, e);
            throw new BusServiceException("查询订单微信退款结果失败", e);
        }

        BeanUtilsHashMapper<RefundQueryResult> beanUtilsHashMapper4WeixinPayUnifiedOrderResult = new BeanUtilsHashMapper<>(
                RefundQueryResult.class);
        RefundQueryResult result = beanUtilsHashMapper4WeixinPayUnifiedOrderResult.fromHash(unifiedOrderResult);

        if (!result.isReturnSuccess()) {
            log.warn("checkPayed|{}|{}", orderId, result.getReturn_msg());
            throw new BusServiceException(result.getReturn_msg());
        }

        if (!result.isResultSuccess()) {
            log.warn("checkPayed|{}|{}", orderId, result.getErr_code_des());
            throw new BusServiceException(result.getErr_code_des());
        }

        return result;
    }

    /**
     * 调用weixinSDK包接口，退款申请。无异常即表示已申请退款，成功与否要看退款notify。
     * @param shopDO
     * @param weixinConfig
     * @param orderDTO 必须携带refundDto
     * @return
     * @throws BusServiceException
     */
    public RefundResult refundViaWXSDK(@NotNull ShopDO shopDO, @NotNull ShopAppDO weixinConfig,
            @NotNull OrderDTO orderDTO) throws BusServiceException {

        PayConfig payConfig = merchantService.getPayConfig(weixinConfig);

        // 组织除sign外所有参数
        RefundDTO refundDTO = orderDTO.getRefund();
        if (null == refundDTO) {
            throw new BusServiceException("参数缺少退款单");
        }

        if (refundDTO.getApplyFee().compareTo(orderDTO.getRealFee()) > 0) {
            throw new BusServiceException("退款金额不可大于订单实际金额");
        }

        // 退款需要证书
        byte[] certData; // = getCertData(shopDO.getId(), payConfig.getMchId());
        certData = getWXPayCertDataFromDB(shopDO.getId(), weixinConfig.getAppId());

        String outTradeNo = orderDTO.getId().toString();
        String outRefundNo = refundDTO.getId().toString();
        Integer totalFee = orderDTO.getRealFee().movePointRight(2).intValue();
        Integer refundFee = refundDTO.getApplyFee().movePointRight(2).intValue();

        PayBaseParam payBaseParam = getPayBaseParam(payConfig, weixinConfig);
        String payKey = getPayKey(payConfig);

        OrderRefundParam param = new OrderRefundParam();
        param.setSub_appid(payBaseParam.getSub_appid());
        param.setSub_mch_id(payBaseParam.getSub_mch_id());
        param.setOut_trade_no(outTradeNo);
        param.setOut_refund_no(outRefundNo);
        param.setTotal_fee(totalFee);
        param.setRefund_fee(refundFee);
        // 因服务器解密问题，定时任务查询退款结果，不采用notify
        // param.setNotify_url(notifyRefundURI);

        BeanUtilsHashMapper<OrderRefundParam> beanUtilsHashMapper = new BeanUtilsHashMapper<>(OrderRefundParam.class);
        Map<String, String> params = beanUtilsHashMapper.toHash(param);
        params.remove("class");

        WXPayConfig wxPayConfig = new WXPayConfig();
        wxPayConfig.setCertData(certData);
        wxPayConfig.setKey(payKey);
        wxPayConfig.setAppID(payBaseParam.getAppid());
        wxPayConfig.setMchID(payBaseParam.getMch_id());

        Map<String, String> refundResult;
        try {
            WXPay wxPay = new WXPay(wxPayConfig);
            refundResult = wxPay.refund(params);
        } catch (Exception e) {
            log.error("refundViaWXSDK|{}", orderDTO.getId(), e);
            throw new BusServiceException("微信支付订单退款申请失败", e);
        }

        BeanUtilsHashMapper<RefundResult> beanUtilsHashMapper4WeixinPayUnifiedOrderResult = new BeanUtilsHashMapper<>(
                RefundResult.class);
        RefundResult result = beanUtilsHashMapper4WeixinPayUnifiedOrderResult.fromHash(refundResult);

        if (!result.isReturnSuccess()) {
            log.warn("refundViaWXSDK|{}|{}", orderDTO.getId(), JSONObject.toJSONString(result));
            throw new BusServiceException(result.getReturn_msg());
        }

        if (!result.isResultSuccess()) {
            log.warn("refundViaWXSDK|{}|{}", orderDTO.getId(), JSONObject.toJSONString(result));
            throw new BusServiceException(result.getErr_code_des());
        }

        return result;
    }

    private byte[] getWXPayCertDataFromDB(Long shopId, String appId) throws BusServiceException {
        ServiceResult<byte[]> serviceResult = merchantService.selectWXPayCert(shopId, appId);
        if (!serviceResult.getSuccess()) {
            throw new BusServiceException("获取微信支付证书失败");
        }
        if (ArrayUtils.isEmpty(serviceResult.getData())) {
            throw new BusServiceException("缺失微信支付证书");
        }
        return serviceResult.getData();
    }

    /**
     * 读取证书  https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=4_3
     * @param shopId
     * @return
     */
    private byte[] getCertData(Long shopId, String mchId) throws BusServiceException {
        // 从某路径读取shopId对应的证书，证书命名规则：shopId_apiclient_cert.p12
        String certPath = getCertPath(shopId, mchId);
        byte[] certData;
        try {
            File file = new File(certPath);
            InputStream certStream = new FileInputStream(file);
            certData = new byte[ (int) file.length() ];
            certStream.read(certData);
            certStream.close();
        } catch (FileNotFoundException e) {
            throw new BusServiceException("读取微信支付证书异常-证书不存在" + certPath + shopId, e);
        } catch (IOException e) {
            throw new BusServiceException("读取微信支付证书异常" + shopId, e);
        }
        return certData;
    }

    private String getCertPath(Long shopId, String mchId) {
        return certPath + shopId + "_" + mchId + "_apiclient_cert.p12";
    }

    /**
     * 调用weixinSDK，微信统一支付
     * @param shopDO
     * @param weixinConfig
     * @param orderDTO
     * @return
     * @throws BusServiceException
     */
    public String payUnifiedOrderViaWXSDK(@NotNull ShopDO shopDO, @NotNull ShopAppDO weixinConfig,
            @NotNull OrderDTO orderDTO, @NotEmpty String openId) throws BusServiceException {
        PayConfig payConfig = merchantService.getPayConfig(weixinConfig);

        return _payUnifiedOrderViaWXSDK(shopDO, weixinConfig, payConfig, orderDTO, openId);
    }

    private String _payUnifiedOrderViaWXSDK(ShopDO shopDO, ShopAppDO weixinConfig, PayConfig payConfig,
            OrderDTO orderDTO, String openId) throws BusServiceException {
        String outTradeNo = orderDTO.getId().toString();
        Integer totalFee = orderDTO.getRealFee().movePointRight(2).intValue();
        String body = shopDO.getName() + "_" + outTradeNo;
        String hostIP = IPUtil.getHostIP();

        PayBaseParam payBaseParam = getPayBaseParam(payConfig, weixinConfig);
        String payKey = getPayKey(payConfig);

        PayUnifiedOrderParam payUnifiedOrderParam = new PayUnifiedOrderParam();
        payUnifiedOrderParam.setSub_appid(payBaseParam.getSub_appid());
        payUnifiedOrderParam.setSub_mch_id(payBaseParam.getSub_mch_id());
        payUnifiedOrderParam.setBody(body);
        payUnifiedOrderParam.setOut_trade_no(outTradeNo);
        payUnifiedOrderParam.setSpbill_create_ip(hostIP);
        payUnifiedOrderParam.setTotal_fee(totalFee);
        payUnifiedOrderParam.setTrade_type(tradeType);

        if (payBaseParam.getSub_appid() != null) {
            payUnifiedOrderParam.setSub_openid(openId);
        }
        else {
            payUnifiedOrderParam.setOpenid(openId);
        }

        BeanUtilsHashMapper<PayUnifiedOrderParam> beanUtilsHashMapper = new BeanUtilsHashMapper<>(
                PayUnifiedOrderParam.class);
        Map<String, String> params = beanUtilsHashMapper.toHash(payUnifiedOrderParam);
        params.remove("class");

        WXPayConfig wxPayConfig = new WXPayConfig();
        wxPayConfig.setKey(payKey);
        wxPayConfig.setAppID(payBaseParam.getAppid());
        wxPayConfig.setMchID(payBaseParam.getMch_id());
        Map<String, String> unifiedOrderResult;
        try {
            WXPay wxPay = new WXPay(wxPayConfig, notifyPayURI, false, false);
            unifiedOrderResult = wxPay.unifiedOrder(params);
        } catch (Exception e) {
            log.error("payUnifiedOrderViaWXSDK|{}", orderDTO.getId(), e);
            throw new BusServiceException("创建微信支付订单失败", e);
        }

        BeanUtilsHashMapper<PayUnifiedOrderResult> beanUtilsHashMapper4WeixinPayUnifiedOrderResult = new BeanUtilsHashMapper<>(
                PayUnifiedOrderResult.class);
        PayUnifiedOrderResult weixinPayUnifiedOrderResult = beanUtilsHashMapper4WeixinPayUnifiedOrderResult
                .fromHash(unifiedOrderResult);

        if (!weixinPayUnifiedOrderResult.isReturnSuccess()) {
            throw new BusServiceException(weixinPayUnifiedOrderResult.getReturn_msg());
        }

        if (!weixinPayUnifiedOrderResult.isResultSuccess()) {
            throw new BusServiceException(weixinPayUnifiedOrderResult.getErr_code_des());
        }

        String prepay_id = weixinPayUnifiedOrderResult.getPrepay_id();

        return prepay_id;

    }

    /**
     * 小程序调起微信支付 准备参数
     * @param weixinConfig
     * @param prePayId
     * @return
     * @throws BusServiceException
     */
    public WXRequestPaymentParam genWxRequestPaymentParam(@NotNull ShopAppDO weixinConfig, @NotNull String prePayId)
            throws BusServiceException {
        PayConfig payConfig = merchantService.getPayConfig(weixinConfig);

        Map<String, String> params = new HashMap<>();
        String appId = weixinConfig.getAppId();
        String timeStamp = System.currentTimeMillis() / 1000 + "";
        String nonceStr = genNonceStr();
        String signType = WXPayConstants.HMACSHA256;
        params.put("appId", appId);
        params.put("timeStamp", timeStamp);
        params.put("nonceStr", nonceStr);
        params.put("package", "prepay_id=" + prePayId);
        params.put("signType", signType);

        String apiKey = getPayKey(payConfig);
        String sign;
        try {
            sign = WXPayUtil.generateSignature(params, apiKey, WXPayConstants.SignType.HMACSHA256);
        } catch (Exception e) {
            throw new BusServiceException("生成支付签名异常", e);
        }

        WXRequestPaymentParam wxRequestPaymentParam = new WXRequestPaymentParam();
        wxRequestPaymentParam.setTimeStamp(timeStamp);
        wxRequestPaymentParam.setNonceStr(nonceStr);
        wxRequestPaymentParam.setPrepayId(prePayId);
        wxRequestPaymentParam.setSignType(signType);
        wxRequestPaymentParam.setPaySign(sign);

        return wxRequestPaymentParam;
    }

    public ServiceResult saveShopAndWeixinUser(@NotNull Long shopId, @NotEmpty String thirdId, @NotEmpty String appId,
            @NotEmpty String openId) {
        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setShopId(shopId);
        shopMemberQuery.setThirdId(thirdId);
        shopMemberQuery.setAppId(appId);
        shopMemberQuery.setOpenId(openId);
        List<ShopMemberDO> selectMem = null;
        try {
            selectMem = memberService.selectMem(shopMemberQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, ExceptionUtil.trimExMsg(e));
        }

        if (!CollectionUtils.isEmpty(selectMem) && selectMem.size() > 1) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "会员信息错误1");
        }

        ShopMemberDO shopMemberDO = new ShopMemberDO();
        if (!CollectionUtils.isEmpty(selectMem)) {
            shopMemberDO = selectMem.get(0);
        }

        shopMemberDO.setShopId(shopId);
        shopMemberDO.setThirdId(ThirdPlatformEnum.weixin.getCode().toString());
        shopMemberDO.setAppId(appId);
        shopMemberDO.setOpenId(openId);

        int saveResult = 0;
        try {
            saveResult = memberService.saveMem(shopMemberDO);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, ExceptionUtil.trimExMsg(e));
        }

        if (saveResult < 1) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "保存失败");
        }
        return ServiceResult.success();
    }

    /**
     * @param shopAppDO
     * @return
     */
    public WeixinAccessToken getAccessToken(@NotNull ShopAppDO shopAppDO) throws BusServiceException {
        WeixinAccessToken weixinAccessToken = null;
        boolean authOrFastRegiste = merchantService.isAuthOrFastRegiste(shopAppDO);
        if (authOrFastRegiste) {
            AuthorizerAccessToken accessToken = ServiceManager.authorizerAccessTokenService.getAccessToken(shopAppDO);
            if (null != accessToken) {
                weixinAccessToken = new WeixinAccessToken();
                weixinAccessToken.setAccess_token(accessToken.getAuthorizer_access_token());
            }
        }
        else {
            if (StringUtils.isBlank(shopAppDO.getAppSecret())) {
                throw new NotExistException("not authOrFastRegiste, need appSecret.");
            }

            String appId = shopAppDO.getAppId();
            String secret = shopAppDO.getAppSecret();
            weixinAccessToken = getAccessTokenCache(appId, secret);

            if (null == weixinAccessToken) {
                getNewAccessToken(appId, secret);

                weixinAccessToken = getAccessTokenCache(appId, secret);
            }
        }

        if (null == weixinAccessToken) {
            throw new NotExistException("null accessToken");
        }

        return weixinAccessToken;
    }

    void setAccessTokenCache(@NotEmpty String appid, @NotEmpty String secret,
            @NotNull WeixinAccessToken weixinAccessToken) {
        String key = CacheKeyPrefix.WEIXIN_ACCESSTOKEN;
        String field = appid + CacheKeyPrefix.SEPARATOR + secret;
        cacheService.hSet(key, field, weixinAccessToken);
        cacheService.expire(key, weixinAccessTokenTimeoutSecounds);
    }

    void delAccessTokenCache() {
        String key = CacheKeyPrefix.WEIXIN_ACCESSTOKEN;
        cacheService.delKey(key);
    }

    WeixinAccessToken getAccessTokenCache(@NotEmpty String appid, @NotEmpty String secret) {
        String key = CacheKeyPrefix.WEIXIN_ACCESSTOKEN;
        String field = appid + CacheKeyPrefix.SEPARATOR + secret;
        WeixinAccessToken weixinAccessToken = cacheService.hGet(key, field, WeixinAccessToken.class);
        return weixinAccessToken;
    }

    WeixinAccessToken getNewAccessTokenFromWeixin(String appid, String secret) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("grant_type", "client_credential");
        vars.put("appid", appid);
        vars.put("secret", secret);
        WeixinAccessToken weixinAccessToken = httpClientService.get(tokenURI, vars, WeixinAccessToken.class);
        if (!weixinAccessToken.isSuccess()) {
            throw new ThirdPlatformServiceException(weixinAccessToken.getError());
        }
        return weixinAccessToken;
    }

    /**
     * auth.code2Session <br/>
     * @param code
     * @return
     */

    public WeixinCode2Session getCode2SessionFromWeixin(@NotNull ShopAppDO shopAppDO, @NotEmpty String code) {
        WeixinCode2Session code2SessionFromWeixin;
        boolean authOrFastRegiste = merchantService.isAuthOrFastRegiste(shopAppDO);
        if (authOrFastRegiste) {
            code2SessionFromWeixin = ServiceManager.foundryMiniProgram4WeixinLoginService.getSession(shopAppDO, code);
        }
        else {
            if (StringUtils.isBlank(shopAppDO.getAppSecret())) {
                throw new NotExistException("not authOrFastRegiste, need appSecret.");
            }
            code2SessionFromWeixin = getCode2SessionFromWeixin(shopAppDO.getAppId(), shopAppDO.getAppSecret(), code);
        }
        return code2SessionFromWeixin;
    }

    WeixinCode2Session getCode2SessionFromWeixin(String appid, String secret, String code) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("grant_type", "authorization_code");
        vars.put("appid", appid);
        vars.put("secret", secret);
        vars.put("js_code", code);
        String response = httpClientService.get(code2SessionURI, vars, String.class);
        WeixinCode2Session weixinCode2Session = JSONObject.parseObject(response, WeixinCode2Session.class);
        if (!weixinCode2Session.isSuccess()) {
            throw new ThirdPlatformServiceException(weixinCode2Session.getError());
        }
        return weixinCode2Session;
    }

    /**
     * 获取新的accessToken，存入cache
     * @param appid
     * @return
     */
    WeixinAccessToken getNewAccessToken(String appid) {
        // TODO
        return null;
    }

    WeixinAccessToken getNewAccessToken(String appid, String secret) {
        String nxLockKey = CacheKeyPrefix.WEIXIN_ACCESSTOKEN + appid + CacheKeyPrefix.SEPARATOR + secret;
        String nxLockRandom = cacheService.getNXLock(nxLockKey, 1L);

        WeixinAccessToken weixinAccessToken = null;
        if (null != nxLockRandom) {
            // 重新获取并更新cache
            try {
                weixinAccessToken = getNewAccessTokenFromWeixin(appid, secret);
                if (StringUtils.isNotBlank(weixinAccessToken.getAccess_token())) {
                    setAccessTokenCache(appid, secret, weixinAccessToken);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                cacheService.releaseNXLock(nxLockKey, nxLockRandom);
            }
        }
        else {
            // 间隔等待一段时间，判断排它锁是否已经消失
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                boolean existsKey = cacheService.existsKey(nxLockKey);
                if (!existsKey) {
                    weixinAccessToken = getAccessTokenCache(appid, secret);
                    break;
                }
            }
        }

        return weixinAccessToken;
    }

    /**
     * 更新店铺的微信accessToken
     * @param shopIds 限定shop范围，empty or null 则所有店铺。
     */
    public void refreshAccessToken4Shops(List<Long> shopIds) {
        // 直接删除cache
        delAccessTokenCache();

        // TODO 查询所有店铺与关联的appid和secret

        // TODO 遍历更新
    }

    /**
     * 获取小程序码，适用于需要的码数量极多的业务场景。通过该接口生成的小程序码，永久有效，数量暂无限制。
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/qr-code/wxacode.getUnlimited.html
     * @param shopAppDO
     * @param weixinACodeParam
     * @return
     */
    public byte[] genCode(@NotNull ShopAppDO shopAppDO, WeixinACodeParam weixinACodeParam) throws BusServiceException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("access_token", getAccessToken(shopAppDO).getAccess_token());

        String scene = "0";
        String page = null;
        Integer width = 280;

        if (null != weixinACodeParam) {
            if (StringUtils.isNotBlank(weixinACodeParam.getScene())) {
                scene = weixinACodeParam.getScene();
            }
            if (StringUtils.isNotBlank(weixinACodeParam.getPage())) {
                page = weixinACodeParam.getPage();
            }
            if (null != weixinACodeParam.getWidth()) {
                width = weixinACodeParam.getWidth();
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("scene", scene);
        if (StringUtils.isNotBlank(page)) {
            params.put("page", page);
        }
        params.put("width", width);
        String request = JSONObject.toJSONString(params);

        byte[] response = httpClientService.post(getwxacodeunlimit, request, byte[].class, vars);
        if (response.length < 200) {
            WeixinACodeResult weixinACodeResult = JSONObject.parseObject(new String(response), WeixinACodeResult.class);
            /*String msg = weixinACodeResult.getErrcode() == 45009 ?
                    "调用分钟频率受限" :
                    (weixinACodeResult.getErrcode() == 41030 ? "所传page页面不存在，或者小程序没有发布" : "未知错误");*/
            String msg = weixinACodeResult.getError();
            throw new WeixinException(msg);
        }
        return response;
    }

    public ServiceResult<String> parseAndSavePhone(@NotNull WxPhoneDTO wxPhoneDTO) {
        ValidationUtil.validate(wxPhoneDTO);

        String sessionKey = wxPhoneDTO.getSessionKey();
        String encryptedData = wxPhoneDTO.getEncryptedData();
        String iv = wxPhoneDTO.getIv();
        WeixinPhone weixinPhone = WeixinMiniProgramUtil.getWeixinPhone(sessionKey, encryptedData, iv);
        if (null == weixinPhone) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "解密微信手机号失败");
        }
        if (!NumberUtils.isDigits(weixinPhone.getPurePhoneNumber())) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "不是正确的手机号");
        }

        if (!StringUtils.isEmpty(wxPhoneDTO.getCurrentMobile()) && weixinPhone.getPurePhoneNumber()
                .equals(wxPhoneDTO.getCurrentMobile())) {
            return ServiceResult.success(wxPhoneDTO.getCurrentMobile());
        }

        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setShopId(wxPhoneDTO.getShopId());
        shopMemberQuery.setThirdId(wxPhoneDTO.getTId().toString());
        shopMemberQuery.setId(wxPhoneDTO.getUserId());
        ShopMemberDO memberDO;
        try {
            memberDO = memberService.assertMem(shopMemberQuery);
        } catch (BusServiceException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "用户不存在");
        }

        if (memberDO.getMobile() == null || !weixinPhone.getPurePhoneNumber()
                .equals(memberDO.getMobile().toString())) {
            memberDO.setMobile(Long.valueOf(weixinPhone.getPurePhoneNumber()));
            int saveResult;
            try {
                saveResult = memberService.saveMem(memberDO);
            } catch (DaoManagerException e) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, ExceptionUtil.trimExMsg(e));
            }
            if (saveResult < 1){
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "保存手机号失败");
            }
        }

        return ServiceResult.success(weixinPhone.getPurePhoneNumber());
    }
}


