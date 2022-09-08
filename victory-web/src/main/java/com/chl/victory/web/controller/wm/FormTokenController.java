package com.chl.victory.web.controller.wm;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chl.victory.web.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
@RequestMapping("/p/wm/ft")
public class FormTokenController {

    @Resource
    HttpServletResponse httpServletResponse;

    @Resource
    HttpServletRequest httpServletRequest;

    /**
     * 生成form token，采用header Host Referer Page
     * @return
     */
    @GetMapping(path = "g", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result gen() {
        String host = httpServletRequest.getHeader("Host");
        String referer = httpServletRequest.getHeader("Referer");

        if (StringUtils.isBlank(host) || StringUtils.isBlank(referer)) {
            return Result.FAIL("参数错误");
        }

        int hashCode = (host + referer + System.currentTimeMillis()).hashCode();

        return Result.SUCCESS(hashCode);
    }
}
