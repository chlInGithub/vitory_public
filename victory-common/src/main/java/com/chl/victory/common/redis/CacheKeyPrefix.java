package com.chl.victory.common.redis;

/**
 * 缓存key与key前缀
 * <br/>
 *     redis key前缀 命名规则<br/>
 *         key前缀直接作为key的情况<br/>
 *             <b>什么数据_OF_维度_数据结构类型</b><br/>
 *         由key前缀 + 具体维度数据 构成实际key的情况<br/>
 *             <b>什么数据_OF_维度_数据结构类型:</b><br/>
 * @author ChenHailong
 * @date 2019/12/4 17:03
 **/
public final class CacheKeyPrefix {

    public static final String SEPARATOR = "_";
    static final String GROUP_SEPARATOR = ":";

    /**
     * shop维度 推荐商品list
     */
    public static final String RECOMMENDED_ITEMIDS_OF_SHOP = "RECOMMENDED_ITEMIDS_OF_SHOP:";

    /**
     * shop维度 某类型圈定的的商品范围
     */
    public static final String TYPE_ITEMIDS_OF_SHOP = "TYPE_ITEMIDS_OF_SHOP:";

    /**
     * shop维度 热销商品list
     */
    public static final String HOTITEM_ITEMIDS_OF_SHOP = "HOTITEM_ITEMIDS_OF_SHOP:";

    /**
     * 店铺+用户维度，购物车商品信息
     */
    public static final String CART_ITEMS_OF_SHOP_AND_USER = "CART_ITEMS_OF_SHOP_AND_USER:";
    public static final String DEFAULT_DELIVER_OF_SHOP_AND_USER = "DEFAULT_DELIVER_OF_SHOP_AND_USER:";

    /**
     * 店铺+活动维度商品集合
     */
    public static final String ITEM_SET_OF_SHOP_AND_ACTIVITY = "ITEM_SET_OF_SHOP_AND_ACTIVITY";
    public static final String ITEM_SET_OF_SHOP_AND_COUPONS = "ITEM_SET_OF_SHOP_AND_COUPONS";

    /**
     * 微信accessToken，对应redis hash模型
     */
    public static final String WEIXIN_ACCESSTOKEN = "WEIXIN_ACCESSTOKEN";

    /**
     * 排它锁
     */
    public static final String NX_LOCK = "NX_LOCK:";

    public static final String MENUS = "MENUS";
    public static final String SUBMIT_URL = "SUBMIT_URL";

    /**
     * dashboard页面的数据cache
     */
    public static final String DASHBOARD_SALE_ORDER_SUMMARY_OF_SHOP = "DASHBOARD_SALE_ORDER_SUMMARY_OF_SHOP";
    public static final String DASHBOARD_MEM_SUMMARY_OF_SHOP = "DASHBOARD_MEM_SUMMARY_OF_SHOP";
    public static final String DASHBOARD_ITEM_SUMMARY_OF_SHOP = "DASHBOARD_ITEM_SUMMARY_OF_SHOP";
    public static final String DASHBOARD_HOT_ITEMS_OF_SHOP = "DASHBOARD_HOT_ITEMS_OF_SHOP";
    public static final String DASHBOARD_NEW_MEMS_OF_SHOP = "DASHBOARD_NEW_MEMS_OF_SHOP";
    public static final String DASHBOARD_NEW_ORDERS_OF_SHOP = "DASHBOARD_NEW_ORDERS_OF_SHOP";

    /**
     * shop维度 消息list
     */
    public static final String INFO_LIST_OF_SHOP = "INFO_LIST_OF_SHOP:";

    /**
     * shop维度，最近消息的hash，避免重复
     */
    public static final String INFO_HASH_OF_SHOP = "INFO_HASH_OF_SHOP:";

    /**
     * 消息已读 bitmap
     */
    public static final String INFO_SEE_OF_SHOP = "INFO_SEE_OF_SHOP";

    /**
     * 全局消息ID cache
     */
    public static final String INFO_ID = "INFO_ID";

    /**
     * 限流
     */
    public static final String ACCESS_LIMIT_OF_SHOP = "ACCESS_LIMIT_OF_SHOP:";

    /**
     * shop item sku 维度 库存cache
     */
    public static final String INVENTORY_OF_SHOP = "INVENTORY_OF_SHOP:";

    /**
     * 售罄商品集合
     */
    public static final String SELLOUT_OF_SHOP = "SELLOUT_OF_SHOP:";

    /**
     * 困存紧张的商品集合
     */
    public static final String LOW_INVENTORY_OF_SHOP = "LOW_INVENTORY_OF_SHOP:";

    /**
     * shop item sku 维度 销量cache
     */
    public static final String SALE_OF_SHOP = "SALE_OF_SHOP:";

    /**
     * shop item sku 维度 购买者数量cache
     */
    public static final String USER_COUNT_OF_SHOP = "USER_COUNT_OF_SHOP:";

    /**
     * shop item 维度 买家ID list
     */
    public static final String LAST_BUYERIDS_OF_SHOP = "LAST_BUYERIDS_OF_SHOP:";
    public static final String LAST_BUYERIMG_OF_SHOP = "LAST_BUYERIMG_OF_SHOP:";

    public static final String CATE_OF_SHOP = "CATE_OF_SHOP:";

    public static final String ITEM_SET_THAT_HAS_SKU_OF_SHOP = "ITEM_SET_THAT_HAS_SKU_OF_SHOP:";

    public static final String STATUS_COUNT_OF_ORDER_OF_SHOP_USER = "STATUS_COUNT_OF_ORDER_OF_SHOP_USER:";

    public static final String ORDER_DETAIL_OF_ORDER_OF_SHOP_USER_ORDER = "ORDER_DETAIL_OF_ORDER_OF_SHOP_USER_ORDER:";

    public static final String ORDER_LIST_OF_ORDER_OF_SHOP_USER_STATUS = "ORDER_LIST_OF_ORDER_OF_SHOP_USER_STATUS:";

    public static final String ORDER_TOTAL_OF_ORDER_OF_SHOP_USER_STATUS = "ORDER_TOTAL_OF_ORDER_OF_SHOP_USER_STATUS:";

    public static final String ITEM_DETAIL_OF_SHOP = "ITEM_DETAIL_OF_SHOP:";

    public static final String ITEM_LIST_OF_SHOP = "ITEM_LIST_OF_SHOP:";

    public static final String ITEM_TOTAL_OF_SHOP = "ITEM_TOTAL_OF_SHOP:";

    public static final String ACTIVITY_OF_SHOP = "ACTIVITY_OF_SHOP:";

    public static final String COUPONS_OF_SHOP = "COUPONS_OF_SHOP:";

    public static final String APP_CONFIG_OF_SHOP = "APP_CONFIG_OF_SHOP";

    public static final String WEIXIN_NOTIFY_PAY_EXIST_OF_OUT_TRADE_NO = "WEIXIN_NOTIFY_PAY_EXIST_OF_OUT_TRADE_NO";
    public static final String WEIXIN_NOTIFY_REFUND_EXIST_OF_REFUND_ID = "WEIXIN_NOTIFY_REFUND_EXIST_OF_REFUND_ID";

    public static final String APPID_MAP_SHOPID = "APPID_MAP_SHOPID";

    /**
     * 第三方平台 验证票据  对应string模型
     */
    public static final String THIRD_PLATFORM_VERIFY_TICKET = "THIRD_PLATFORM_VERIFY_TICKET";

    /**
     * 正在 快速创建小程序 的 shopId 和 申请时间ms
     */
    public static final String WX_MINIPROGRAME_FAST_REGISTING = "WX_MINIPROGRAME_FAST_REGISTING:";

    /**
     * 快速注册填写的信息  与  shop 关系
     */
    public static final String WX_MINIPROGRAME_FAST_REGISTING_INFOCODE = "WX_MINIPROGRAME_FAST_REGISTING_INFOCODE:";

    /**
     * 授权应用的基本信息  用于快速创建小程序
     */
    public static final String WEIXIN_ACCOUNT_BASE_INFO = "WEIXIN_ACCOUNT_BASE_INFO";

    /**
     * 授权者信息   用于将已有小程序授权给第三方
     */
    public static final String WEIXIN_ACCOUNT_AUTHORIZER_INFO = "WEIXIN_ACCOUNT_AUTHORIZER_INFO";

    /**
     * 授权应用的webview配置
     */
    public static final String WEIXIN_WEB_VIEW_DOMAIN = "WEIXIN_WEB_VIEW_DOMAIN";

    public static final String WEIXIN_DOMAIN = "WEIXIN_DOMAIN";

    /**
     * 上传资源md5-app素材
     */
    public static final String APP_IMG_MEDIA = "APP_IMG_MEDIA";

    /**
     * app nick 待审核
     */
    public static final String WEIXIN_ACCOUNT_NICK_NAME_AUDITING_SET = "WEIXIN_ACCOUNT_NICK_NAME_AUDITING_SET";

    /**
     * app nick 待审核
     */
    public static final String WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH = "WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH";

    /**
     * nick审核失败原因
     */
    public static final String WEIXIN_ACCOUNT_NICK_NAME_AUDITING_FAIL_RESULT = "WEIXIN_ACCOUNT_NICK_NAME_AUDITING_FAIL_RESULT";

    /**
     * app 当前 nick
     */
    public static final String WEIXIN_ACCOUNT_NICK_NAME = "WEIXIN_ACCOUNT_NICK_NAME";

    /**
     * 可以设置的所有类目
     */
    public static final String WEIXIN_APP_CATES = "WEIXIN_APP_CATES";

    /**
     * 已设置的所有类目
     */
    public static final String WEIXIN_APP_SETED_CATES = "WEIXIN_APP_SETED_CATES";

    /**
     * 上传代码
     */
    public static final String WEIXIN_CODE_COMMIT = "WEIXIN_CODE_COMMIT";

    /**
     * 提交的代码审核
     */
    public static final String WEIXIN_CODE_AUDIT = "WEIXIN_CODE_AUDIT";

    /**
     * 发布的代码
     */
    public static final String WEIXIN_CODE_RELEASE = "WEIXIN_CODE_RELEASE";

    /**
     * 体验者s
     */
    public static final String WEIXIN_TESTER = "WEIXIN_TESTER";

    /**
     * 体验者关系
     */
    public static final String WEIXIN_TESTER_WECHATID_USERID = "WEIXIN_TESTER_WECHATID_USERID";
    public static final String WEIXIN_TESTER_USERID_WECHATID = "WEIXIN_TESTER_USERID_WECHATID";

    /**
     * 微信第三方平台授权码 - shopId
     */
    public static final String WEIXIN_AUTH_PRECODE = "WEIXIN_AUTH_PRECODE";

    public static final String WX_MINIPROGRAME_FAST_REGISTING_FAIL = "WX_MINIPROGRAME_FAST_REGISTING_FAIL:";

    public static final String FORM_TOKEN_SHOP = "FORM_TOKEN_SHOP:";

    /**
     * 随机短字符串的重复次数，直接追加在重复串后方，得到不冲突的串
     */
    public static final String SHORT_STR_REPET_COUNT_HASH = "SHORT_STR_REPET_COUNT_HASH";

    /**
     * 短字符串 与 model 关系
     */
    public static final String SHORT_STR_HASH = "SHORT_STR_HASH";

    /**
     * 记录分享来源 用户+商品 : 分享者
     */
    public static final String SHARE_ITEM_RELATIONSHIP_HASH = "SHARE_ITEM_RELATIONSHIP_HASH";

    public static final String PRESELL_ITEM_SET_OF_SHOP = "PRESELL_ITEM_SET_OF_SHOP:";

    /**
     * 分享次数计数
     */
    public static final String SHARE_COUNT_HASH = "SHARE_COUNT_HASH";

    /**
     * 微导购 分享次数计数
     */
    public static final String WEISALES_SHARE_COUNT_HASH = "WEISALES_SHARE_COUNT_HASH";

    /**
     * 分享的引流情况，shop 分享者 维度
     */
    public static final String SHARE_PV_SHOP_USER_HASH = "SHARE_PV_SHOP_USER_HASH";

    /**
     * 分享的引流情况，shop 维度
     */
    public static final String SHARE_PV_SHOP_HASH = "SHARE_PV_SHOP_HASH";

    /**
     * 用户分享：获客
     */
    public static final String SHARE_SHOP_USER_SHARE_GAINUSER_SET = "SHARE_SHOP_USER_SHARE_GAINUSER_SET:";

    /**
     * 用户：分享集合
     */
    public static final String SHARE_SHOP_USER_SHARE_SET = "SHARE_SHOP_USER_SHARE_SET:";

    /**
     * 店铺 策略 对应的 itemId结合
     */
    public static final String SHOP_STRATEGY_ITEMS_SET = "SHOP_STRATEGY_ITEMS_SET:";

    /**
     * 店铺 处理位置  对应的  策略集合
     */
    public static final String SHOP_DEALPOINT_STRATEGYS_SET = "SHOP_DEALPOINT_STRATEGYS_SET:";

    /**
     * 店铺 商品 对应的 策略集合
     */
    public static final String SHOP_ITEM_STRATEGYS_SET = "SHOP_ITEM_STRATEGYS_SET:";

    /**
     * 店铺 策略内容
     */
    public static final String SHOP_STRATEGY_HASH = "SHOP_STRATEGY_HASH:";

    public static final String SHOP_PRESELL_ITEM_TOTAL_HASH = "SHOP_PRESELL_ITEM_TOTAL_HASH:";

    ///////////////////////////smart code////////////////////////////
    /**
     * user map smartcode
     */
    public static final String SMART_CODE_USER_MAP = "SMART_CODE_USER_MAP:";

    /**
     * user smartcode map ele
     */
    public static final String SMART_CODE_USER_ELE_MAP = "SMART_CODE_USER_ELE_MAP:";

    /**
     * smartcode map user
     */
    public static final String SMART_CODE_MAP_USER = "SMART_CODE_MAP_USER";

    /**
     * ele code map view count
     */
    public static final String SMART_CODE_ELE_COUNT_MAP = "SMART_CODE_ELE_COUNT_MAP";

    /**
     * smart code map good ele
     */
    public static final String SMART_CODE_GOOD_ELE = "SMART_CODE_GOOD_ELE:";

    /**
     * 活码增值服务 map user
     */
    public static final String SMART_CODE_POWER_MAP = "SMART_CODE_POWER_MAP";

    /**
     * 小程序代码发布  appId map 模板ID
     */
    public static final String WEIXIN_CODE_TEMPLATEID_APPID_MAP = "WEIXIN_CODE_TEMPLATEID_APPID_MAP";

    /**
     * 默认模板ID
     */
    public static final String WEIXIN_CODE_TEMPLATEID = "WEIXIN_CODE_TEMPLATEID";
}
