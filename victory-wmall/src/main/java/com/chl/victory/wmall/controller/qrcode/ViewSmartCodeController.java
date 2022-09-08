package com.chl.victory.wmall.controller.qrcode;

import java.io.IOException;
import java.net.URI;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.localservice.SmartCodeLocalService.SmartEle;
import com.chl.victory.webcommon.util.CookieUtil;
import com.chl.victory.wmall.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.localservice.manager.LocalServiceManager.smartCodeLocalService;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/viewSmartCode/")
@Slf4j
public class ViewSmartCodeController {

    @Resource
    HttpServletResponse httpServletResponse;
    @Resource
    HttpServletRequest httpServletRequest;

    @Resource
    CacheService cacheService;

    SmartEle getGoodEleCache(String smartCodeId){
        SmartEle ele = cacheService.get(CacheKeyPrefix.SMART_CODE_GOOD_ELE + smartCodeId, SmartEle.class);
        return ele;
    }
    void goodEleCache(String smartCodeId, SmartEle ele){
        cacheService.save(CacheKeyPrefix.SMART_CODE_GOOD_ELE + smartCodeId, ele, 30);
    }

    SmartEle getGoodEle(String smartCodeId) {
        SmartEle ele = getGoodEleCache(smartCodeId);
        if (null == ele) {
            ele = smartCodeLocalService.getSmartCodeGoodEle(smartCodeId);
            if (null == ele || StringUtils.isBlank(ele.getImg())) {
                return null;
            }

            ele.setShowCount(null);
            ele.setShowMax(null);
            ele.setSort(null);
            goodEleCache(smartCodeId, ele);
        }
        return ele;
    }

    private final String cookieName4CurrentSCId = "currentSCId";
    private final String cookieName4CurrentEId = "currentEId";
    /**
     * 输出活码当前优先级最高的元素的码
     * @param id
     * @throws Exception
     */
    @GetMapping("code")
    public void viewSmartCode(@RequestParam("id") String id) throws Exception {
        SmartEle ele = getGoodEle(id);
        if (null == ele) {
            return;
        }

        String codeImg = ele.getImg();
        String url = getImgUrl(codeImg);

        byte[] bytes = IOUtils.toByteArray(new URI(url));
        try {
            Cookie cookie = new Cookie(cookieName4CurrentSCId, id);
            httpServletResponse.addCookie(cookie);
            cookie = new Cookie(cookieName4CurrentEId, ele.getId());

            httpServletResponse.addCookie(cookie);
            httpServletResponse.setContentType("image/jpeg");
            httpServletResponse.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出活码当前优先级最高的元素的title
     * @param id
     * @return
     * @throws Exception
     */
    @GetMapping("title")
    public Result viewSmartTitle(@RequestParam("id") String id) throws Exception {
        Cookie cookie4SCId = CookieUtil.getCookie(httpServletRequest, cookieName4CurrentSCId);
        if (!id.equals(cookie4SCId.getValue())) {
            return Result.FAIL("参数错误");
        }

        SmartEle ele = getGoodEle(id);
        if (null == ele) {
            return Result.FAIL();
        }

        return Result.SUCCESS(ele.getTitle());
    }

    /**
     * 增加子码的查看次数
     * @return
     */
    @GetMapping("increViewCode")
    public Result increViewCode() {
        Cookie cookie4EleId = CookieUtil.getCookie(httpServletRequest, cookieName4CurrentEId);
        Cookie cookie4SCId = CookieUtil.getCookie(httpServletRequest, cookieName4CurrentSCId);
        if (null == cookie4EleId || null == cookie4SCId || StringUtils.isBlank(cookie4EleId.getValue()) || StringUtils.isBlank(cookie4SCId.getValue())) {
            return Result.FAIL("参数错误");
        }

        smartCodeLocalService.increEleShowCount(cookie4SCId.getValue(), cookie4EleId.getValue());

        return Result.SUCCESS();
    }

    private String getImgUrl(String ownerImg) {
        return "https://wmall.5jym.com/img/" + ownerImg;
    }
}