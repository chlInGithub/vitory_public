package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.weixin.model.BaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.AddCategoryDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.GetAllCategoriesResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.GetCategoriesResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.GetCategoryCanUsedToAuditResult;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.authorizerService;
import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.httpClientService;

/**
 * 第三方平台 代小程序 类目相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.foundryminiprogram.category")
@Slf4j
public class FoundryMiniProgram4CategoryService extends FoundryMiniProgramBaseService {

    @Setter
    String getallcategories;

    @Setter
    String getcategory;

    @Setter
    String addcategory;

    @Setter
    String deletecategory;

    @Setter
    String modifycategory;

    @Setter
    String getCategoryCanUsedToAudit;

    /**
     * 获取已设置的二级类目及用于代码审核的可选三级类目。
     * @param weixinConfig
     * @return
     */
    public GetCategoryCanUsedToAuditResult getCategoryCanUsedToAudit(@NotNull ShopAppDO weixinConfig)
            throws BusServiceException {
        GetCategoryCanUsedToAuditResult post = httpClientService
                .post(getCategoryCanUsedToAudit, null, GetCategoryCanUsedToAuditResult.class,
                        getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        return post;
    }

    /**
     * 添加类目资质
     * @param weixinConfig
     * @return
     */
    public void modifyCategory(@NotNull ShopAppDO weixinConfig, @NotNull AddCategoryDTO addCategoryDTO)
            throws BusServiceException {
        if (ArrayUtils.isEmpty(addCategoryDTO.getCerticates())) {
            throw new BusServiceException("必须传递资质材料素材");
        }
        fillMediaId(weixinConfig, addCategoryDTO);

        String request = JSONObject.toJSONString(addCategoryDTO);

        BaseResult post = httpClientService
                .post(modifycategory, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }
    }

    /**
     * 删除类目
     * @param weixinConfig
     * @param first 一级类目 ID
     * @param second 二级类目 ID
     * @return
     */
    public void deletecategory(@NotNull ShopAppDO weixinConfig, @NotNull Integer first, @NotNull Integer second)
            throws BusServiceException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("first", first);
        vars.put("second", second);
        String request = JSONObject.toJSONString(vars);

        BaseResult post = httpClientService
                .post(deletecategory, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }
    }

    /**
     * 添加类目
     * @param weixinConfig
     * @return
     */
    /*public void addCategory(@NotNull ShopAppDO weixinConfig, @NotNull AddCategoryDTO[] addCategoryDTOs) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("categories", addCategoryDTOs);
        String request = JSONObject.toJSONString(vars);

        BaseResult post = httpClientService
                .post(addcategory, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.toString());
        }
    }*/

    /**
     * 获取已设置的所有类目
     * @param weixinConfig
     * @return
     */
    public GetCategoriesResult getCategory(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        String key = CacheKeyPrefix.WEIXIN_APP_SETED_CATES;
        String field = weixinConfig.getAppId();
        GetCategoriesResult getCategoriesResult = cacheService.hGet(key, field, GetCategoriesResult.class);
        if (null == getCategoriesResult) {
            GetCategoriesResult post = httpClientService
                    .post(getcategory, null, GetCategoriesResult.class, getAccessTokenVars(weixinConfig));

            if (!post.isSuccess()) {
                throw new ThirdPlatformServiceException(post.getError());
            }

            cacheService.hSet(key, field, post, CacheExpire.DAYS_1);
            getCategoriesResult = post;
        }

        return getCategoriesResult;
    }

    /**
     * 获取可以设置的所有类目
     * @param weixinConfig
     * @return
     */
    public String getAllCategories(@NotNull ShopAppDO weixinConfig) throws BusServiceException {
        String key = CacheKeyPrefix.WEIXIN_APP_CATES;
        String categories = cacheService.hGet(key, weixinConfig.getAppId(), String.class);
        if (StringUtils.isBlank(categories)) {
            GetAllCategoriesResult post = httpClientService
                    .post(getallcategories, null, GetAllCategoriesResult.class, getAccessTokenVars(weixinConfig));

            if (!post.isSuccess()) {
                throw new ThirdPlatformServiceException(post.getError());
            }

            if (null == post.getCategories_list() || ArrayUtils.isEmpty(post.getCategories_list().getCategories())) {
                throw new ThirdPlatformServiceException("没有获取到所有类目|" + weixinConfig.getAppId());
            }

            categories = JSONObject.toJSONString(post.getCategories_list().getCategories());
            cacheService.hSet(key, weixinConfig.getAppId(), categories, CacheExpire.DAYS_1);
        }

        return categories;
    }

    @Data
    public static class AddCategoryTemp{
        List<AddCategoryDTO> categories;
    }

    public void addCategory(@NotNull ShopAppDO weixinConfig, @NotNull AddCategoryDTO addCategoryDTO) throws BusServiceException {
        if (!ArrayUtils.isEmpty(addCategoryDTO.getCerticates())) {
            fillMediaId(weixinConfig, addCategoryDTO);
        }else {
            addCategoryDTO.setCerticates(new AddCategoryDTO.Certicate[0]);
        }

        AddCategoryTemp addCategoryTemp = new AddCategoryTemp();
        addCategoryTemp.setCategories(Arrays.asList(addCategoryDTO));

        String request = JSONObject.toJSONString(addCategoryTemp);

        BaseResult post = httpClientService
                .post(addcategory, request, BaseResult.class, getAccessTokenVars(weixinConfig));

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }
    }

    private void fillMediaId(ShopAppDO weixinConfig, AddCategoryDTO addCategoryDTO) throws BusServiceException {
        for (AddCategoryDTO.Certicate certicate : addCategoryDTO.getCerticates()) {
            String mediaCache = authorizerService.getMediaCache(weixinConfig.getShopId(), weixinConfig.getAppId(), certicate.getValue());
            if (StringUtils.isBlank(mediaCache)) {
                throw new BusServiceException("没有找到素材");
            }
            certicate.setValue(mediaCache);
        }
    }
}

