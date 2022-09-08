package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.localservice.model.InfoDTO;
import com.chl.victory.serviceapi.weixin.model.BaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.AccountBasicInfoDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.DomainInfo;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.SearchStatusResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.SetNickNameDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.SetNickNameResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.WebViewDomainInfo;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.chl.victory.service.services.ServiceManager.authorizerService;
import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;
import static com.chl.victory.service.services.ServiceManager.infoService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * 第三方平台 代小程序 基础信息相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.foundryminiprogram.baseinfo")
@Slf4j
public class FoundryMiniProgram4BasicInfoService extends FoundryMiniProgramBaseService {

    @Setter
    String getaccountbasicinfo;

    @Setter
    String modifyDomain;

    @Setter
    String setwebviewdomain;

    @Setter
    String setnickname;

    @Setter
    String querynickname;

    @Setter
    String modifyheadimage;

    @Setter
    String modifysignature;

    @Setter
    String getwxasearchstatus;

    @Setter
    String changewxasearchstatus;

    /**
     * 隐私设置
     * @param weixinConfig
     * @param disabledSearch 1 表示不可搜索，0 表示可搜索
     */
    public void setSearchStatus(@NotNull ShopAppDO weixinConfig, boolean disabledSearch) throws BusServiceException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("status", disabledSearch ? 1 : 0);
        String request = JSONObject.toJSONString(vars);

        BaseResult post = httpClientService
                .post(changewxasearchstatus, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }
    }

    /**
     * 查询隐私设置
     * @param weixinConfig
     * @return 1 表示不可搜索，0 表示可搜索
     */
    public Integer getSearchStatus(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        SearchStatusResult post = httpClientService
                .post(getwxasearchstatus, null, SearchStatusResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        return post.getStatus();
    }

    /**
     * 设置功能介绍
     * @param weixinConfig
     * @param signature 功能介绍（简介）
     */
    public void modifysignature(@NotNull ShopAppDO weixinConfig, @NotNull String signature) throws BusServiceException {
        Map<String, String> vars = new HashMap<>();
        vars.put("signature", signature);
        String request = JSONObject.toJSONString(vars);

        BaseResult post = httpClientService
                .post(modifysignature, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        delAccountBasicInfoCache(weixinConfig);
    }

    /**
     * 设置头像
     * @param weixinConfig
     * @param mediaId 头像素材 media_id
     */
    public void modifyheadimage(@NotNull ShopAppDO weixinConfig, @NotNull String mediaId) throws BusServiceException {
        Map<String, String> vars = new HashMap<>();
        vars.put("head_img_media_id", mediaId);
        vars.put("x1", "0");
        vars.put("x2", "1");
        vars.put("y1", "0");
        vars.put("y2", "1");
        String request = JSONObject.toJSONString(vars);

        BaseResult post = httpClientService
                .post(modifyheadimage, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        delAccountBasicInfoCache(weixinConfig);
    }

    /**
     * 设置名称
     * @param weixinConfig
     * @param setNickNameDTO
     */
    public void setNickName(@NotNull ShopAppDO weixinConfig, @NotNull SetNickNameDTO setNickNameDTO)
            throws BusServiceException {
        String request = JSONObject.toJSONString(setNickNameDTO);

        SetNickNameResult post = httpClientService
                .post(setnickname, request, SetNickNameResult.class, getAccessTokenVars(weixinConfig));

        if (post.isSuccess()) {
            if (post.getAudit_id() != null) {
                SetNickNameResultTemp setNickNameResultTemp = new SetNickNameResultTemp();
                setNickNameResultTemp.setNickName(setNickNameDTO.getNick_name());
                setNickNameResultTemp.setAuditId(post.getAudit_id());
                setNickNameAuditingCache(weixinConfig, setNickNameResultTemp);
                throw new ThirdPlatformServiceException("名称正在审核中，请耐心等待.");
            }

            nickNameAuditSuccess(weixinConfig, setNickNameDTO.getNick_name());
        }
        if (StringUtils.isNotBlank(post.getWording())) {
            throw new ThirdPlatformServiceException(post.getWording());
        }

        if (post.getAudit_id() != null) {
            throw new ThirdPlatformServiceException("名称正在审核中，请耐心等待.");
        }

        throw new ThirdPlatformServiceException(post.getError());
    }

    @Data
    public static class NickNameQuery{
        Long audit_id;
    }
    @Data
    public static class NickNameQueryResult extends BaseResult{
        String nickname;

        /**
         * 审核状态，1：审核中，2：审核失败，3：审核成功
         */
        Integer audit_stat;
        String fail_reason;
    }

    /**
     * 查询最新一次提交昵称的审核状态
     * @param weixinConfig
     * @return
     * @throws BusServiceException
     */
    NickNameQueryResult getnicknameAuditStatus(@NotNull ShopAppDO weixinConfig, @NotEmpty Long auditID) throws BusServiceException {
        NickNameQuery nickNameQuery = new NickNameQuery();
        nickNameQuery.setAudit_id(auditID);
        String request = JSONObject.toJSONString(nickNameQuery);

        NickNameQueryResult post = httpClientService
                .post(querynickname, request, NickNameQueryResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        return post;
    }

    public void checkNickAudit(){
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH;
        Set<String> fields = cacheService.hFields(key);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        fields.forEach(item-> checkNickAudit(item));
    }

    private void checkNickAudit(String appId) {
        try {
            Long shopId = merchantService.getShopId(appId);
            ShopAppDO shopAppDO = merchantService.selectShopApp(shopId, appId);
            SetNickNameResultTemp cache = getNickNameAuditingCache(shopAppDO);
            if (null == cache || cache.getAuditId() == null) {
                return;
            }

            NickNameQueryResult nickNameQueryResult = getnicknameAuditStatus(shopAppDO, cache.getAuditId());
            if (nickNameQueryResult.getAudit_stat().equals(2)) {
                nickNameAuditFail(shopAppDO, nickNameQueryResult.fail_reason);
            }
            if (nickNameQueryResult.getAudit_stat().equals(3)) {
                nickNameAuditSuccess(shopAppDO, nickNameQueryResult.nickname);
            }
        } catch (BusServiceException e) {
            log.error("checkAuditResult|{}", appId, e);
        }
    }

    public void nickNameAuditFail(ShopAppDO shopAppDO, String reason) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_SET;
        cacheService.sRem(key, shopAppDO.getAppId());
        key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH;
        cacheService.hDel(key, shopAppDO.getAppId());

        key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_FAIL_RESULT;
        cacheService.hSet(key, shopAppDO.getAppId(), reason);

        InfoDTO infoDTO = new InfoDTO();
        infoDTO.setTitle("名称审核有新进展");
        infoDTO.setContent("appId:" + shopAppDO.getAppId() + ",名称审核失败," + (StringUtils.isNotBlank(reason) ?
                (",微信返回的原因:" + reason) :
                ""));
        infoService.addInfo(infoDTO, shopAppDO.getShopId(), true);
    }

    public void nickNameAuditSuccess(ShopAppDO shopAppDO, String audittedNickName) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_SET;
        cacheService.sRem(key, shopAppDO.getAppId());
        key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH;
        cacheService.hDel(key, shopAppDO.getAppId());

        setNickNameCache(shopAppDO, audittedNickName);
        delAccountBasicInfoCache(shopAppDO);

        InfoDTO infoDTO = new InfoDTO();
        infoDTO.setTitle("名称审核有新进展");
        infoDTO.setContent("appId:" + shopAppDO.getAppId() + ",名称审核通过。");
        infoService.addInfo(infoDTO, shopAppDO.getShopId(), true);
    }

    public void setNickNameCache(ShopAppDO weixinConfig, String nick_name) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME;
        cacheService.hSet(key, weixinConfig.getAppId(), nick_name);
    }

    @Data
    public static class SetNickNameResultTemp{
        String nickName;
        Long auditId;
    }

    private void setNickNameAuditingCache(ShopAppDO weixinConfig, SetNickNameResultTemp temp) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_SET;
        cacheService.sAdd(key, weixinConfig.getAppId());
        key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH;
        cacheService.hSet(key, weixinConfig.getAppId(), temp);
    }

    public SetNickNameResultTemp getNickNameAuditingCache(ShopAppDO weixinConfig) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_SET;
        boolean isMember = cacheService.sIsMember(key, weixinConfig.getAppId());
        if (isMember) {
            key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH;
            SetNickNameResultTemp hGet = cacheService.hGet(key, weixinConfig.getAppId(), SetNickNameResultTemp.class);
            return hGet;
        }
        return null;
    }

    private void delNickNameAuditingCache(ShopAppDO weixinConfig) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_SET;
        cacheService.sRem(key, weixinConfig.getAppId());
        key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME_AUDITING_HASH;
        cacheService.hDel(key, weixinConfig.getAppId());
    }

    /**
     * 获取基本信息 优先从cache
     * @param weixinConfig
     * @return
     */
    public AccountBasicInfoDTO getAccountBasicInfo(ShopAppDO weixinConfig, boolean tryOther)
            throws BusServiceException {

        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_BASE_INFO;
        AccountBasicInfoDTO accountBasicInfoDTO = cacheService
                .hGet(key, weixinConfig.getAppId(), AccountBasicInfoDTO.class);
        if (null == accountBasicInfoDTO) {
            AccountBasicInfoDTO post = httpClientService
                    .post(getaccountbasicinfo, null, AccountBasicInfoDTO.class, getAccessTokenVars(weixinConfig));
            if (post.isSuccess()) {
                cacheService.hSet(key, weixinConfig.getAppId(), post);
            }else {
                throw new ThirdPlatformServiceException(post.getError());
            }

            accountBasicInfoDTO = post;
        }

        if (!accountBasicInfoDTO.isSuccess() && tryOther) {
            accountBasicInfoDTO = authorizerService.getAuthorizerInfo4MiniPrograme(weixinConfig, false);
        }

        accountBasicInfoDTO.setNickName(LocalServiceManager.foundryMiniProgramLocalService
                .getNickNameCache(weixinConfig.getShopId(), weixinConfig.getAppId()));
        SetNickNameResultTemp nickNameAuditingCache = getNickNameAuditingCache(weixinConfig);
        if (null != nickNameAuditingCache) {
            accountBasicInfoDTO.setAuditingNickName(nickNameAuditingCache.nickName);
        }

        return accountBasicInfoDTO;
    }

    void delAccountBasicInfoCache(ShopAppDO weixinConfig) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_BASE_INFO;
        cacheService.hDel(key, weixinConfig.getAppId());
    }

    /**
     * 添加 服务器域名
     * @param type {@link ModifyDomainType}
     * @param weixinConfig
     * @param requestdomain
     * @param wsrequestdomain
     * @param uploaddomain
     * @param downloaddomain
     */
    public DomainInfo modifyDomain(@NotNull ModifyDomainType type, @NotNull ShopAppDO weixinConfig,
            String[] requestdomain, String[] wsrequestdomain, String[] uploaddomain, String[] downloaddomain)
            throws BusServiceException {
        Map<String, Object> vars = new HashedMap();
        vars.put("action", type.getDesc());
        if (null != requestdomain && requestdomain.length > 0) {
            vars.put("requestdomain", requestdomain);
        }
        if (null != wsrequestdomain && wsrequestdomain.length > 0) {
            vars.put("wsrequestdomain", wsrequestdomain);
        }
        if (null != uploaddomain && uploaddomain.length > 0) {
            vars.put("uploaddomain", uploaddomain);
        }
        if (null != downloaddomain && downloaddomain.length > 0) {
            vars.put("downloaddomain", downloaddomain);
        }
        String request = JSONObject.toJSONString(vars);

        DomainInfo post = httpClientService
                .post(modifyDomain, request, DomainInfo.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        delDomainCache(weixinConfig);

        return post;
    }

    private void delDomainCache(ShopAppDO weixinConfig) {
        String key = CacheKeyPrefix.WEIXIN_DOMAIN;
        cacheService.hDel(key, weixinConfig.getAppId());
    }

    /**
     * 获取 服务器域名
     * @param weixinConfig
     * @return
     */
    public DomainInfo getDomain(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        String key = CacheKeyPrefix.WEIXIN_DOMAIN;
        DomainInfo domainResult = cacheService.hGet(key, weixinConfig.getAppId(), DomainInfo.class);
        if (null == domainResult) {
            Map<String, Object> vars = new HashedMap();
            vars.put("action", "get");
            String request = JSONObject.toJSONString(vars);

            DomainInfo post = httpClientService
                    .post(modifyDomain, request, DomainInfo.class, getAccessTokenVars(weixinConfig));

            if (!post.isSuccess()) {
                throw new ThirdPlatformServiceException(post.getError());
            }

            domainResult = post;
            cacheService.hSet(key, weixinConfig.getAppId(), domainResult);
        }

        return domainResult;
    }

    /**
     * 添加 业务域名
     * @param type {@link ModifyDomainType}
     * @param weixinConfig
     */
    public WebViewDomainInfo setWebViewDomain(@NotNull ModifyDomainType type, @NotNull ShopAppDO weixinConfig,
            String[] webviewdomain) throws BusServiceException {
        Map<String, Object> vars = new HashedMap();
        vars.put("action", type.getDesc());
        vars.put("webviewdomain", webviewdomain);
        String request = JSONObject.toJSONString(vars);

        WebViewDomainInfo post = httpClientService
                .post(setwebviewdomain, request, WebViewDomainInfo.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        delWebViewDomainCache(weixinConfig);

        return post;
    }

    private void delWebViewDomainCache(ShopAppDO weixinConfig) {
        String key = CacheKeyPrefix.WEIXIN_WEB_VIEW_DOMAIN;
        cacheService.hDel(key, weixinConfig.getAppId());
    }

    /**
     * 获取业务域名 优先从cache取
     * @param weixinConfig
     * @return
     */
    public WebViewDomainInfo getWebViewDomain(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        String key = CacheKeyPrefix.WEIXIN_WEB_VIEW_DOMAIN;
        WebViewDomainInfo webViewDomainResult = cacheService
                .hGet(key, weixinConfig.getAppId(), WebViewDomainInfo.class);
        if (null == webViewDomainResult) {
            Map<String, Object> vars = new HashedMap();
            vars.put("action", "get");
            String request = JSONObject.toJSONString(vars);

            WebViewDomainInfo post = httpClientService
                    .post(setwebviewdomain, request, WebViewDomainInfo.class, getAccessTokenVars(weixinConfig));

            if (!post.isSuccess()) {
                throw new ThirdPlatformServiceException(post.getError());
            }

            webViewDomainResult = post;
            cacheService.hSet(key, weixinConfig.getAppId(), webViewDomainResult);
        }

        return webViewDomainResult;
    }

    public enum ModifyDomainType {
        add("add"), delete("delete"), set("set");

        private String desc;

        ModifyDomainType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

}
