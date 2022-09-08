package com.chl.victory.wmall.controller.weixin;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.common.enums.ThirdPlatformEnum;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.merchant.model.ShopAppDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDTO;
import com.chl.victory.serviceapi.weixin.model.WeixinCode2Session;
import com.chl.victory.serviceapi.weixin.model.WxPhoneDTO;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.filter.CheckSignFilter;
import com.chl.victory.wmall.filter.SessionFilter;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.memberFacade;
import static com.chl.victory.webcommon.manager.RpcManager.merchantFacade;
import static com.chl.victory.webcommon.manager.RpcManager.miniProgramFacade;

/**
 * 处理从微信小程序发出的请求，非html
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/wx/")
@Api(description = "用户相关信息")
@Validated
public class WeixinController {

    @Resource
    SessionService sessionService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @PostMapping("phoneV2")
    @ApiOperation(value = "处理小程序手机号密文", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result phoneV2(WeixinPhoneParam param) throws Exception {
        return phone(param);
    }

    @PostMapping("phone")
    @ApiOperation(value = "处理小程序手机号密文", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result phone(@RequestBody WeixinPhoneParam param) throws Exception {
        // TODO https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/getPhoneNumber.html

        String v = httpServletRequest.getParameter(CheckSignFilter.PARAM_NAME_V);
        DealResult dealResult;
        if (StringUtils.isEmpty(v)) {
            dealResult = dealPhoneV1(param);
        }else {
            dealResult = dealPhoneV2(param);
        }

        WmallSessionCache sessionCache = dealResult.getSessionCache();
        WxPhoneDTO wxPhoneDTO = dealResult.getWxPhoneDTO();
        ServiceResult<String> mobileResult = miniProgramFacade.parseAndSaveMobile(wxPhoneDTO);
        if (mobileResult.getSuccess()) {
            sessionCache.setMobile(mobileResult.getData());
            sessionService.setSession(sessionCache.getThirdOpenId(), sessionCache.getKey(), sessionCache);
        }

        WmallSessionCache temp = new WmallSessionCache();
        temp.setMobile(sessionCache.getMobile());
        temp.setThirdImg(sessionCache.getThirdImg());
        temp.setThirdNick(sessionCache.getThirdNick());
        return Result.SUCCESS(temp);
    }

    private DealResult dealPhoneV2(WeixinPhoneParam param) throws Exception {
        if (StringUtils.isEmpty(param.getEncryptedData()) || StringUtils.isEmpty(param.getIv())) {
            throw new Exception("数据错误");
        }

        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        if (sessionCache == null) {
            throw new Exception("缺失访问会话");
        }

        if (sessionCache.getUserId() == null) {
            throw new Exception("缺失用户标识");
        }

        WxPhoneDTO wxPhoneDTO = new WxPhoneDTO();
        wxPhoneDTO.setShopId(sessionCache.getShopId());
        wxPhoneDTO.setUserId(sessionCache.getUserId());
        wxPhoneDTO.setTId(Long.valueOf(sessionCache.getThirdId()));
        wxPhoneDTO.setAppId(sessionCache.getAppId());
        wxPhoneDTO.setEncryptedData(CheckSignFilter.replace4Spe(param.getEncryptedData()));
        wxPhoneDTO.setIv(CheckSignFilter.replace4Spe(param.getIv()));
        wxPhoneDTO.setSessionKey(sessionCache.getSessionKey());

        DealResult dealResult = new DealResult();
        dealResult.setSessionCache(sessionCache);
        dealResult.setWxPhoneDTO(wxPhoneDTO);

        return dealResult;
    }

    @Data
    class DealResult{
        WxPhoneDTO wxPhoneDTO;
        WmallSessionCache sessionCache;
    }

    private DealResult dealPhoneV1(WeixinPhoneParam param) throws Exception {
        ValidationUtil.validate(param);
        Long shopId = param.shopId;
        String appId = param.appId;
        Long tId = param.tId;
        String loginCode = param.loginCode;
        String iv = param.iv;
        String encryptedData = param.encryptedData;
        String domain = param.getDomain();

        SessionFilter.ThirdCommonParam thirdCommonParam = new SessionFilter.ThirdCommonParam();
        thirdCommonParam.setShopId(shopId);
        thirdCommonParam.setThirdId(tId.toString());
        thirdCommonParam.setTCode(loginCode);

        // 获取用户信息
        ServiceResult<WeixinCode2Session> code2SessionResult = RpcManager.miniProgramFacade
                .getCode2Session(shopId, appId, thirdCommonParam.getTCode());
        if (!code2SessionResult.getSuccess()) {
            throw new Exception(code2SessionResult.getMsg());
        }

        WeixinCode2Session code2SessionFromWeixin = code2SessionResult.getData();

        String openid = code2SessionFromWeixin.getOpenid();
        String sessionKey = code2SessionFromWeixin.getSession_key();

        thirdCommonParam.setTOpenId(openid);
        String key = SessionFilter.getSessionCacheKey(thirdCommonParam);

        WmallSessionCache sessionCache = sessionService.getSession(domain, key, WmallSessionCache.class);

        if (sessionCache == null) {
            throw new Exception("缺失访问会话");
        }

        if (sessionCache.getUserId() == null) {
            throw new Exception("缺失用户标识");
        }

        WxPhoneDTO wxPhoneDTO = new WxPhoneDTO();
        wxPhoneDTO.setShopId(shopId);
        wxPhoneDTO.setUserId(sessionCache.getUserId());
        wxPhoneDTO.setTId(tId);
        wxPhoneDTO.setAppId(sessionCache.getAppId());
        wxPhoneDTO.setEncryptedData(encryptedData);
        wxPhoneDTO.setIv(iv);
        wxPhoneDTO.setSessionKey(sessionKey);

        DealResult dealResult = new DealResult();
        dealResult.setSessionCache(sessionCache);
        dealResult.setWxPhoneDTO(wxPhoneDTO);

        return dealResult;
    }

    @PostMapping("userInfoV2")
    @ApiOperation(value = "处理小程序用户信息", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result userInfoV2(WeixinUserInfoParam param) throws Exception {
        return userInfo(param);
    }

    @PostMapping("userInfo")
    @ApiOperation(value = "处理小程序用户信息", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result userInfo(@RequestBody WeixinUserInfoParam param) throws Exception {

        String v = httpServletRequest.getParameter(CheckSignFilter.PARAM_NAME_V);
        ShopMemberDTO shopMember;
        if (StringUtils.isEmpty(v)) {
            shopMember = dealUserInfoV1(param);
        }else {
            shopMember = dealUserInfoV2(param);
        }

        ServiceResult serviceResult = memberFacade.saveNickAndImg(shopMember);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    private ShopMemberDTO dealUserInfoV2(WeixinUserInfoParam param) throws Exception {
        if (StringUtils.isEmpty(param.getAvatarUrl()) || StringUtils.isEmpty(param.getNickName())) {
            throw new Exception("数据错误");
        }

        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        if (sessionCache == null) {
            throw new Exception("缺失访问会话");
        }

        if (sessionCache.getUserId() == null) {
            throw new Exception("缺失用户标识");
        }
        ShopMemberDTO shopMember = new ShopMemberDTO();
        shopMember.setShopId(sessionCache.getShopId());
        shopMember.setId(sessionCache.getUserId());
        shopMember.setNick(param.getNickName());
        shopMember.setAvatarUrl(param.getAvatarUrl());

        return shopMember;
    }

    private ShopMemberDTO dealUserInfoV1(WeixinUserInfoParam param) throws Exception {
        ValidationUtil.validate(param);
        Long shopId = param.shopId;
        String appId = param.getAppId();
        Long tId = param.tId;
        String loginCode = param.loginCode;
        String avatarUrl = param.avatarUrl;
        String nickName = param.nickName;
        String domain = param.domain;

        SessionFilter.ThirdCommonParam thirdCommonParam = new SessionFilter.ThirdCommonParam();
        thirdCommonParam.setShopId(shopId);
        thirdCommonParam.setThirdId(tId.toString());
        thirdCommonParam.setTCode(loginCode);

        // 获取用户信息
        ServiceResult<WeixinCode2Session> code2SessionResult = RpcManager.miniProgramFacade
                .getCode2Session(shopId, appId, thirdCommonParam.getTCode());
        if (!code2SessionResult.getSuccess()) {
            throw new Exception(code2SessionResult.getMsg());
        }

        WeixinCode2Session code2SessionFromWeixin = code2SessionResult.getData();

        String openid = code2SessionFromWeixin.getOpenid();

        thirdCommonParam.setTOpenId(openid);

        String key = SessionFilter.getSessionCacheKey(thirdCommonParam);
        WmallSessionCache sessionCache = sessionService.getSession(domain, key, WmallSessionCache.class);

        if (sessionCache == null) {
            throw new Exception("缺失访问会话");
        }

        if (sessionCache.getUserId() == null) {
            throw new Exception("缺失用户标识");
        }

        /*if (!StringUtils.isEmpty(sessionCache.getThirdNick()) && nickName.equals(sessionCache.getThirdNick())
                && !StringUtils.isEmpty(sessionCache.getThirdImg()) && avatarUrl.equals(sessionCache.getThirdImg())) {
            return Result.SUCCESS();
        }*/

        ShopMemberDTO shopMember = new ShopMemberDTO();
        shopMember.setShopId(shopId);
        shopMember.setId(sessionCache.getUserId());
        shopMember.setNick(nickName);
        shopMember.setAvatarUrl(avatarUrl);

        return shopMember;
    }

    /**
     * 暂时不用
     * @param shopId
     * @param code
     * @return
     */
    @PostMapping("loginCode")
    @ApiOperation(value = "处理小程序登录code", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result loginCode(@RequestParam @ApiParam(name = "店铺ID", required = true) Long shopId,
            @RequestParam("appId") String appId,
            @RequestParam @ApiParam(name = "微信登录的code", required = true) String code) throws Exception {
        // TODO https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/getPhoneNumber.html

        // 获取shop信息
        ServiceResult<ShopDTO> shopResult = merchantFacade.selectShop(shopId);

        if (!shopResult.getSuccess()) {
            return Result.FAIL("店铺不存在");
        }

        ServiceResult<ShopAppDTO> shopAppResult = merchantFacade.getShopAppCache(shopId, appId);
        if (!shopAppResult.getSuccess() || shopAppResult == null) {
            return Result.FAIL("缺少shopAPP");
        }
        ShopAppDTO shopAppCache = shopAppResult.getData();

        // 获取用户信息
        ServiceResult<WeixinCode2Session> code2SessionResult = RpcManager.miniProgramFacade
                .getCode2Session(shopId, appId, code);
        if (!code2SessionResult.getSuccess()) {
            return Result.FAIL(code2SessionResult.getMsg());
        }

        WeixinCode2Session code2SessionFromWeixin = code2SessionResult.getData();

        String openid = code2SessionFromWeixin.getOpenid();
        String sessionKey = code2SessionFromWeixin.getSession_key();

        if (StringUtils.isEmpty(openid)) {
            return Result.FAIL("获取微信信息失败");
        }

        // 存储店铺和微信用户关系
        miniProgramFacade.saveShopAndWeixinUser(shopId, ThirdPlatformEnum.weixin.getCode().toString(), appId, openid);

        // TODO  暂不处理sessionKey的cache存储

        return Result.SUCCESS(openid);
    }

    @Data
    public static class WeixinPhoneParam {

        @NotNull(message = "缺失店铺标识") Long shopId;

        @NotNull(message = "缺失第三方平台ID") Long tId;

        @NotEmpty(message = "缺失微信登录编号") String loginCode;

        @NotEmpty(message = "缺失加密算法初始向量") String iv;

        @NotEmpty(message = "缺失微信手机号信息密文") String encryptedData;

        @NotEmpty(message = "缺失domain") String domain;

        @NotEmpty(message = "缺少app标识") String appId;
    }

    @Data
    public static class WeixinUserInfoParam {

        @NotNull(message = "缺失店铺标识") Long shopId;

        @NotNull(message = "缺失第三方平台ID") Long tId;

        @NotEmpty(message = "缺失编号") String loginCode;

        @NotEmpty(message = "缺失头像URL") String avatarUrl;

        @NotEmpty(message = "缺失微信昵称") String nickName;

        @NotEmpty(message = "缺失domain") String domain;

        @NotEmpty(message = "缺少app标识") String appId;
    }

}
