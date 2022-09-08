package com.chl.victory.web.controller.wm.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.web.service.LoginService;
import com.chl.victory.web.service.RSAService;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.merchant.model.MerchantUserDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDTO;
import com.chl.victory.serviceapi.merchant.model.ShopDeliveryAreaDTO;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.Result;
import com.chl.victory.webcommon.util.CookieUtil;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.chl.victory.webcommon.manager.RpcManager.merchantFacade;

/**
 * @author hailongchen9
 */
@Controller
@RequestMapping("/p")
public class ShopController {

    @Resource
    SessionService sessionService;

    @Resource
    LoginService loginService;

    @Resource
    RSAService rsaService;

    @Resource
    HttpServletRequest httpServletRequest;

    @PostMapping(path = "/wm/shop/modifyPw", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result modifyPw(@Validated ModifyPw modifyPw) {
        //  比对手机号和旧密码
        ServiceResult<MerchantUserDTO> verifyResult = loginService
                .verifyLogin(modifyPw.mobile, modifyPw.oldPw, httpServletRequest.getSession().getId());
        if (verifyResult.getSuccess() && verifyResult.getData() != null && verifyResult.getData().getId() != null) {
            // 比对当前登录的手机号
            if (!SessionUtil.getSessionCache().getMobile().equals(verifyResult.getData().getMobile().toString())) {
                return Result.FAIL("用户名或密码错误");
            }

            String newPass = rsaService
                    .deCryptoAndEnCrypto(modifyPw.getNewPw(), httpServletRequest.getSession().getId());
            if (StringUtils.isEmpty(newPass)) {
                return Result.FAIL("修改密码失败, 钥匙没要到。");
            }
            MerchantUserDTO merchantUserDO = new MerchantUserDTO();
            merchantUserDO.setMobile(verifyResult.getData().getMobile());
            merchantUserDO.setId(verifyResult.getData().getId());
            merchantUserDO.setPass(newPass);
            merchantUserDO.setInitPassModified(true);
            ServiceResult saveResult = merchantFacade.saveUser(merchantUserDO);
            if (!saveResult.getSuccess()) {
                return Result.FAIL("修改密码失败," + saveResult.getMsg());
            }
        }
        else {
            return Result.FAIL(verifyResult.getMsg());
        }
        // 更新密码
        return Result.SUCCESS();
    }

    @PostMapping(path = "/wm/shop/save", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result save(@Validated Shop shop) throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<ShopDTO> shopDOResult = merchantFacade.selectShop(sessionCache.getShopId());
        if (!shopDOResult.getSuccess()){
            return Result.FAIL("shop不存在");
        }

        ShopDTO shopDTO = shopDOResult.getData();
        boolean needUpdate = false;
        ShopDTO shopDO = new ShopDTO();
        if (!shop.getName().equals(shopDTO.getName())) {
            needUpdate = true;
        }
        if (!shop.getImgs().equals(shopDTO.getImg())) {
            needUpdate = true;
            shopDO.setImg(shop.getImgs());
        }
        if (!shop.getMobile().equals(shopDTO.getMobile())) {
            needUpdate = true;
        }
        if (!shop.getAddress().equals(shopDTO.getAddress())) {
            needUpdate = true;
            shopDO.setAddress(shop.getAddress());
        }
        if (needUpdate) {
            shopDO.setId(sessionCache.getShopId());
            shopDO.setName(shop.getName());
            shopDO.setMobile(shop.getMobile());
            shopDO.setOperatorId(SessionUtil.getSessionCache().getUserId());
            ServiceResult serviceResult = merchantFacade.saveShop(shopDO);
            if (serviceResult.getSuccess()) {
                sessionCache.setShopId(shopDO.getId());
                sessionCache.setShopName(shopDO.getName());
                Cookie loginDomain = CookieUtil.getLoginDomain(httpServletRequest);
                sessionService.setSession(loginDomain.getValue(), sessionCache.getUserId().toString(), sessionCache);
                return Result.SUCCESS();
            }
            else {
                return Result.FAIL(serviceResult.getMsg() + " 请检查验证码");
            }
        }
        return Result.SUCCESS();
    }

    @PostMapping(path = "/wm/shop/saveFreightFree", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result saveFreightFree(@RequestParam("orderFeeOfFreightFree") BigDecimal freightFree) {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult result = merchantFacade.saveFreightFree(freightFree, SessionUtil.getSessionCache().getShopId());
        if (result.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(result.getMsg());
    }

    @PostMapping(path = "/wm/shop/saveDeliveryArea", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result saveDeliveryArea(@RequestParam("deliveryArea") String deliveryArea) {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult result = merchantFacade.saveDeliveryArea(deliveryArea, SessionUtil.getSessionCache().getShopId());
        if (result.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(result.getMsg());
    }

    @PostMapping(path = "/wm/shop/savePayType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result savePayType(@RequestParam("payTypes") String payTypes) {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult result = merchantFacade
                .savePayType(JSONObject.parseArray(payTypes, Integer.class), SessionUtil.getSessionCache().getShopId());
        if (result.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(result.getMsg());
    }

    @PostMapping(path = "/wm/shop/saveDeliveryType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result saveDeliveryType(@RequestParam("deliveryTypes") String deliveryTypes) {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult result = merchantFacade
                .saveDeliveryType(JSONObject.parseArray(deliveryTypes, Integer.class), SessionUtil.getSessionCache().getShopId());
        if (result.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(result.getMsg());
    }

    /**
     * 店铺基本信息
     * @return
     */
    @GetMapping(path = "/wm/shop/query", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result query() throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        if (sessionCache.getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult<ShopDTO> shopResult = merchantFacade.selectShop(sessionCache.getShopId());
        if (!shopResult.getSuccess()) {
            return Result.FAIL("shop不存在");
        }

        ShopDTO shopDTO = shopResult.getData();
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopDTO, shop);
        shop.setImgs(shopDTO.getImg());
        ServiceResult<Integer> countResult = merchantFacade.countShopApp(sessionCache.getShopId());
        if (countResult.getSuccess()) {
            shop.setAuthedAppNum(countResult.getData());
        }

        return Result.SUCCESS(shop);
    }

    /**
     * 店铺统一运费配置
     * @return
     */
    @GetMapping(path = "/wm/shop/queryFreightFree", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryFreightFree() {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult<BigDecimal> shopResult = merchantFacade
                .selectShopFreightFree(SessionUtil.getSessionCache().getShopId());
        if (shopResult.getSuccess()) {
            BigDecimal orderFeeOfFreightFree = shopResult.getData();
            return Result.SUCCESS(orderFeeOfFreightFree);
        }

        return Result.FAIL(shopResult.getMsg());
    }

    /**
     * 店铺支付类型配置
     * @return
     */
    @GetMapping(path = "/wm/shop/queryPayType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryPayType() {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult<List<Integer>> shopResult = merchantFacade
                .selectShopPayType(SessionUtil.getSessionCache().getShopId());
        if (shopResult.getSuccess()) {
            List<PayTypeVO> payTypeVOs = new ArrayList<>();
            if (!CollectionUtils.isEmpty(shopResult.getData())) {
                payTypeVOs.addAll(shopResult.getData().stream().map(item -> {
                    PayTypeEnum byCode = PayTypeEnum.getByCode(item);
                    return new PayTypeVO(byCode == null ? -1 : byCode.getCode());
                }).collect(Collectors.toList()));
            }
            return Result.SUCCESS(payTypeVOs);
        }

        return Result.FAIL(shopResult.getMsg());
    }

    /**
     * 店铺交付类型配置
     * @return
     */
    @GetMapping(path = "/wm/shop/queryDeliveryType", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryDeliveryType() {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult<List<Integer>> shopResult = merchantFacade
                .selectShopDeliveryType(SessionUtil.getSessionCache().getShopId());
        if (shopResult.getSuccess()) {
            List<PayTypeVO> payTypeVOs = new ArrayList<>();
            if (!CollectionUtils.isEmpty(shopResult.getData())) {
                payTypeVOs.addAll(shopResult.getData().stream().map(item -> {
                    DeliverTypeEnum byCode = DeliverTypeEnum.getByCode(item);
                    return new PayTypeVO(byCode == null ? -1 : byCode.getCode());
                }).collect(Collectors.toList()));
            }
            return Result.SUCCESS(payTypeVOs);
        }

        return Result.FAIL(shopResult.getMsg());
    }

    /**
     * 店铺配送范围设置
     * @return
     */
    @GetMapping(path = "/wm/shop/queryDeliveryArea", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryDeliveryArea() {
        if (SessionUtil.getSessionCache().getShopId() == null) {
            return Result.FAIL("请配置店铺信息");
        }

        ServiceResult<List<ShopDeliveryAreaDTO>> selectShopDeliveryArea = merchantFacade
                .selectShopDeliveryArea(SessionUtil.getSessionCache().getShopId());
        if (selectShopDeliveryArea.getSuccess()) {
            return Result.SUCCESS(selectShopDeliveryArea.getData());
        }

        return Result.FAIL(selectShopDeliveryArea.getMsg());
    }

    /**
     * 登录者基本信息
     * @return
     */
    @GetMapping(path = "/wm/shop/loginUser", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result loginUser() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        LoginUser loginUser = new LoginUser();
        loginUser.setId(sessionCache.getUserId());
        loginUser.setMobile(sessionCache.getMobile());
        loginUser.setShopName(sessionCache.getShopName());
        loginUser.setShopId(sessionCache.getShopId());
        loginUser.setInvalidTime(DateFormatUtils.format(sessionCache.getInvalidTime(), DateConstants.format1));
        return Result.SUCCESS(loginUser);
    }

    @Data
    public static class PayTypeVO {

        Integer code;

        public PayTypeVO(Integer code) {
            this.code = code;
        }
    }

    @Data
    public static class LoginUser {

        String mobile;

        Long id;

        Long shopId;


        String shopName;

        String invalidTime;
    }

    @Data
    public static class ModifyPw {

        @NotBlank(message = "请填写店铺联系手机号")
        public String mobile;

        @NotBlank(message = "请填写当前密码")
        public String oldPw;

        @NotBlank(message = "请填写新密码")
        public String newPw;
    }

    @Data
    public static class Shop {

        @Positive(message = "id必须正数")
        public Long id;

        @NotBlank(message = "请填写店铺名称")
        public String name;

        @NotNull(message = "请填写店铺联系手机号")
        @Positive(message = "请填写店铺联系手机号")
        public Long mobile;

        @NotEmpty(message = "请上传店铺图片")
        public String imgs;

        @NotBlank(message = "请填写店铺地址")
        public String address;

        // @NotBlank(message = "请填写appId")
        //public String appId;

        // @NotBlank(message = "请填写secret")
        //public String secret;

        //@NotBlank(message = "请填写apiKey")
        //public String apiKey;

        //@NotBlank(message = "请填写mechId")
        //public String mechId;

        //@NotBlank(message = "请填写修改确认码")
        //public String checkCode;

        //@NotBlank(message = "请填写预备确认码")
        //public String nextCheckCode;

        public int authedAppNum;
    }
}
