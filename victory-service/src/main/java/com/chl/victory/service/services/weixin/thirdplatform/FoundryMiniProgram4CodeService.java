package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.common.enums.ThirdPlatformEnum;
import com.chl.victory.dao.model.merchant.ShopDO;
import com.chl.victory.dao.model.merchant.StyleConfig;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.localservice.model.InfoDTO;
import com.chl.victory.serviceapi.weixin.model.BaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.AuditResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.CommitDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.ReleaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.SubmitAuditDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.chl.victory.localservice.manager.LocalServiceManager.foundryMiniProgramLocalService;
import static com.chl.victory.localservice.manager.LocalServiceManager.templateIdLocalService;
import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.componentService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;
import static com.chl.victory.service.services.ServiceManager.infoService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * 第三方平台 代小程序 类目相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.foundryminiprogram.code")
@Slf4j
public class FoundryMiniProgram4CodeService extends FoundryMiniProgramBaseService {

    @Setter
    @Getter
    String defaultTemplateId;

    @Setter
    String commit;

    @Setter
    String get_qrcode;

    @Setter
    String submit_audit;

    @Setter
    String get_latest_auditstatus;

    @Setter
    String undocodeaudit;

    @Setter
    String release;

    @Setter
    String revertcoderelease;

    /**
     * 将小程序的线上版本进行回退
     * @param weixinConfig
     * @return
     */
    public void revertcoderelease(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        BaseResult post = httpClientService
                .post(revertcoderelease, null, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }
    }

    /**
     * 查询最新一次提交的审核状态
     * @param weixinConfig
     * @return
     * @throws BusServiceException
     */
    public AuditResult getLatestAuditstatus(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("access_token",
                thirdPlatformAuthorizerAccessTokenService.getAccessToken(weixinConfig).getAuthorizer_access_token());
        AuditResult post = httpClientService
                .get(get_latest_auditstatus, vars, AuditResult.class);

        return post;
    }

    /**
     * 发布最后一个审核通过的小程序代码版本
     * @param weixinConfig
     * @return
     */
    public ReleaseResult release(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        AuditResult auditCache = getAuditCache(weixinConfig);

        if (null == auditCache) {
            throw new ThirdPlatformServiceException("不存在提交审核的代码");
        }

        if (!auditCache.isAuditOk()) {
            throw new ThirdPlatformServiceException("不存在审核成功的代码");
        }

        BaseResult post = httpClientService.post(release, "{}", BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        ReleaseResult releaseResult = new ReleaseResult();
        releaseResult.setVersion_desc(auditCache.getVersion_desc());
        releaseResult.setTime(DateFormatUtils.format(new Date(), DateConstants.format1));
        setReleaseCache(weixinConfig, releaseResult);
        foundryMiniProgramLocalService.delCommitCache(weixinConfig.getShopId(), weixinConfig.getAppId());
        delAuditCache(weixinConfig);

        return releaseResult;
    }

    private void setReleaseCache(ShopAppDO weixinConfig, ReleaseResult releaseResult) {
        String key = CacheKeyPrefix.WEIXIN_CODE_RELEASE;
        String field = weixinConfig.getAppId();
        cacheService.hSet(key, field, releaseResult);
    }

    /**
     * 小程序审核撤回
     * @param weixinConfig
     * @return
     */
    public void undocodeaudit(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("access_token",
                thirdPlatformAuthorizerAccessTokenService.getAccessToken(weixinConfig).getAuthorizer_access_token());
        BaseResult post = httpClientService.get(undocodeaudit, vars, BaseResult.class);

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        delAuditCache(weixinConfig);
    }

    /**
     * 获取体验版二维码 byte[], 计划直接传给页面，由页面处理
     * @param weixinConfig
     * @return
     */
    public byte[] getQRCode4Test(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("access_token",
                thirdPlatformAuthorizerAccessTokenService.getAccessToken(weixinConfig).getAuthorizer_access_token());
        byte[] post = httpClientService.get(get_qrcode, vars, byte[].class);
        return post;
    }

    /**
     * 提交审核
     * @param weixinConfig
     * @param submitAuditDTO
     */
    public AuditResult submitAudit(@NotNull ShopAppDO weixinConfig, @NotNull SubmitAuditDTO submitAuditDTO)
            throws BusServiceException {

        AuditResult latestAuditstatus = getLatestAuditstatus(weixinConfig);
        if (latestAuditstatus.getStatus() != null && latestAuditstatus.getStatus().equals(2)){
            throw new ThirdPlatformServiceException("存在正在审核中的版本");
        }

        String request = JSONObject.toJSONString(submitAuditDTO);
        AuditResult post = httpClientService
                .post(submit_audit, request, AuditResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        post.setVersion_desc(submitAuditDTO.getVersion_desc());
        post.setStatus(2);
        post.setTime(DateFormatUtils.format(new Date(), DateConstants.format1));
        setAuditCache(weixinConfig, post);

        return post;
    }

    /**
     * 提交代码
     * @param weixinConfig
     * @param commitDTO
     */
    public void commit(@NotNull ShopAppDO weixinConfig, @NotNull CommitDTO commitDTO) throws BusServiceException {
        ShopDO shopDO = merchantService.selectShop(weixinConfig.getShopId());
        CommitDTO.ExtJson wxMiniExtJson = genExtJson(weixinConfig, shopDO);
        String extJson = JSONObject.toJSONString(wxMiniExtJson);
        commitDTO.setExt_json(extJson);

        if (StringUtils.isBlank(commitDTO.getTemplate_id())) {
            String templateId = templateIdLocalService.getTemplateId(weixinConfig.getAppId());
            if (StringUtils.isBlank(templateId)) {
                throw new ThirdPlatformServiceException("缺少模板ID");
            }
            commitDTO.setTemplate_id(templateId);
        }

        String request = JSONObject.toJSONString(commitDTO);
        BaseResult post = httpClientService.post(commit, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        commitDTO.setTime(DateFormatUtils.format(new Date(), DateConstants.format1));
        setCommitCache(weixinConfig, commitDTO);
    }

    private void setCommitCache(ShopAppDO weixinConfig, CommitDTO commitDTO) {
        String key = CacheKeyPrefix.WEIXIN_CODE_COMMIT;
        String field = weixinConfig.getAppId();
        cacheService.hSet(key, field, commitDTO);
    }

    public CommitDTO.ExtJson genExtJson(ShopAppDO shopAppDO, ShopDO shopDO) {
        CommitDTO.ExtJson extJson = new CommitDTO.ExtJson();
        extJson.setExtAppid(shopAppDO.getAppId());
        CommitDTO.ExtJson.Ext ext = new CommitDTO.ExtJson.Ext();
        extJson.setExt(ext);
        ext.setAppId(shopAppDO.getAppId());
        ext.setShopId(shopAppDO.getShopId().toString());
        ext.setShopImg(shopDO.getImg());
        ext.setTId(ThirdPlatformEnum.weixin.getCode());
        ext.setDomain(shopAppDO.getDomain());
        ext.setShopName(shopDO.getName());
        ext.setRequestDomain(componentService.getComponentConfig().getRequestDomain());

        try {
            String selectStyle = merchantService.selectStyle(shopAppDO.getAppId(), shopAppDO.getShopId());
            StyleConfig styleConfig = StyleConfig.parse(selectStyle);
            ext.setBgColor(styleConfig.getBgColor());
            ext.setNgbgColor(styleConfig.getNavBarBackColor());
            ext.setNgFrontColor(styleConfig.getNavBarFontColor());
        } catch (BusServiceException e) {
        }
        return extJson;
    }

    void setAuditCache(ShopAppDO shopAppDO, AuditResult auditResult) {
        String key = CacheKeyPrefix.WEIXIN_CODE_AUDIT;
        String field = shopAppDO.getAppId();
        cacheService.hSet(key, field, auditResult);
    }

    /**
     * 更新代码审核的状态和原因
     * @param shopAppDO
     * @param status
     * @param reason
     */
    public void modifyAuditCache(ShopAppDO shopAppDO, Integer status, String reason) {
        AuditResult auditCache = getAuditCache(shopAppDO);
        if (null != auditCache) {
            auditCache.setStatus(status);
            auditCache.setReason(reason);
            if (status != 2 && status != 0) {
                delAuditCache(shopAppDO);
            }else {
                setAuditCache(shopAppDO, auditCache);
            }

            InfoDTO infoDTO = new InfoDTO();
            infoDTO.setTitle("代码审核有新进展");
            infoDTO.setContent("appId:" + shopAppDO.getAppId() + ",代码审核进展:" + auditCache.getStatusDesc() + (StringUtils
                    .isNotBlank(reason) ? (",微信返回的原因:" + reason) : ""));
            infoService.addInfo(infoDTO, shopAppDO.getShopId(), true);
        }
    }

    void delAuditCache(ShopAppDO shopAppDO) {
        String key = CacheKeyPrefix.WEIXIN_CODE_AUDIT;
        String field = shopAppDO.getAppId();
        cacheService.hDel(key, field);
    }

    public AuditResult getAuditCache(ShopAppDO shopAppDO) {
        String key = CacheKeyPrefix.WEIXIN_CODE_AUDIT;
        String field = shopAppDO.getAppId();
        AuditResult auditResult = cacheService.hGet(key, field, AuditResult.class);
        return auditResult;
    }

    public void checkAuditResult() {
        String key = CacheKeyPrefix.WEIXIN_CODE_AUDIT;
        Set<String> fields = cacheService.hFields(key);

        if (CollectionUtils.isEmpty(fields)) {
            return;
        }

        fields.forEach(item -> checkAuditResult(item));
    }

    private void checkAuditResult(String appId) {
        try {
            Long shopId = merchantService.getShopId(appId);
            ShopAppDO shopAppDO = merchantService.selectShopApp(shopId, appId);
            AuditResult latestAuditstatus = getLatestAuditstatus(shopAppDO);
            if (!latestAuditstatus.getStatus().equals(2)) {
                modifyAuditCache(shopAppDO, latestAuditstatus.getStatus(), latestAuditstatus.getReason());
            }
        } catch (BusServiceException e) {
            log.error("checkAuditResult|{}", appId, e);
        }
    }

}

