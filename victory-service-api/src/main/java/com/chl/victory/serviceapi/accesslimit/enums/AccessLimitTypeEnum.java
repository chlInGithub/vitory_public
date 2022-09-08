package com.chl.victory.serviceapi.accesslimit.enums;

/**
 * 限流类型，如对所有接口、对下单等。限流类型 * 时间周期 构成多种限流方案。
 * @author ChenHailong
 * @date 2020/4/10 14:33
 **/
public enum AccessLimitTypeEnum {
    // 用于2b商城管理系统
    WM_SESSION_SHOP_ALL_INTERFACE(1, "session shop维度，所有接口访问总量限流")
    ,WM_SHOP_ALL_INTERFACE(2, "shop维度，所有接口访问总量限流")
    ,WM_SHOP_ITEM_TOTAL(3, "shop维度，发布商品量限流")
    ,WM_SHOP_IMG_SIZE_TOTAL(4, "shop维度，图片总空间限流")
    ,WM_SHOP_COUPON_TOTAL(5, "shop维度，优惠券总量限流")
    ,WM_SHOP_ACTIVITY_TOTAL(6, "shop维度，活动总量限流")
    ,WM_SHOP_APP_TOTAL(7, "shop维度，绑定app总量限流")
    ,WM_SHOP_SALE_STRATEGY_TOTAL(8, "shop维度，销售策略总量限流")

    // 用于2c商城系统
    ,WMALL_SHOP_CREATE_ORDER(100, "shop维度, 下单量限流")
    ,WMALL_USER_SHOP_CART_COMPUTE(101, "用户 shop 维度，计算购物车金额接口访问量限流")
    ,WMALL_USER_SHOP_ORDER_COMPUTE(102, "用户 shop 维度，计算订单金额接口访问量限流")
    ,WMALL_USER_SHOP_ORDER_CREATE(103, "用户 shop 维度，创建订单接口访问量限流")
    ,WMALL_SHOP_ALL_INTERFACE(110, "shop 维度，所有接口访问量限流")
    ,WMALL_SESSION_SHOP_ALL_INTERFACE(120, "session shop维度，所有接口访问总量限流")
    ,WMALL_SHOP_SHARE(130, "shop维度，非微导购，获取分享小程序码总量限流")
    ,WMALL_SHOP_WEISALES_SHARE(131, "shop维度，微导购，获取分享小程序码总量限流")
    ;

    private Integer code;
    private String desc;

    AccessLimitTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static  AccessLimitTypeEnum getByCode(Integer code){
        for (AccessLimitTypeEnum shopActivityStatusEnum : AccessLimitTypeEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;    }
}
