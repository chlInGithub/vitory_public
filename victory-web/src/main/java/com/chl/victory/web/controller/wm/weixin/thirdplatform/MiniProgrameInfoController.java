package com.chl.victory.web.controller.wm.weixin.thirdplatform;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.imgservice.ZimgService;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.localservice.model.UploadMediaResult;
import com.chl.victory.serviceapi.merchant.model.StyleConfigDTO;
import com.chl.victory.serviceapi.weixin.enums.WeixinMediaTypeEnum;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.merchant.model.PayConfigDTO;
import com.chl.victory.serviceapi.merchant.model.ShopAppDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer.UploadedMediaDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.AccountBasicInfoDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.DomainInfo;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.DomainResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.SetNickNameDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo.WebViewDomainInfo;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.AddCategoryDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category.GetCategoriesResult;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import static com.chl.victory.localservice.manager.LocalServiceManager.foundryMiniProgramLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.authorizerFacade;
import static com.chl.victory.webcommon.manager.RpcManager.foundryMiniProgram4BasicInfoFacade;
import static com.chl.victory.webcommon.manager.RpcManager.foundryMiniProgram4CategoryFacade;
import static com.chl.victory.webcommon.manager.RpcManager.merchantFacade;

/**
 * ???????????????
 * @author ChenHailong
 * @date 2020/5/28 14:02
 **/
@Controller
@RequestMapping("/p/wm/weixin/thirdplatform/mini/info/")
@Slf4j
public class MiniProgrameInfoController {

    @Resource
    ZimgService zimgService;

    /**
     * ???????????????????????????????????????
     * @return
     * @throws BusServiceException
     */
    @GetMapping(path = "setedCates", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result setedCates() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<GetCategoriesResult> categoriesResult;
        // ???????????????????????????
        categoriesResult = foundryMiniProgram4CategoryFacade.getCategory(sessionCache.getShopId(), sessionCache.getAppId());
        if (!categoriesResult.getSuccess()){
            return Result.FAIL(categoriesResult.getMsg());
        }

        return Result.SUCCESS(categoriesResult.getData());
    }

    @GetMapping(path = "allCates", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result allCates() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<String> categoriesResult;
        // ???????????????????????????
        categoriesResult = foundryMiniProgram4CategoryFacade
                .getAllCategories(sessionCache.getShopId(), sessionCache.getAppId());
        if (!categoriesResult.getSuccess()){
            return Result.FAIL(categoriesResult.getMsg());
        }
        return Result.SUCCESS(categoriesResult.getData());
    }

    /**
     * ??????????????????????????????
     * @param imgFile
     * @return
     * @throws Exception
     */
    @PostMapping(path = "uploadMedia", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result weixinUploadImg(@RequestParam("imgFile") MultipartFile imgFile) throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        ServiceResult<ShopAppDTO> shopAppResult = merchantFacade
                .selectShopAppWithValidate(sessionCache.getShopId(), sessionCache.getAppId());
        ShopAppDTO shopAppDTO = shopAppResult.getData();
        if (!shopAppResult.getSuccess() || null == shopAppDTO) {
            return Result.FAIL("??????shopApp");
        }

        String md5 = zimgService.uploadImage(imgFile);
        if (!StringUtils.isEmpty(md5)) {
            String mediaId;
            ServiceResult<String> uploadMediaUrlResult = authorizerFacade
                    .getUploadMediaUrl(shopAppDTO.getShopId(), shopAppDTO.getAppId(), WeixinMediaTypeEnum.image);
            if (!uploadMediaUrlResult.getSuccess()) {
                return Result.FAIL(uploadMediaUrlResult.getMsg());
            }
            String uploadMediaUrl = uploadMediaUrlResult.getData();

            UploadMediaResult uploadMediaResult = foundryMiniProgramLocalService.uploadTemporaryMedia(shopAppDTO.getShopId(), shopAppDTO.getAppId(), uploadMediaUrl, imgFile);
            mediaId = uploadMediaResult.getMedia_id();

            UploadedMediaDTO uploadedMediaDTO = new UploadedMediaDTO();
            uploadedMediaDTO.setMd5(md5);
            uploadedMediaDTO.setMediaId(mediaId);
            RpcManager.authorizerFacade
                    .setMediaCache(sessionCache.getShopId(), sessionCache.getAppId(), uploadedMediaDTO);
            return Result.SUCCESS(md5);
        }
        return Result.FAIL("??????????????????");
    }

    /**
     * ????????????????????????
     * @return
     * @throws Exception
     */
    @PostMapping(path = "uploadPayCert", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result uploadPayCert(@RequestParam("file") MultipartFile file) throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        final int maxBytes = 4000;
        if (file.getSize() > maxBytes) {
            return Result.FAIL("???????????????");
        }

        if (!file.getOriginalFilename().endsWith("p12")) {
            return Result.FAIL("?????????p12??????");
        }

        // TODO ??????????????????????????????

        ServiceResult<Integer> result = merchantFacade
                .saveWXPayCert(sessionCache.getShopId(), sessionCache.getAppId(), file.getBytes());
        if (result.getSuccess() && result.getData() > 0) {
            return Result.SUCCESS();
        }

        return Result.FAIL("??????????????????");
    }

    /**
     * ??????????????????
     */
    @PostMapping(path = "modifySignature", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result modifySignature(@RequestParam("signature") String signature) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        foundryMiniProgram4BasicInfoFacade
                .modifysignature(sessionCache.getShopId(), sessionCache.getAppId(), signature);
        return Result.SUCCESS();
    }

    /**
     * ????????????
     */
    @PostMapping(path = "modifyHeadImage", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result modifyHeadImage(@RequestParam("imgs") String imgs) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        String[] split = imgs.split(",");
        if (split.length != 1) {
            return Result.FAIL("????????????????????????");
        }
        String imgId = split[ 0 ];

        // TODO ?????????????????????
        /*String mediaId = authorizerService.getMediaCache(shopAppDO, imgId);
        if (StringUtils.isBlank(mediaId)) {
            return Result.FAIL("??????????????????");
        }*/

        ServiceResult serviceResult = foundryMiniProgram4BasicInfoFacade
                .modifyheadimage(sessionCache.getShopId(), sessionCache.getAppId(), imgId);
        if (!serviceResult.getSuccess()) {
            return Result.FAIL(serviceResult.getMsg());
        }

        return Result.SUCCESS();
    }

    /**
     * ????????????
     */
    @PostMapping(path = "modifyNick", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result modifyNick(@NotNull SetNickNameDTO setNickNameDTO) throws BusServiceException {
        ValidationUtil.validate(setNickNameDTO);
        SessionCache sessionCache = SessionUtil.getSessionCache();
        foundryMiniProgram4BasicInfoFacade
                .setNickName(sessionCache.getShopId(), sessionCache.getAppId(), setNickNameDTO);
        return Result.SUCCESS();
    }

    /**
     * ????????????????????????
     */
    @PostMapping(path = "pay", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result pay(@NotNull PayConfigDTO payConfig) throws BusServiceException {
        ValidationUtil.validate(payConfig);
        SessionCache sessionCache = SessionUtil.getSessionCache();

        ServiceResult<ShopAppDTO> shopAppResult = merchantFacade
                .selectShopAppWithValidate(sessionCache.getShopId(), sessionCache.getAppId());
        if (!shopAppResult.getSuccess() || shopAppResult.getData() == null) {
            return Result.FAIL("??????shopApp");
        }

        ShopAppDTO shopAppDO = shopAppResult.getData();

        if (StringUtils.isNotBlank(shopAppDO.getPayConfig())) {
            PayConfigDTO currentPayConfig = PayConfigDTO.parse(shopAppDO.getPayConfig());
            boolean needUpdate = false;
            if (payConfig.isSub() != currentPayConfig.isSub()) {
                needUpdate = true;
            }

            if (StringUtils.isBlank(payConfig.getApiKey())) {
                payConfig.setApiKey(currentPayConfig.getApiKey());
            }

            if (!needUpdate && !payConfig.isSub() && StringUtils.isNotBlank(payConfig.getApiKey()) && !payConfig
                    .getApiKey().equals(currentPayConfig.getApiKey())) {
                needUpdate = true;
            }
            if (!needUpdate && !payConfig.getMchId().equals(currentPayConfig.getMchId())) {
                needUpdate = true;
            }
            if (!needUpdate) {
                return Result.SUCCESS();
            }
        }

        ServiceResult serviceResult = merchantFacade
                .savePayConfig(shopAppDO.getId(), sessionCache.getAppId(), sessionCache.getShopId(), payConfig);
        if (!serviceResult.getSuccess()) {
            return Result.FAIL(serviceResult.getMsg());
        }
        return Result.SUCCESS();
    }

    /**
     * ????????????????????????
     */
    @PostMapping(path = "style", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result style(@NotNull StyleConfigDTO payConfig) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        ServiceResult<ShopAppDTO> shopAppResult = merchantFacade
                .selectShopAppWithValidate(sessionCache.getShopId(), sessionCache.getAppId());
        if (!shopAppResult.getSuccess() || shopAppResult.getData() == null) {
            return Result.FAIL("??????shopApp");
        }

        ShopAppDTO shopAppDO = shopAppResult.getData();

        ServiceResult serviceResult = merchantFacade
                .saveStyle(shopAppDO.getId(), sessionCache.getAppId(), sessionCache.getShopId(), payConfig);
        if (!serviceResult.getSuccess()) {
            return Result.FAIL(serviceResult.getMsg());
        }
        return Result.SUCCESS();
    }

    /**
     * ????????????
     */
    @PostMapping(path = "modifyCate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result modifyCate(@NotNull AddCategoryDTO addCategoryDTO, @RequestParam("isAdd") int isAdd) throws BusServiceException {
        ValidationUtil.validate(addCategoryDTO);
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult serviceResult;
        if (isAdd == 1) {
            serviceResult = foundryMiniProgram4CategoryFacade
                    .addCategory(sessionCache.getShopId(), sessionCache.getAppId(), addCategoryDTO);
        }else {
            serviceResult = foundryMiniProgram4CategoryFacade
                    .modifyCategory(sessionCache.getShopId(), sessionCache.getAppId(), addCategoryDTO);
        }
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * ????????????
     * ???????????? https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/Mini_Programs/Mini_Program_Information_Settings.html
     */
    @GetMapping(path = "query", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result query() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<ShopAppDTO> shopAppResult = merchantFacade
                .selectShopAppWithValidate(sessionCache.getShopId(), sessionCache.getAppId());
        if (!shopAppResult.getSuccess()) {
            return Result.FAIL("??????shopApp");
        }
        ShopAppDTO shopAppDO = shopAppResult.getData();
        ServiceResult<Boolean> existWXPayCertResult = merchantFacade
                .existWXPayCert(sessionCache.getShopId(), sessionCache.getAppId());
        boolean existWXPayCert = existWXPayCertResult.getSuccess() && existWXPayCertResult.getData();

        // ???nick?????????????????????????????? ??????????????????
        ServiceResult<AccountBasicInfoDTO> accountBasicInfoResult = foundryMiniProgram4BasicInfoFacade
                .getAccountBasicInfo(sessionCache.getShopId(), sessionCache.getAppId(), true);
        if (!accountBasicInfoResult.getSuccess()) {
            return Result.FAIL(accountBasicInfoResult.getMsg());
        }

        AccountBasicInfoDTO accountBasicInfo = accountBasicInfoResult.getData();

        if (accountBasicInfo == null) {
            return Result.SUCCESS();
        }

        if (!accountBasicInfo.isSuccess()) {
            return Result.FAIL(accountBasicInfo.getErrmsg());
        }

        // ??????app???????????????
        /*String nickName = "";
        nickName = foundryMiniProgram4BasicInfoService.getNickNameCache(shopAppDO);

        String auditingNickName = "";
        auditingNickName = foundryMiniProgram4BasicInfoService.getNickNameAuditingCache(shopAppDO);
        */
        AppBaseInfoVO appBaseInfoVO = new AppBaseInfoVO();
        appBaseInfoVO.setNickName(accountBasicInfo.getNickName());
        appBaseInfoVO.setAuditingNickName(accountBasicInfo.getAuditingNickName());
        BeanUtils.copyProperties(accountBasicInfo, appBaseInfoVO);

        appBaseInfoVO.setHead_image_info(new AppBaseInfoVO.HeadImageInfo());
        BeanUtils.copyProperties(accountBasicInfo.getHead_image_info(), appBaseInfoVO.getHead_image_info());
        appBaseInfoVO.setSignature_info(new AppBaseInfoVO.SignatureInfo());
        BeanUtils.copyProperties(accountBasicInfo.getSignature_info(), appBaseInfoVO.getSignature_info());
        appBaseInfoVO.setWx_verify_info(new AppBaseInfoVO.WXVerifyInfo());
        BeanUtils.copyProperties(accountBasicInfo.getWx_verify_info(), appBaseInfoVO.getWx_verify_info());

        if (StringUtils.isBlank(shopAppDO.getPayConfig())) {
            appBaseInfoVO.setPay(new PayConfigDTO());
        }
        else {
            PayConfigDTO payConfig = PayConfigDTO.parse(shopAppDO.getPayConfig());
            if (!payConfig.isSub() && StringUtils.isNotBlank(payConfig.getApiKey())) {
                appBaseInfoVO.setExistApiKey(true);
                payConfig.setApiKey("");
            }
            appBaseInfoVO.setPay(payConfig);
        }

        appBaseInfoVO.setExistPayCert(existWXPayCert);

        return Result.SUCCESS(appBaseInfoVO);
    }

    /**
     * ??????domain  webviewdomain??????
     */
    @GetMapping(path = "queryDomains", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryDomains() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<DomainResult> domainResult;
        DomainVO domainVO = new DomainVO();

        domainResult = foundryMiniProgram4BasicInfoFacade.getDomain(sessionCache.getShopId(), sessionCache.getAppId());
        if (!domainResult.getSuccess()) {
            return Result.FAIL(domainResult.getMsg());
        }

        DomainInfo domain = domainResult.getData().getDomainInfo();
        if (domain != null) {
            domainVO.setRequestdomain(domain.getRequestdomain());
            domainVO.setWsrequestdomain(domain.getWsrequestdomain());
            domainVO.setUploaddomain(domain.getUploaddomain());
            domainVO.setDownloaddomain(domain.getDownloaddomain());
        }

        WebViewDomainInfo webViewDomain = domainResult.getData().getWebViewDomainInfo();
        if (webViewDomain != null) {
            domainVO.setWebviewdomain(webViewDomain.getWebviewdomain());
        }

        return Result.SUCCESS(domainVO);
    }

    /**
     * ??????????????????
     */
    @GetMapping(path = "queryStyle", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryStyle() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<StyleConfigDTO> result;
        StyleConfigVO vo = new StyleConfigVO();

        result = merchantFacade.selectStyle(sessionCache.getAppId(), sessionCache.getShopId());
        if (!result.getSuccess()) {
            return Result.FAIL(result.getMsg());
        }

        StyleConfigDTO dto = result.getData();
        if (dto != null) {
            BeanUtils.copyProperties(dto, vo);
        }

        return Result.SUCCESS(vo);
    }

    @Data
    public static class StyleConfigVO{
        /**
         * ??????????????????
         */
        String navBarBackColor;

        /**
         * ?????????????????????
         */
        String navBarFontColor;

        /**
         * ???????????????
         */
        String bgColor;
    }

    @Data
    public static class AppBaseInfoVO {

        /**
         * ?????? appid
         */
        String appid;

        String nickName;

        String auditingNickName;

        /**
         * ???????????????1???????????????2???????????????3???????????????
         */
        Integer account_type;

        /**
         * ????????????
         * 0	?????? 1	?????? 2	?????? 3	?????? 4	????????????
         */
        Integer principal_type;

        /**
         * ????????????
         */
        String principal_name;

        /**
         * ??????????????????
         * 1	?????????????????? 2	??????????????? 3	??????????????????
         */
        Integer realname_status;

        /**
         * ??????????????????
         */
        WXVerifyInfo wx_verify_info;

        /**
         * ??????????????????
         */
        SignatureInfo signature_info;

        /**
         * ????????????
         */
        HeadImageInfo head_image_info;

        PayConfigDTO pay;

        boolean existPayCert;

        boolean existApiKey;

        @Data
        public static class HeadImageInfo {

            /**
             * ?????? url
             */
            String head_image_url;

            /**
             * ???????????????????????????????????????
             */
            Integer modify_used_count;

            /**
             * ???????????????????????????????????????
             */
            Integer modify_quota;
        }

        @Data
        public static class SignatureInfo {

            /**
             * ????????????
             */
            String signature;

            /**
             * ?????????????????????????????????????????????
             */
            Integer modify_used_count;

            /**
             * ?????????????????????????????????????????????
             */
            Integer modify_quota;
        }

        @Data
        public static class WXVerifyInfo {

            /**
             * ??????????????????????????????????????????????????????????????????
             */
            boolean qualification_verify;

            /**
             * ??????????????????
             */
            boolean naming_verify;

            /**
             * ?????????????????????qualification_verify == true ?????????????????????
             */
            boolean annual_review;

            /**
             * ?????????????????????????????????qualification_verify == true ?????????????????????,??????1550490981
             */
            Long annual_review_begin_time;

            String annual_review_begin_time_desc;

            /**
             * ?????????????????????????????????qualification_verify == true ?????????????????????
             */
            Long annual_review_end_time;

            String annual_review_end_time_desc;

            public void setAnnual_review_begin_time(Long annual_review_begin_time) {
                this.annual_review_begin_time = annual_review_begin_time;
                if (null != annual_review_begin_time) {
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                        annual_review_begin_time_desc = simpleDateFormat
                                .format(new Date(annual_review_begin_time * 1000));
                    } catch (Exception e) {
                    }
                }
            }

            public void setAnnual_review_end_time(Long annual_review_end_time) {
                this.annual_review_end_time = annual_review_end_time;
                if (null != annual_review_end_time) {
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                        annual_review_end_time_desc = simpleDateFormat.format(new Date(annual_review_end_time * 1000));
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Data
    public static class DomainVO {

        String[] requestdomain;

        String[] wsrequestdomain;

        String[] uploaddomain;

        String[] downloaddomain;

        String[] webviewdomain;
    }
}
