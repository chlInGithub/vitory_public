package com.chl.victory.service.services.weixin.thirdplatform;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.localservice.model.InfoDTO;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.ThirdPlatformServiceException;
import com.chl.victory.serviceapi.weixin.model.BaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.component.ComponentAccessToken;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.fastcreate.FastRegisterDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.infoService;

/**
 * 第三方平台 代小程序 快速创建小程序相关接口
 * @author ChenHailong
 * @date 2020/5/26 17:54
 **/
@Service
@ConfigurationProperties("weixin.thirdplatform.foundryminiprogram.fastcreate")
@Slf4j
public class FoundryMiniProgram4FastCreateService {

    @Setter
    String fastregisterweapp;

    /**
     * 快速创建小程序,具体创建结果需要等待微信的回告
     * @param model
     * @link https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/Mini_Programs/Fast_Registration_Interface_document.html
     */
    public void fastRegister(@NotNull Long shopId, @NotNull FastRegisterDTO model) {
        ComponentAccessToken componentAccessToken = ServiceManager.componentService.getComponentAccessToken();
        Map<String, String> vars = new HashMap<>();
        vars.put("component_access_token", componentAccessToken.getComponent_access_token());

        model.setComponent_phone("18500425785");
        String request = JSONObject.toJSONString(model);

        BaseResult post = ServiceManager.httpClientService.post(fastregisterweapp, request, BaseResult.class, vars);

        if (!post.isSuccess()) {
            throw new ThirdPlatformServiceException(post.getError());
        }

        addRegistingCache(shopId, model.hashCode());
    }

    /**
     * cache：店铺正在新建，注册信息对应的shopId
     * @param shopId
     * @param infoCode
     */
    public void addRegistingCache(Long shopId, Integer infoCode) {
        String key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING + CacheKeyPrefix.SEPARATOR + shopId;
        cacheService.save(key, System.currentTimeMillis() + "", CacheExpire.DAYS_1);

        key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING_FAIL + shopId;
        cacheService.delKey(key);

        key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING_INFOCODE + CacheKeyPrefix.SEPARATOR + infoCode;
        cacheService.save(key, shopId.toString(), CacheExpire.DAYS_1);
    }

    public void addRegistingFail(Long shopId, String msg) {
        String key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING_FAIL + shopId;
        cacheService.save(key, msg, CacheExpire.DAYS_1);

        InfoDTO infoDTO = new InfoDTO();
        infoDTO.setTitle("小程序授权失败");
        infoDTO.setContent("小程序授权失败");
        infoService.addInfo(infoDTO, shopId, false);
    }

    public String getRegistingFail(Long shopId) {
        String key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING_FAIL + shopId;
        String result = cacheService.get(key, String.class);
        return result;
    }

    /**
     * 通过注册信息的hashcode，查询shopId
     * @param hashCode @link{@see FastRegisterDTO}
     * @return
     */
    public Long getFastRegistingShopId(Integer hashCode) {
        String key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING_INFOCODE + CacheKeyPrefix.SEPARATOR + hashCode;
        Long shopId = cacheService.get(key, Long.class);
        return shopId;
    }

    public void rmRegistingCache(Long shopId) {
        String key = CacheKeyPrefix.WX_MINIPROGRAME_FAST_REGISTING + CacheKeyPrefix.SEPARATOR + shopId;
        cacheService.delKey(key);
    }
}
