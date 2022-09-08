package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.weixin.model.BaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.tester.BindTesterResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.tester.GetTesterResult;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;

/**
 * 第三方平台 代小程序 类目相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.foundryminiprogram.tester")
@Slf4j
public class FoundryMiniProgram4TesterService extends FoundryMiniProgramBaseService {

    @Setter
    String bind_tester;

    @Setter
    String memberauth;

    @Setter
    String unbind_tester;

    /**
     * @param weixinConfig
     * @param userstr 人员对应的唯一字符串
     * @return
     */
    public void unbindTester(@NotNull ShopAppDO weixinConfig, @NotEmpty String userstr) throws BusServiceException {
        GetTesterResult.Tester tester = getTesterCacheByUserId(weixinConfig, userstr);
        if (null == tester) {
            return;
        }

        Map<String, String> vars = new HashMap<>();
        vars.put("userstr", userstr);
        String request = JSONObject.toJSONString(vars);

        BaseResult post = httpClientService
                .post(unbind_tester, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        delTesterCache(weixinConfig, tester);
    }

    /**
     * 获取小程序所有已绑定的体验者列表
     * @param weixinConfig
     * @return
     */
    public GetTesterResult memberauth(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        String key = CacheKeyPrefix.WEIXIN_TESTER;
        String field = weixinConfig.getAppId();
        GetTesterResult result = cacheService.hGet(key, field, GetTesterResult.class);
        if (null == result) {
            Map<String, String> vars = new HashMap<>();
            vars.put("action", "get_experiencer");
            String request = JSONObject.toJSONString(vars);

            GetTesterResult post = httpClientService
                    .post(memberauth, request, GetTesterResult.class, getAccessTokenVars(weixinConfig));

            if (!post.isSuccess()) {
                throw new ThirdPlatformServiceException(post.getError());
            }

            if (ArrayUtils.isNotEmpty(post.getMembers())) {
                for (GetTesterResult.Tester tester : post.getMembers()) {
                    GetTesterResult.Tester testerCacheByUserId = getTesterCacheByUserId(weixinConfig,
                            tester.getUserstr());
                    if (testerCacheByUserId != null) {
                        tester.setWechatId(testerCacheByUserId.getWechatId());
                    }
                }
                cacheService.hSet(key, field, post);
            }
            else {
                post.setMembers(new GetTesterResult.Tester[ 0 ]);
            }
            result = post;
        }

        return result;
    }

    void delMemberauthCache(ShopAppDO shopAppDO) {
        String key = CacheKeyPrefix.WEIXIN_TESTER;
        String field = shopAppDO.getAppId();
        cacheService.hDel(key, field);
    }

    /**
     * @param weixinConfig
     * @param wechatid 微信号
     * @return 人员对应的唯一字符串
     */
    public GetTesterResult.Tester bindTester(@NotNull ShopAppDO weixinConfig, @NotEmpty String wechatid)
            throws BusServiceException {
        GetTesterResult.Tester tester = getTesterCacheByWechatId(weixinConfig, wechatid);
        if (null != tester) {
            return tester;
        }

        Map<String, String> vars = new HashMap<>();
        vars.put("wechatid", wechatid);
        String request = JSONObject.toJSONString(vars);

        BindTesterResult post = httpClientService
                .post(bind_tester, request, BindTesterResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        tester = new GetTesterResult.Tester();
        tester.setUserstr(post.getUserstr());
        tester.setWechatId(wechatid);
        setTesterCache(weixinConfig, tester);

        return tester;
    }

    private void setTesterCache(ShopAppDO weixinConfig, GetTesterResult.Tester tester) {
        String key = CacheKeyPrefix.WEIXIN_TESTER_WECHATID_USERID;
        String field = weixinConfig.getAppId() + CacheKeyPrefix.SEPARATOR + tester.getWechatId();
        cacheService.hSet(key, field, tester);

        key = CacheKeyPrefix.WEIXIN_TESTER_USERID_WECHATID;
        field = weixinConfig.getAppId() + CacheKeyPrefix.SEPARATOR + tester.getUserstr();
        cacheService.hSet(key, field, tester);

        key = CacheKeyPrefix.WEIXIN_TESTER;
        field = weixinConfig.getAppId();
        cacheService.hDel(key, field);
    }

    private GetTesterResult.Tester getTesterCacheByWechatId(ShopAppDO weixinConfig, String wechatId) {
        String key = CacheKeyPrefix.WEIXIN_TESTER_WECHATID_USERID;
        String field = weixinConfig.getAppId() + CacheKeyPrefix.SEPARATOR + wechatId;
        GetTesterResult.Tester tester = cacheService.hGet(key, field, GetTesterResult.Tester.class);
        return tester;
    }

    public GetTesterResult.Tester getTesterCacheByUserId(ShopAppDO weixinConfig, String userId) {
        String key = CacheKeyPrefix.WEIXIN_TESTER_USERID_WECHATID;
        String field = weixinConfig.getAppId() + CacheKeyPrefix.SEPARATOR + userId;
        GetTesterResult.Tester tester = cacheService.hGet(key, field, GetTesterResult.Tester.class);
        return tester;
    }

    public void delTesterCache(ShopAppDO weixinConfig, GetTesterResult.Tester tester) {
        String key = CacheKeyPrefix.WEIXIN_TESTER_WECHATID_USERID;
        String field = weixinConfig.getAppId() + CacheKeyPrefix.SEPARATOR + tester.getWechatId();
        cacheService.hDel(key, field);

        key = CacheKeyPrefix.WEIXIN_TESTER_USERID_WECHATID;
        field = weixinConfig.getAppId() + CacheKeyPrefix.SEPARATOR + tester.getUserstr();
        cacheService.hDel(key, field);

        key = CacheKeyPrefix.WEIXIN_TESTER;
        field = weixinConfig.getAppId();
        cacheService.hDel(key, field);
    }
}
