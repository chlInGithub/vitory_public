package com.chl.victory.web.controller.wm.img;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.chl.victory.imgservice.ZimgService;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.merchant.model.ShopImgDTO;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;
import static com.chl.victory.webcommon.manager.RpcManager.merchantImgFacade;

@Controller
@RequestMapping("/p")
public class ImgController {

    @Resource
    ZimgService zimgService;

    @PostMapping(path = "/wm/img/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result uploadImg(@RequestParam("imgFile") MultipartFile imgFile) throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        Long shopId = sessionCache.getShopId();
        ServiceResult<Integer> result = merchantImgFacade.totalSizeImg(shopId);
        if (result.getSuccess() && result.getData() != null) {
            accessLimitFacade.doAccessLimit(shopId, result.getData(), AccessLimitTypeEnum.WM_SHOP_IMG_SIZE_TOTAL,
                    AccessLimitTypeEnum.WM_SHOP_IMG_SIZE_TOTAL.getDesc());
        }

        String md5 = zimgService.uploadImage(imgFile);
        if (!StringUtils.isEmpty(md5)) {
            // ???????????????????????????shop?????????????????????????????????????????????????????????????????????????????????
            ShopImgDTO shopImgDTO = new ShopImgDTO();
            shopImgDTO.setImgId(md5);
            shopImgDTO.setShopId(sessionCache.getShopId());
            shopImgDTO.setOperatorId(sessionCache.getUserId());
            shopImgDTO.setSize(imgFile.getSize());
            ServiceResult serviceResult = merchantImgFacade.saveImg(shopImgDTO);
            if (serviceResult.getSuccess()) {
                return Result.SUCCESS(md5);
            }
            // ????????????????????????????????????????????????????????????????????????????????????ID?????????
            ServiceResult<Integer> countImg = merchantImgFacade.countImg(null, md5);
            if (countImg.getSuccess() && countImg.getData() != null && countImg.getData() == 0) {
                zimgService.deleteImage(md5);
            }
            return Result.FAIL(serviceResult.getMsg());
        }
        return Result.FAIL("??????????????????");
    }

    @PostMapping(path = "/wm/img/del", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result delImg(@RequestParam("id") String imgId, @RequestParam(name = "all", required = false) Integer all)
            throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        ServiceResult<Integer> countImg = merchantImgFacade.countImg(sessionCache.getShopId(), imgId);
        if (!countImg.getSuccess() || countImg.getData() == null) {
            return Result.FAIL("????????????");
        }
        if (countImg.getData() < 1) {
            return Result.SUCCESS(true);
        }

        if (all == null || all != 1) {
            // ??????????????????imgid?????????????????????shop-img??????
            countImg = merchantImgFacade.countImg(null, imgId);
            if (!countImg.getSuccess() || countImg.getData() == null) {
                return Result.FAIL("????????????");
            }
            // ????????????????????????????????????1???????????????shop-img??????
            ServiceResult<Integer> usedNumResult = merchantImgFacade.getUsedNum(sessionCache.getShopId(), imgId);
            if (!usedNumResult.getSuccess() || usedNumResult.getData() == null) {
                return Result.FAIL("????????????");
            }

            if (countImg.getData() > 1 || usedNumResult.getData() > 1) {
                ServiceResult<Boolean> delResult = merchantImgFacade.delOnlyImg(sessionCache.getShopId(), imgId);
                if (delResult.getSuccess()) {
                    return Result.SUCCESS(true);
                }
                return Result.FAIL(delResult.getMsg());
            }
        }

        // ??????????????????
        boolean result = zimgService.deleteImage(imgId);
        if (result) {
            // ?????????????????????????????????????????????????????????????????????
            ServiceResult<Boolean> delResult = merchantImgFacade.delImg(sessionCache.getShopId(), imgId);
            if (delResult.getSuccess() && delResult.getData()) {
                return Result.SUCCESS(true);
            }
            return Result.FAIL(delResult.getMsg());
        }
        return Result.FAIL("????????????");
    }

    @GetMapping(path = "/wm/img/query", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result query(@RequestParam(name = "picId", required = false) String picId,
            @RequestParam(name = "lastId", required = false) Long lastId) throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        Long shopId = sessionCache.getShopId();

        List<ShopImgDTO> shopImgDTOS = new ArrayList<>();
        if (!StringUtils.isEmpty(picId)) {
            ServiceResult<List<ShopImgDTO>> imgResult = merchantImgFacade.queryImgs(shopId, picId);
            shopImgDTOS.addAll(imgResult.getData());
        }
        else {
            ServiceResult<List<ShopImgDTO>> imgResult = merchantImgFacade.query4ImgMan(shopId, lastId);
            shopImgDTOS.addAll(imgResult.getData());
        }

        List<PicVO> picVOS = Collections.EMPTY_LIST;
        if (!CollectionUtils.isEmpty(shopImgDTOS)) {
            picVOS = shopImgDTOS.stream().map(item -> {
                PicVO picVO = new PicVO(item.getId(), item.getImgId(), item.getSize().intValue(), 0);
                return picVO;
            }).collect(Collectors.toList());
        }

        ServiceResult<Integer> countResult = merchantImgFacade.countImg(shopId, null);
        Integer imgCount = countResult.getData() != null ? countResult.getData() : 0;

        ServiceResult<Integer> totalResult = merchantImgFacade.totalSizeImg(shopId);
        Integer imgTotalSize = totalResult.getData() != null ? totalResult.getData() : 0;

        PicManQueryVO picManQueryVO = new PicManQueryVO(picVOS, imgCount, imgTotalSize);

        return Result.SUCCESS(picManQueryVO);
    }

    @Data
    @AllArgsConstructor
    public static class PicManQueryVO {

        List<PicVO> pics;

        Integer count;

        Integer total;
    }

    @Data
    @AllArgsConstructor
    public static class PicVO {

        Long id;

        String picId;

        Integer size;

        Integer usedPlaces;
    }
}
