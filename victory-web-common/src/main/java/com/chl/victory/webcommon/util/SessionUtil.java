package com.chl.victory.webcommon.util;

import com.chl.victory.webcommon.model.SessionCache;

/**
 * 从ThreadLocal获取/清除session信息
 *
 * @author ChenHailong
 * @date 2019/4/23 14:52
 **/
public class SessionUtil {
    private static final ThreadLocal<SessionCache> LOCAL = new ThreadLocal<>();

    public static SessionCache getSessionCache(){
        return LOCAL.get();
    }

    public static void setSessionCache(SessionCache sessionCache){
        LOCAL.set(sessionCache);
    }

    public static void clear(){
        LOCAL.remove();
    }
}
