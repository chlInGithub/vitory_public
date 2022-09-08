package com.chl.victory.service.services;

import java.lang.reflect.Field;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.chl.victory.common.httpclient.HttpClientService;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.localservice.CartLocalService;
import com.chl.victory.localservice.OrderLocalService;
import com.chl.victory.service.services.accesslimit.AccessLimitService;
import com.chl.victory.service.services.activity.ShopActivityService;
import com.chl.victory.service.services.coupons.ShopCouponsService;
import com.chl.victory.service.services.coupons.UserCouponsService;
import com.chl.victory.service.services.experience.ExperienceService;
import com.chl.victory.service.services.fee.ShareSalesFeeService;
import com.chl.victory.service.services.info.InfoService;
import com.chl.victory.service.services.item.ItemService;
import com.chl.victory.service.services.member.MemberService;
import com.chl.victory.service.services.merchant.MerchantImgService;
import com.chl.victory.service.services.merchant.MerchantService;
import com.chl.victory.service.services.merchant.SaleStrategyService;
import com.chl.victory.service.services.merchant.ShopPowerService;
import com.chl.victory.service.services.order.CartService;
import com.chl.victory.service.services.order.OrderService;
import com.chl.victory.service.services.share.ShareService;
import com.chl.victory.service.services.shorturl.ShortUrlService;
import com.chl.victory.service.services.sms.SmsRechargeService;
import com.chl.victory.service.services.sms.SmsSendingService;
import com.chl.victory.service.services.sms.SmsSetService;
import com.chl.victory.service.services.weixin.PayNotifyService;
import com.chl.victory.service.services.weixin.WeixinMiniProgramService;
import com.chl.victory.service.services.weixin.WxPayService;
import com.chl.victory.service.services.weixin.thirdplatform.AuthorizerAccessTokenService;
import com.chl.victory.service.services.weixin.thirdplatform.AuthorizerService;
import com.chl.victory.service.services.weixin.thirdplatform.ComponentService;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4BasicInfoService;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4CategoryService;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4CodeService;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4FastCreateService;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4TesterService;
import com.chl.victory.service.services.weixin.thirdplatform.FoundryMiniProgram4WeixinLoginService;
import com.chl.victory.service.services.weixin.thirdplatform.event.WXEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * service bean 仓库，逻辑代码从这里获取service bean引用。便于service管理，解决service bean循环依赖问题。
 * @author ChenHailong
 * @date 2020/4/13 18:32
 **/
@Repository
@Slf4j
public class ServiceManager {

    public static CacheService cacheService;

    public static OrderService orderService;

    public static CartService cartService;

    public static WeixinMiniProgramService weixinMiniProgramService;

    public static SmsRechargeService smsRechargeService;

    public static SmsSendingService smsSendingService;

    public static SmsSetService smsSetService;

    public static MerchantImgService merchantImgService;

    public static MerchantService merchantService;

    public static MemberService memberService;

    public static ItemService itemService;

    public static InfoService infoService;

    public static ShopCouponsService shopCouponsService;

    public static UserCouponsService userCouponsService;

    public static ShopActivityService shopActivityService;

    public static AccessLimitService accessLimitService;

    public static HttpClientService httpClientService;

    public static ComponentService componentService;

    public static AuthorizerAccessTokenService authorizerAccessTokenService;

    public static FoundryMiniProgram4FastCreateService foundryMiniProgram4FastCreateService;

    public static FoundryMiniProgram4WeixinLoginService foundryMiniProgram4WeixinLoginService;

    public static FoundryMiniProgram4BasicInfoService foundryMiniProgram4BasicInfoService;
    public static FoundryMiniProgram4CategoryService foundryMiniProgram4CategoryService;
    public static FoundryMiniProgram4CodeService foundryMiniProgram4CodeService;
    public static FoundryMiniProgram4TesterService foundryMiniProgram4TesterService;
    public static AuthorizerService authorizerService;
    public static WXEventService wxEventService;

    public static ExperienceService experienceService;
    public static ShortUrlService shortUrlService;
    public static ShareService shareService;
    public static ShareSalesFeeService shareSalesFeeService;

    public static ShopPowerService shopPowerService;
    public static PayNotifyService payNotifyService;
    public static WxPayService wxPayService;

    public static SaleStrategyService saleStrategyService;

    @Resource
    SaleStrategyService _saleStrategyService;

    @Resource
    PayNotifyService _payNotifyService;

    @Resource
    WxPayService _wxPayService;

    @Resource
    ShopPowerService _shopPowerService;

    public static OrderLocalService orderLocalService;

    @Resource
    OrderLocalService _orderLocalService;

    public static CartLocalService cartLocalService;

    @Resource
    CartLocalService _cartLocalService;

    @Resource
    ShareSalesFeeService _shareSalesFeeService;
    @Resource
    ShareService _shareService;
    @Resource
    ShortUrlService _shortUrlService;
    @Resource
    ExperienceService _experienceService;
    @Resource
    WXEventService _wxEventService;
    @Resource
    FoundryMiniProgram4TesterService _foundryMiniProgram4TesterService;
    @Resource
    AuthorizerService _authorizerService;

    @Resource
    FoundryMiniProgram4CodeService _foundryMiniProgram4CodeService;

    @Resource
    FoundryMiniProgram4CategoryService _foundryMiniProgram4CategoryService;

    @Resource
    FoundryMiniProgram4BasicInfoService _foundryMiniProgram4BasicInfoService;

    @Resource
    FoundryMiniProgram4WeixinLoginService _foundryMiniProgram4WeixinLoginService;

    @Resource
    FoundryMiniProgram4FastCreateService _foundryMiniProgram4FastCreateService;

    @Resource
    AuthorizerAccessTokenService _authorizerAccessTokenService;

    @Resource
    HttpClientService _httpClientService;

    @Resource
    ComponentService _componentService;

    @Resource
    CacheService _cacheService;

    @Resource
    OrderService _orderService;

    @Resource
    CartService _cartService;

    @Resource
    WeixinMiniProgramService _weixinMiniProgramService;

    @Resource
    SmsRechargeService _smsRechargeService;

    @Resource
    SmsSendingService _smsSendingService;

    @Resource
    SmsSetService _smsSetService;

    @Resource
    MerchantImgService _merchantImgService;

    @Resource
    MerchantService _merchantService;

    @Resource
    MemberService _memberService;

    @Resource
    ItemService _itemService;

    @Resource
    InfoService _infoService;

    @Resource
    ShopCouponsService _shopCouponsService;

    @Resource
    UserCouponsService _userCouponsService;

    @Resource
    ShopActivityService _shopActivityService;

    @Resource
    AccessLimitService _accessLimitService;

    @PostConstruct
    public void init() throws NoSuchFieldException, IllegalAccessException {
        try {

            Field[] fields = getClass().getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                String fieldname = fields[ i ].getName();
                if (fieldname.startsWith("_")) {
                    String sfieldname = fieldname.substring(1);
                    Field sfield = getClass().getDeclaredField(sfieldname);
                    sfield.setAccessible(true);
                    sfield.set(this, fields[ i ].get(this));
                }
            }
        } catch (Exception e) {
            log.error("serviceManager init EX", e);
            throw e;
        }
    }
}
