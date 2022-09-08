package com.chl.victory.wmall.controller.qrcode;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.util.PatternUtil;
import com.chl.victory.core.qrcode.QRCodeGenerator;
import com.chl.victory.core.util.ImageUtils;
import com.chl.victory.imgservice.ZimgService;
import com.chl.victory.imgservice.ZimgUploadException;
import com.chl.victory.localservice.SmartCodeLocalService.SmartCode;
import com.chl.victory.localservice.SmartCodeLocalService.SmartEle;
import com.chl.victory.localservice.SmartCodePowerLocalService;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.webcommon.service.SessionService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.chl.victory.localservice.manager.LocalServiceManager.smartCodeLocalService;
import static com.chl.victory.localservice.manager.LocalServiceManager.smartCodePowerLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.memberFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/qrcode/")
@Api(description = "二维码")
@Slf4j
public class QrcodeController {

    @Resource
    ZimgService zimgService;

    @Resource
    HttpServletResponse httpServletResponse;

    @Resource
    HttpServletRequest httpServletRequest;

    /**
     * 活码基本信息列表
     * @return
     */
    @GetMapping("smartCodeList")
    public Result<List<SmartCodeVO>> smartCodeList() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        List<SmartCode> smartCodes = smartCodeLocalService.getSmartCodes(userId);
        if (CollectionUtils.isEmpty(smartCodes)) {
            return Result.SUCCESS();
        }

        List<SmartCodeVO> vos = smartCodes.stream().map(smartCode -> {
            SmartCodeVO smartCodeVO = new SmartCodeVO();
            BeanUtils.copyProperties(smartCode, smartCodeVO);
            return smartCodeVO;
        }).collect(Collectors.toList());

        return Result.SUCCESS(vos);
    }

    /**
     * 活码子元素列表
     * @param id
     * @return
     */
    @GetMapping("smartCodeEles")
    public Result<List<SmartEle>> smartCodeEles(@RequestParam("id") String id) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        List<SmartEle> smartEles = smartCodeLocalService.getSmartEles(userId, id);

        return Result.SUCCESS(smartEles);
    }

    @GetMapping("smartCode")
    public Result<SmartCodeVO> smartCode(@RequestParam("id") String id) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        SmartCode smartCode = smartCodeLocalService.getSmartCode(userId, id);
        List<SmartEle> smartEles = smartCodeLocalService.getSmartEles(userId, id);

        SmartCodeVO smartCodeVO = new SmartCodeVO();
        BeanUtils.copyProperties(smartCode, smartCodeVO);
        smartCodeVO.setEles(smartEles);

        return Result.SUCCESS(smartCodeVO);
    }

    @GetMapping("delSmartCode")
    public Result delSmartCode(@RequestParam("id") String id) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        SmartCode smartCode = smartCodeLocalService.getSmartCode(userId, id);
        if (null == smartCode) {
            return Result.SUCCESS();
        }

        List<SmartEle> smartEles = smartCodeLocalService.getSmartEles(userId, id);
        smartCodeLocalService.delSmartCode(userId, id);

        try {
            if (null != smartCode) {
                if (StringUtils.isNotBlank(smartCode.getOwnerImg())) {
                    zimgService.deleteImage(smartCode.getOwnerImg());
                }
                if (StringUtils.isNotBlank(smartCode.getCodeImg())) {
                    zimgService.deleteImage(smartCode.getCodeImg());
                }
            }
            if (!CollectionUtils.isEmpty(smartEles)) {
                for (SmartEle smartEle : smartEles) {
                    if (StringUtils.isNotBlank(smartEle.getImg())) {
                        zimgService.deleteImage(smartEle.getImg());
                    }
                }
            }
        } catch (Exception e) {
        }

        return Result.SUCCESS();
    }

    @GetMapping("delSmartCodeEle")
    public Result delSmartCodeEle(@RequestParam("smartCodeId") String smartCodeId, @RequestParam("id") String id) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        SmartEle smartEle = smartCodeLocalService.getSmartEle(userId, smartCodeId, id);
        if (null == smartEle) {
            return Result.SUCCESS();
        }

        smartCodeLocalService.delSmartEle(userId, smartCodeId, id);

        try {
            if (null != smartEle && StringUtils.isNotBlank(smartEle.getImg())) {
                zimgService.deleteImage(smartEle.getImg());
            }
        } catch (Exception e) {

        }

        return Result.SUCCESS();
    }

    @Data
    public static class DelImg{
        @NotEmpty(message = "缺少tupianID")
        String img;

        @NotEmpty(message = "缺少ID")
        String id;

        String parentId;

        /**
         * 1: smartCode 2: codeEle
         */
        @NotNull(message = "缺少类型")
        Integer type;
    }
    @GetMapping("delImg")
    public Result delSmartCodeEle(DelImg delImg) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        if (delImg.equals(1)) {
            SmartCode smartCode = smartCodeLocalService.getSmartCode(userId, delImg.getId());
            if (null == smartCode || !delImg.getImg().equals(smartCode.getCodeImg())) {
                return Result.SUCCESS();
            }
        }else if (delImg.equals(2)) {
            SmartEle smartEle = smartCodeLocalService.getSmartEle(userId, delImg.getParentId(), delImg.getId());
            if (null == smartEle || !delImg.getImg().equals(smartEle.getImg())) {
                return Result.SUCCESS();
            }
        }

        try {
            zimgService.deleteImage(delImg.getImg());
        } catch (Exception e) {

        }

        return Result.SUCCESS();
    }

    /**
     * 生成活码ID
     */
    @GetMapping("smartCodeId")
    public Result<String> newSmartCode() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        Long count = smartCodeLocalService.countSmartCodes(userId);
        if (count > 5) {
            return Result.FAIL("您当前只可拥有5个活码");
        }

        String id = "" + System.nanoTime();

        return Result.SUCCESS(id);
    }

    @PostMapping(value = "saveSmartCode", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result saveSmartCode(SaveSmartCodeVO smartCodeVO) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        SmartCodePowerLocalService.SmartCodePower smartCodePower = smartCodePowerLocalService.getPower(userId);
        Integer codeCountMax = smartCodePower.getCodeCountLimit().getMax();
        Integer eleCountMax = smartCodePower.getEleCountLimit().getMax();

        SmartCode smartCodeCache = smartCodeLocalService.getSmartCode(userId, smartCodeVO.getId());
        if (null == smartCodeCache) {
            Long count = smartCodeLocalService.countSmartCodes(userId);
            if (count >= codeCountMax) {
                return Result.FAIL("LIMIT 当前只可拥有" + codeCountMax + "个活码");
            }
        }

        String smartCodeId = smartCodeVO.getId();
        SmartCode smartCode = new SmartCode();
        BeanUtils.copyProperties(smartCodeVO, smartCode);
        List<SmartEle> eles = smartCodeVO.getEles();
        if (CollectionUtils.isEmpty(eles)) {
            return Result.FAIL("缺少元素");
        }
        if (eles.size() > eleCountMax) {
            return Result.FAIL("LIMIT 目前每个活码只可配置" + eleCountMax + "个子码");
        }

        String ownerImgCache = smartCodeCache == null ? "" : smartCodeCache.getOwnerImg();

        String md5 = smartCodeCache == null ? "" : smartCodeCache.getCodeImg();
        boolean existCodeImg = StringUtils.isNotBlank(smartCode.getCodeImg());
        boolean changeOwnerImg = !ownerImgCache.equals(smartCode.getOwnerImg());
        if (!existCodeImg || changeOwnerImg) {
            String content = "https://smartcode.5jym.com/wmall/qrcode/smartCode.html?id=" + smartCodeId;
            byte[] generate;
            if (StringUtils.isBlank(smartCode.getOwnerImg())) {
                generate = QRCodeGenerator.generate(content);
            }
            else {
                String imgUrl = getImgUrl(smartCode.getOwnerImg());
                byte[] bytes;
                try {
                    bytes = IOUtils.toByteArray(new URI(imgUrl));
                } catch (Exception e) {
                    return Result.FAIL("读取中间图片失败");
                }
                generate = QRCodeGenerator.generate(content, bytes);
            }

            try {
                md5 = zimgService.uploadImage(generate);
            } catch (ZimgUploadException e) {
                return Result.FAIL("存储活码图片失败");
            }
            smartCode.setCodeImg(md5);
        }

        smartCodeLocalService.saveSmartCode(userId, smartCode);
        smartCodeLocalService.saveSmartEles(userId, smartCodeId, eles);

        return Result.SUCCESS(md5);
    }

    private String getImgUrl(String ownerImg) {
        return "https://wmall.5jym.com/img/" + ownerImg;
    }

    @PostMapping("upload")
    public Result<String> update(@RequestParam("file") MultipartFile file, @RequestParam("token") String token,
            @RequestParam("sessionId") String sessionId) {
        /*SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();*/

        // todo limit

        // watermark
        byte[] bytes;
        try {
            Image image = ImageUtils.gen(file.getInputStream(), System.currentTimeMillis() + "");
            bytes = ImageUtils.getBytes(image);
        } catch (Exception e) {
            return Result.FAIL("图片处理失败");
        }

        String md5;
        try {
            md5 = zimgService.uploadImage(bytes);
        } catch (Exception e) {
            return Result.FAIL("上传失败");
        }

        // todo deal data

        return Result.SUCCESS(md5);
    }

    /**
     * 生成二维码
     * @return
     */
    @GetMapping("gen")
    public void gen(@RequestParam("text") String text) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        // TODO limit

        /*boolean match = PatternUtil.isUrl(text);
        if (!match) {
            log.error("URL格式错误{}", text);
            return;
        }*/

        byte[] bytes = QRCodeGenerator.generate(text);
        log.error("byte size {}", bytes.length);
        try {
            httpServletResponse.setContentType("image/jpeg");
            httpServletResponse.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取活码的admin联系人二维码图片
     */
    @GetMapping("admin")
    public Result<String> admin() {
        String md5 = "82942aca38a7985a37adb1e8aac340c8";

        return Result.SUCCESS(md5);
    }

    /**
     * 博予科技官网二维码
     * @return
     */
    @GetMapping("by")
    public Result<String> by() {
        String md5 = "9b53e134673d5f948e6d258573cc2460";

        return Result.SUCCESS(md5);
    }

    @Resource
    SessionService sessionService;

    @PostMapping(value = "userInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result userInfo(@RequestParam("mobile") Long mobile, @RequestParam("thirdNick") String thirdNick) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long userId = sessionCache.getUserId();

        ShopMemberDTO shopMember = new ShopMemberDTO();
        shopMember.setShopId(sessionCache.getShopId());
        shopMember.setId(userId);
        shopMember.setNick(thirdNick);
        shopMember.setMobile(mobile);
        shopMember.setThirdId(sessionCache.getThirdId());
        shopMember.setOpenId(sessionCache.getThirdOpenId());

        ServiceResult serviceResult = memberFacade.save(shopMember);
        if (serviceResult.getSuccess()) {
            sessionCache.setMobile(mobile.toString());
            sessionCache.setThirdNick(thirdNick);
            sessionService.setSession(sessionCache.getThirdOpenId(), sessionCache.getKey(), sessionCache);

            return Result.SUCCESS();
        }

        return Result.FAIL(serviceResult.getMsg());
    }

    @Data
    public static class SmartCodeVO extends SmartCode {

        /**
         * 可选元素列表
         */
        List<SmartEle> eles;
    }

    @Data
    public static class SaveSmartCodeVO extends SmartCode {

        /**
         * 可选元素列表
         */
        String eles;

        public List<SmartEle> getEles() {
            if (StringUtils.isNotBlank(eles)) {
                return JSONObject.parseArray(eles, SmartEle.class);
            }
            return null;
        }
    }
}