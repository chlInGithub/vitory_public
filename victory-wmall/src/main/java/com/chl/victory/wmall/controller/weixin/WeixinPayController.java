package com.chl.victory.wmall.controller.weixin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.weixin.model.pay.WxPrePayDTO;
import com.chl.victory.webcommon.util.HttpResponseUtil;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.wxPayFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/wxpay/")
@Api(description = "用户相关信息")
@Validated
@Slf4j
public class WeixinPayController {

    /**
     * 微信预支付
     */
    @GetMapping("checkPayed")
    @ApiOperation(value = "验证是否已微信支付", produces = MediaType.APPLICATION_JSON_VALUE)
    public void checkPayed(@RequestParam @ApiParam(name = "订单ID", required = true) Long orderId,
            HttpServletResponse response, HttpServletRequest request) throws Exception {
        // sesssion
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        wxPayFacade.checkPayed(sessionCache.getShopId(), sessionCache.getUserId(), sessionCache.getAppId(), orderId);

        // redirect order detail
        HttpResponseUtil.sendRedirect(request, response, "/wmall/orderdetail.html?orderId=" + orderId);
    }

    /**
     * 微信预支付
     */
    @PostMapping("prePay")
    @ApiOperation(value = "微信预支付", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result prePay(@RequestParam @ApiParam(name = "订单ID", required = true) Long orderId) throws Exception {
        // sesssion
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        ServiceResult<WxPrePayDTO> result = wxPayFacade
                .prePay(sessionCache.getShopId(), sessionCache.getUserId(), sessionCache.getAppId(), orderId);
        if (result.getSuccess()) {
            return Result.SUCCESS(result.getData());
        }
        return Result.FAIL(result.getMsg());
    }
}
