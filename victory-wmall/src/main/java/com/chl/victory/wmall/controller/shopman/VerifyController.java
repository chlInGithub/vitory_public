package com.chl.victory.wmall.controller.shopman;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.merchant.query.MerchantUserQueryDTO;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.filter.CheckSignFilter;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/shopman/")
public class VerifyController {

    @Resource
    SessionService sessionService;

    @Resource
    HttpServletRequest request;

    /**
     * 验证店铺人员
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "verify", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result taken(@RequestParam("merchant") String merchant, @RequestParam("mobile") Long mobile) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        String[] split = merchant.split("_");
        Long defaultValue = 0L;
        Long shopId = NumberUtils.toLong(split[ 0 ], defaultValue);
        Long merchantId = NumberUtils.toLong(split[ 1 ], defaultValue);

        if (defaultValue.equals(shopId) || defaultValue.equals(merchantId)) {
            return Result.FAIL("参数错误");
        }
        // exist
        MerchantUserQueryDTO merchantUserQueryDTO = new MerchantUserQueryDTO();
        merchantUserQueryDTO.setShopId(shopId);
        merchantUserQueryDTO.setId(merchantId);
        merchantUserQueryDTO.setMobile(mobile);
        ServiceResult<Boolean> merchantUser = RpcManager.merchantFacade.isMerchantUser(merchantUserQueryDTO);
        if (!merchantUser.getData()) {
            return Result.FAIL("不存在绑定关系");
        }

        // fill data
        sessionCache.setShopMan(true);
        sessionCache.setShopId4ShopMan(shopId);
        sessionCache.setUserId4ShopMan(merchantId);

        String domainName = request.getParameter(CheckSignFilter.PARAM_NAME_APPID);
        String sessionId = request.getParameter(CheckSignFilter.PARAM_NAME_SESSIONID);
        sessionService.setSession(domainName, sessionId, sessionCache);

        return Result.SUCCESS();
    }
}
