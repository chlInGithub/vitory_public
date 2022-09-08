package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.util.HttpClientUtil;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.weixin.enums.WeixinMediaTypeEnum;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.AuthorizerAccessToken;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.AuthorizerInfo4MiniProgrameDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.UploadMediaResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.UploadedMediaDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.AccountBasicInfoDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4BasicInfoService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;
import static com.chl.victory.service.services.ServiceManager.merchantService;

/**
 * 第三方平台 授权者信息相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.authorizer")
@Slf4j
public class AuthorizerService {

    @Setter
    String apiGetAuthorizerInfo;

    @Setter
    String uploadTemporaryMedia;

    public String getUploadMediaUrl(@NotNull Long shopId, @NotEmpty String appId, WeixinMediaTypeEnum weixinMediaTypeEnum) throws BusServiceException {
        @NotNull ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, appId);
        AuthorizerAccessToken accessToken = ServiceManager.authorizerAccessTokenService.getAccessToken(shopAppDO);
        String url = uploadTemporaryMedia + "?access_token=" + accessToken.getAuthorizer_access_token() + "&type="
                + weixinMediaTypeEnum.getDesc();
        return url;
    }

    /**
     * 上传临时素材
     * @see com.chl.victory.localservice.FoundryMiniProgramLocalService#uploadTemporaryMedia(Long, String, String, MultipartFile)
     */
    UploadMediaResult uploadTemporaryMedia(ShopAppDO shopAppDO, MultipartFile file,
            WeixinMediaTypeEnum weixinMediaTypeEnum) throws BusServiceException {
        String url = getUploadMediaUrl(shopAppDO.getShopId(), shopAppDO.getAppId(), weixinMediaTypeEnum);
        String postImageToWeixin;
        try {
            postImageToWeixin = HttpClientUtil.postImageToWeixin(url, file);
        } catch (Exception e) {
            throw new BusServiceException("uploadTemporaryMediaEx|" + shopAppDO.getAppId(), e);
        }

        UploadMediaResult uploadMediaResult = JSONObject.parseObject(postImageToWeixin, UploadMediaResult.class);

        if (!uploadMediaResult.isSuccess()) {
            throw new BusServiceException(uploadMediaResult.getError());
        }

        return uploadMediaResult;
    }

    public void setMediaCache(@NotNull Long shopId, @NotEmpty String appId, @NotNull UploadedMediaDTO mediaDTO) {
        String key = CacheKeyPrefix.APP_IMG_MEDIA;
        String field = appId + CacheKeyPrefix.SEPARATOR + mediaDTO.getMd5();
        cacheService.hSet(key, field, mediaDTO.getMediaId());
    }

    /**
     * @param shopId
     * @param appId
     * @param resourceId 图片服务器的imgId
     * @return
     */
    public String getMediaCache(@NotNull Long shopId, @NotEmpty String appId, @NotEmpty String resourceId) {
        String key = CacheKeyPrefix.APP_IMG_MEDIA;
        String field = appId + CacheKeyPrefix.SEPARATOR + resourceId;
        String mediaId = cacheService.hGet(key, field, String.class);
        return mediaId;
    }

    /**
     * 小程序授权方的帐号基本信息
     * @param weixinConfig
     * @return
     */
    public AccountBasicInfoDTO getAuthorizerInfo4MiniPrograme(@NotNull ShopAppDO weixinConfig, boolean tryOther)
            throws BusServiceException {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_BASE_INFO;
        AccountBasicInfoDTO accountBasicInfoDTO = cacheService
                .hGet(key, weixinConfig.getAppId(), AccountBasicInfoDTO.class);
        if (accountBasicInfoDTO == null) {
            Map<String, String> vars = new HashMap<>();
            vars.put("component_appid", ServiceManager.componentService.getComponentConfig().getComponentAppId());
            vars.put("authorizer_appid", weixinConfig.getAppId());
            String request = JSONObject.toJSONString(vars);

            vars = new HashMap<>();
            vars.put("component_access_token",
                    ServiceManager.componentService.getComponentAccessToken().getComponent_access_token());

            AuthorizerInfo4MiniProgrameDTO post = httpClientService
                    .post(apiGetAuthorizerInfo, request, AuthorizerInfo4MiniProgrameDTO.class, vars);

            if (post.isSuccess()) {
                // 转换
                accountBasicInfoDTO = new AccountBasicInfoDTO();
                accountBasicInfoDTO.setAppid(weixinConfig.getAppId());
                accountBasicInfoDTO.setAccount_type(3);
                AccountBasicInfoDTO.SignatureInfo signatureInfo = new AccountBasicInfoDTO.SignatureInfo();
                signatureInfo.setSignature(post.getAuthorizer_info().getSignature());
                accountBasicInfoDTO.setSignature_info(signatureInfo);
                AccountBasicInfoDTO.WXVerifyInfo wxVerifyInfo = new AccountBasicInfoDTO.WXVerifyInfo();
                wxVerifyInfo.setQualification_verify(post.getAuthorizer_info().getVerify_type_info().getId() == 0);
                accountBasicInfoDTO.setWx_verify_info(wxVerifyInfo);
                accountBasicInfoDTO.setPrincipal_name(post.getAuthorizer_info().getPrincipal_name());
                AccountBasicInfoDTO.HeadImageInfo headImageInfo = new AccountBasicInfoDTO.HeadImageInfo();
                headImageInfo.setHead_image_url(post.getAuthorizer_info().getHead_img());
                accountBasicInfoDTO.setHead_image_info(headImageInfo);

                foundryMiniProgram4BasicInfoService
                        .setNickNameCache(weixinConfig, post.getAuthorizer_info().getNick_name());

                cacheService.hSet(key, weixinConfig.getAppId(), accountBasicInfoDTO);
            }else {
                String msg = post.getError();
                throw new ThirdPlatformServiceException(msg);
            }
        }

        if (!accountBasicInfoDTO.isSuccess() && tryOther) {
            accountBasicInfoDTO = foundryMiniProgram4BasicInfoService.getAccountBasicInfo(weixinConfig, false);
        }
        return accountBasicInfoDTO;
    }

    public void delAuthorizerInfo4MiniPrograme(@NotNull ShopAppDO shopAppDO) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_BASE_INFO;
        cacheService.hDel(key, shopAppDO.getAppId());
    }
}
