package com.chl.victory.webcommon.manager;

import java.lang.reflect.Field;
import javax.annotation.PostConstruct;

import com.chl.victory.serviceapi.accesslimit.AccessLimitFacade;
import com.chl.victory.serviceapi.activity.ShopActivityFacade;
import com.chl.victory.serviceapi.cart.CartFacade;
import com.chl.victory.serviceapi.coupons.ShopCouponsFacade;
import com.chl.victory.serviceapi.coupons.UserCouponsFacade;
import com.chl.victory.serviceapi.item.ItemFacade;
import com.chl.victory.serviceapi.member.MemberFacade;
import com.chl.victory.serviceapi.merchant.MerchantFacade;
import com.chl.victory.serviceapi.merchant.MerchantImgFacade;
import com.chl.victory.serviceapi.merchant.SaleStrategyFacade;
import com.chl.victory.serviceapi.merchant.ShopPowerFacade;
import com.chl.victory.serviceapi.order.OrderFacade;
import com.chl.victory.serviceapi.share.ShareFacade;
import com.chl.victory.serviceapi.weixin.AuthorizerFacade;
import com.chl.victory.serviceapi.weixin.ComponentFacade;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4BasicInfoFacade;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4CategoryFacade;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4CodeFacade;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4FastCreateFacade;
import com.chl.victory.serviceapi.weixin.FoundryMiniProgram4TesterFacade;
import com.chl.victory.serviceapi.weixin.InfoAndEventFacade;
import com.chl.victory.serviceapi.weixin.MiniProgramFacade;
import com.chl.victory.serviceapi.weixin.WxPayFacade;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * @author ChenHailong
 * @date 2020/8/24 16:02
 **/
@Component
@Slf4j
public class RpcManager {
    @DubboReference
    AccessLimitFacade _accessLimitFacade;
    @DubboReference
    ShopActivityFacade _shopActivityFacade;
    @DubboReference
    ItemFacade _itemFacade;
    @DubboReference
    ShopCouponsFacade _shopCouponsFacade;
    @DubboReference
    UserCouponsFacade _userCouponsFacade;
    @DubboReference
    MemberFacade _memberFacade;
    @DubboReference
    OrderFacade _orderFacade;
    @DubboReference
    MerchantImgFacade _merchantImgFacade;
    @DubboReference
    MerchantFacade _merchantFacade;
    @DubboReference
    MiniProgramFacade _miniProgramFacade;
    @DubboReference
    ComponentFacade _componentFacade;
    @DubboReference
    FoundryMiniProgram4FastCreateFacade _foundryMiniProgram4FastCreateFacade;
    @DubboReference
    FoundryMiniProgram4BasicInfoFacade _foundryMiniProgram4BasicInfoFacade;
    @DubboReference
    FoundryMiniProgram4CodeFacade _foundryMiniProgram4CodeFacade;
    @DubboReference
    FoundryMiniProgram4TesterFacade _foundryMiniProgram4TesterFacade;
    @DubboReference
    FoundryMiniProgram4CategoryFacade _foundryMiniProgram4CategoryFacade;
    @DubboReference
    AuthorizerFacade _authorizerFacade;
    @DubboReference
    InfoAndEventFacade _infoAndEventFacade;
    @DubboReference
    CartFacade _cartFacade;
    @DubboReference
    ShareFacade _shareFacade;
    @DubboReference
    WxPayFacade _wxPayFacade;
    @DubboReference
    SaleStrategyFacade _saleStrategyFacade;

    public static SaleStrategyFacade saleStrategyFacade;
    public static ShopActivityFacade shopActivityFacade;
    public static AccessLimitFacade accessLimitFacade;
    public static ItemFacade itemFacade;
    public static ShopCouponsFacade shopCouponsFacade;
    public static UserCouponsFacade userCouponsFacade;
    public static MemberFacade memberFacade;
    public static OrderFacade orderFacade;
    public static MerchantImgFacade merchantImgFacade;
    public static MerchantFacade merchantFacade;
    public static MiniProgramFacade miniProgramFacade;
    public static ComponentFacade componentFacade;
    public static FoundryMiniProgram4FastCreateFacade foundryMiniProgram4FastCreateFacade;
    public static FoundryMiniProgram4BasicInfoFacade foundryMiniProgram4BasicInfoFacade;
    public static FoundryMiniProgram4CodeFacade foundryMiniProgram4CodeFacade;
    public static FoundryMiniProgram4TesterFacade foundryMiniProgram4TesterFacade;
    public static FoundryMiniProgram4CategoryFacade foundryMiniProgram4CategoryFacade;
    public static AuthorizerFacade authorizerFacade;
    public static InfoAndEventFacade infoAndEventFacade;
    public static CartFacade cartFacade;
    public static ShareFacade shareFacade;
    public static WxPayFacade wxPayFacade;

    @PostConstruct
    public void init() {
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
        } catch (IllegalArgumentException e) {
            log.error("", e);
        } catch (NoSuchFieldException e) {
            log.error("", e);
        } catch (SecurityException e) {
            log.error("", e);
        } catch (IllegalAccessException e) {
            log.error("", e);
        }
    }
}
