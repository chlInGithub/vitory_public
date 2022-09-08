package com.chl.victory.common.redis;

/**
 * 缓存超时常量
 * @author ChenHailong
 * @date 2020/4/9 11:53
 **/
public class CacheExpire {

    /**
     * dashboard 页面数据 超时时间
     */
    public static final Integer DASHBOARD_EXPIRE = 60;

    /**
     * sessionId维度访问限流计数 超时时间
     */
    public static final Integer SESSION_ACCESS_LIMIT_EXPIRE = 10;

    /**
     * 1 day
     */
    public static final Integer DAYS_1 = 60 * 60 * 24;
    public static final Integer HOUR_1 = 60 * 60;

    /**
     * 30 days
     */
    public static final Integer DAYS_30 = DAYS_1 * 30;

    public static final Integer MINUTE_1 = 60;

    public static final Integer MINUTE_10 = MINUTE_1 * 10;

    public static final Integer SECONDS_30 = 30;

    public static final Integer SECONDS_10 = 10;
}
