package com.chl.victory.web.service;

import javax.annotation.Resource;

import com.chl.victory.common.redis.CacheService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.merchant.model.MerchantUserDTO;
import com.chl.victory.serviceapi.merchant.query.MerchantUserQueryDTO;
import com.chl.victory.webcommon.manager.RpcManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author ChenHailong
 * @date 2019/4/22 17:05
 **/
@Service
public class LoginService {

    @Resource
    RSAService rsaService;

    @Resource
    CacheService cacheService;

    public String genPublicK(String sessionId) {
        return rsaService.genPublicKey(sessionId);
    }

    public ServiceResult<MerchantUserDTO> verifyLogin(String cryptographicName, String cryptographicPass,
            String sessionId) {
        String name = rsaService.deCrypto(cryptographicName, sessionId);
        if (!NumberUtils.isDigits(name)) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "用户名或密码错误");
        }
        String pass = rsaService.deCrypto(cryptographicPass, sessionId);
        // 数据库查询是否匹配
        //boolean result = mock(name, pass);

        MerchantUserQueryDTO userQuery = new MerchantUserQueryDTO();
        userQuery.setMobile(NumberUtils.toLong(name));
        ServiceResult<MerchantUserDTO> serviceResult = RpcManager.merchantFacade.selectUser(userQuery);
        if (!serviceResult.getSuccess() || serviceResult.getData() == null) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "用户名不存在");
        }

        boolean result = rsaService.deCrypto(serviceResult.getData().getPass()).equals(pass);

        if (result) {
            serviceResult.getData().setPass(null);
            MerchantUserDTO merchantUserDTO = new MerchantUserDTO();
            BeanUtils.copyProperties(serviceResult.getData(), merchantUserDTO);
            return ServiceResult.success(merchantUserDTO);
        }

        return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "用户名或密码错误");
    }

    private boolean mock(String name, String pass) {
        String _name = "chlName";
        String _pass = "i4fMalYhWzWSMOdO2rD3KoVKKU+d0HgEsFeV/EI4iHAyHiTFHJm3Ns0sJ4IZzVxdRieH1QR6ncyeNfjP30819Us7s/j6bkHM/e5Lvorf/7JeNaZX7vtrtHH1nOocf6BlecGNOh7l+PM+4Byc9iLfwJdPZIY4qvrvvpAs319vFTQ=";

        return _name.equals(name) && rsaService.deCrypto(_pass).equals(pass);
    }

    public boolean isLogin(String tokenLast, String sessinId) {
        String cacheTokenLast = cacheService.get("loginToken" + sessinId);
        return StringUtils.isNotBlank(cacheTokenLast) && (tokenLast.equals(cacheTokenLast)
                /*|| System.currentTimeMillis() - NumberUtils.toLong(cacheTokenLast.split("_")[1]) < 500*/);
    }
}
