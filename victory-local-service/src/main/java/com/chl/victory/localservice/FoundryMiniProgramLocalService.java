package com.chl.victory.localservice;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.common.util.HttpClientUtil;
import com.chl.victory.localservice.model.UploadMediaResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ChenHailong
 * @date 2020/9/3 9:21
 **/
@Component
public class FoundryMiniProgramLocalService {
    @Resource
    CacheService cacheService;

    public String getNickNameCache(@NotNull Long shopId, @NotEmpty String appId) {
        String key = CacheKeyPrefix.WEIXIN_ACCOUNT_NICK_NAME;
        String hGet = cacheService.hGet(key, appId, String.class);
        return hGet;
    }

    /**
     * @param shopId
     * @param appId
     * @return  ReleaseResult
     */
    public String getReleaseCache(@NotNull Long shopId, @NotEmpty String appId) {
        String key = CacheKeyPrefix.WEIXIN_CODE_RELEASE;
        String field = appId;
        String releaseResult = cacheService.hGet(key, field, String.class);
        return releaseResult;
    }

    /**
     * @param shopId
     * @param appId
     * @return AuditResult
     */
    public String getAuditCache(@NotNull Long shopId, @NotEmpty String appId) {
        String key = CacheKeyPrefix.WEIXIN_CODE_AUDIT;
        String field = appId;
        String auditResult = cacheService.hGet(key, field, String.class);
        return auditResult;
    }

    /**
     * 上传代码 cache
     * @return CommitDTO
     */
    public String getCommitCache(@NotNull Long shopId, @NotEmpty String appId) {
        String key = CacheKeyPrefix.WEIXIN_CODE_COMMIT;
        String field = appId;
        String commitDTO = cacheService.hGet(key, field, String.class);
        return commitDTO;
    }

    /**
     * @param shopId
     * @param appId
     */
    public void delCommitCache(@NotNull Long shopId, @NotEmpty String appId) {
        String key = CacheKeyPrefix.WEIXIN_CODE_COMMIT;
        String field = appId;
        cacheService.hDel(key, field);
    }

    /**
     * 是否正在  快速注册  中
     * @param shopId
     * @return
     */
    public boolean isRegisting(Long shopId) {
        String key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING + CacheKeyPrefix.SEPARATOR + shopId;
        return cacheService.existsKey(key);
    }

    /**
     * 上传媒体文件
     * @param shopId
     * @param appId
     * @param url
     * @param file
     * @return
     * @throws Exception
     */
    public UploadMediaResult uploadTemporaryMedia(Long shopId, String appId, String url, MultipartFile file) throws Exception {
        String postImageToWeixin;
        try {
            postImageToWeixin = HttpClientUtil.postImageToWeixin(url, file);
        } catch (Exception e) {
            throw new Exception("uploadTemporaryMediaEx|" + appId, e);
        }

        UploadMediaResult uploadMediaResult = JSONObject.parseObject(postImageToWeixin, UploadMediaResult.class);

        if (!uploadMediaResult.isSuccess()) {
            throw new Exception(uploadMediaResult.getError());
        }

        return uploadMediaResult;
    }
}
