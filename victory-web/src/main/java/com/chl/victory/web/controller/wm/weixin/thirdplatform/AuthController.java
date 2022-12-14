package com.chl.victory.web.controller.wm.weixin.thirdplatform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.exception.AccessLimitException;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.merchant.model.PayConfigDTO;
import com.chl.victory.serviceapi.merchant.model.ShopAppDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.fastcreate.FastRegisterDTO;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.Result;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.chl.victory.localservice.manager.LocalServiceManager.foundryMiniProgramLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;
import static com.chl.victory.webcommon.manager.RpcManager.componentFacade;
import static com.chl.victory.webcommon.manager.RpcManager.foundryMiniProgram4BasicInfoFacade;
import static com.chl.victory.webcommon.manager.RpcManager.foundryMiniProgram4FastCreateFacade;
import static com.chl.victory.webcommon.manager.RpcManager.merchantFacade;

/**
 * ????????????????????????
 * @author ChenHailong
 * @date 2020/5/28 14:02
 **/
@Controller
@RequestMapping("/p/wm/weixin/thirdplatform/auth/")
public class AuthController {

    @Autowired
    HttpServletRequest httpServletRequest;

    @Resource
    SessionService sessionService;

    /**
     * ????????????????????????????????????????????????????????????????????????
     */
    @GetMapping(path = "queryAuthorizedApps", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryAuthorizedApps() throws BusServiceException {
        Long shopId = SessionUtil.getSessionCache().getShopId();

        // ??????????????????app
        ServiceResult<List<ShopAppDTO>> shopAppResult = merchantFacade.selectShopApps(shopId);
        if (!shopAppResult.getSuccess()){
            return Result.FAIL(shopAppResult.getMsg());
        }

        List<ShopAppDTO> shopAppDOS = shopAppResult.getData();

        // ?????????????????????????????????
        boolean registing = foundryMiniProgramLocalService.isRegisting(shopId);

        AuthorizedAppsVO authorizedAppsVO = new AuthorizedAppsVO();
        List<AuthorizedAppsVO.AuthorizedAppVO> authorizedAppVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(shopAppDOS)) {
            authorizedAppVOS = shopAppDOS.stream().map(AuthorizedAppsVO.AuthorizedAppVO::transfer)
                    .collect(Collectors.toList());
        }
        authorizedAppsVO.setApps(authorizedAppVOS);
        authorizedAppsVO.setRegisting(registing);

        return Result.SUCCESS(authorizedAppsVO);
    }

    /**
     * ??????????????????????????????
     */
    @PostMapping(path = "chooseApp", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result chooseApp(@RequestParam("appId") String appId) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        // ??????shop????????????appId
        merchantFacade.selectShopAppWithValidate(sessionCache.getShopId(), appId);

        sessionCache.setAppId(appId);
        String host = httpServletRequest.getHeader("Host");
        sessionService.setSession(host, sessionCache.getKey(), sessionCache);

        return Result.SUCCESS();
    }

    /**
     * ?????????????????????
     */
    @PostMapping(path = "fastRegister", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result fastRegister(@NotEmpty FastRegisterDTO fastRegisterDTO) throws BusServiceException {
        Long shopId = SessionUtil.getSessionCache().getShopId();

        ServiceResult<Integer> coutnResult = merchantFacade.countShopApp(shopId);
        if (!coutnResult.getSuccess() || coutnResult.getData() == null){
            return Result.FAIL(coutnResult.getMsg());
        }
        int result = coutnResult.getData();
        if (result > 0) {
            accessLimitFacade.doAccessLimit(shopId, result, AccessLimitTypeEnum.WM_SHOP_APP_TOTAL, AccessLimitTypeEnum.WM_SHOP_APP_TOTAL.getDesc());
        }

        boolean registing = foundryMiniProgramLocalService.isRegisting(shopId);
        if (!registing) {
            ServiceResult serviceResult = foundryMiniProgram4FastCreateFacade.fastRegister(shopId, fastRegisterDTO);
            if (!serviceResult.getSuccess()) {
                return Result.FAIL(serviceResult.getMsg());
            }
        }

        return Result.SUCCESS();
    }

    /**
     * ????????????url
     */
    @GetMapping(path = "authUrl", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result authUrl() throws AccessLimitException, BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long shopId = sessionCache.getShopId();

        ServiceResult<Integer> coutnResult = merchantFacade.countShopApp(shopId);
        if (!coutnResult.getSuccess() || coutnResult.getData() == null){
            return Result.FAIL(coutnResult.getMsg());
        }
        int result = coutnResult.getData();
        if (result > 0) {
            accessLimitFacade.doAccessLimit(shopId, result, AccessLimitTypeEnum.WM_SHOP_APP_TOTAL, AccessLimitTypeEnum.WM_SHOP_APP_TOTAL.getDesc());
        }

        String accessLimitKey =
                CacheKeyPrefix.ACCESS_LIMIT_OF_SHOP + shopId + CacheKeyPrefix.SEPARATOR
                        + "preAuthCode";
        accessLimitFacade.doAccessLimit(accessLimitKey, 2, CacheExpire.MINUTE_1 * 10, "???10???????????????????????????");

        ServiceResult<String> preAuthUrl = componentFacade.getPreAuthUrl(shopId);
        return Result.SUCCESS(preAuthUrl);
    }

    @Data
    public static class AuthorizedAppsVO {

        /**
         * ?????????app??????
         */
        List<AuthorizedAppVO> apps;

        /**
         * ??????????????????????????????????????????
         */
        boolean registing;

        @Data
        public static class AuthorizedAppVO {

            String appId;

            String appName;

            Integer fastRegiste;

            Integer configPay;

            Integer sub;

            public static AuthorizedAppVO transfer(ShopAppDTO shopAppDO) {
                AuthorizedAppVO authorizedAppVO = new AuthorizedAppVO();
                authorizedAppVO.setAppId(shopAppDO.getAppId());
                authorizedAppVO.setFastRegiste(shopAppDO.getFastRegiste());
                if (StringUtils.isEmpty(shopAppDO.getPayConfig())) {
                    authorizedAppVO.setConfigPay(0);
                    authorizedAppVO.setSub(0);
                }
                else {
                    PayConfigDTO payConfig = PayConfigDTO.parse(shopAppDO.getPayConfig());
                    authorizedAppVO.setConfigPay(1);
                    authorizedAppVO.setSub(payConfig.isSub() ? 1 : 0);
                }

                String nickNameCache = foundryMiniProgramLocalService
                        .getNickNameCache(shopAppDO.getShopId(), shopAppDO.getAppId());
                authorizedAppVO.setAppName(nickNameCache);
                return authorizedAppVO;
            }
        }
    }

}
