package com.chl.victory.service.services.share;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.service.services.share.shareredirecthandler.ShareRedirectDispatcher;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.share.model.ShareDTO;
import com.chl.victory.serviceapi.share.model.ShareRecentDTO;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import com.chl.victory.serviceapi.weixin.model.WeixinACodeParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.itemService;
import static com.chl.victory.service.services.ServiceManager.shopPowerService;

/**
 * @author ChenHailong
 * @date 2020/8/3 18:30
 **/
@Service
@Validated
public class ShareService {

    /**
     * 分享店铺  de 分享图片
     */
    Map<String, ShareImgsCache> shareAppMapCode = Collections.synchronizedMap(new LinkedHashMap<>(10, 2, true));

    @Resource
    ShareRedirectDispatcher shareRedirectDispatcher;

    public void addGainUser(@NotNull Long shopId, @NotNull Long shareUserId, @NotNull String scene,
            @NotNull Long gainUserId) {
        if (shareUserId.equals(gainUserId)) {
            return;
        }
        // 判断gainUserId对应的记录是最近5分钟内新建的
        boolean newLastFiveMin = ServiceManager.memberService.newLastXMin(shopId, gainUserId, 5);
        if (!newLastFiveMin) {
            return;
        }

        // 增加分享记录获客人数
        String key = CacheKeyPrefix.SHARE_SHOP_USER_SHARE_GAINUSER_SET + shopId + CacheKeyPrefix.SEPARATOR + shareUserId
                + CacheKeyPrefix.SEPARATOR + scene;
        cacheService.sAdd(key, gainUserId);
    }

    /**
     * 用户某次分享的获客数量
     * @param shopId
     * @param userId
     * @param scene
     * @return
     */
    public Long countGainUser(@NotNull Long shopId, @NotNull Long userId, @NotNull String scene) {
        String key = CacheKeyPrefix.SHARE_SHOP_USER_SHARE_GAINUSER_SET + shopId + CacheKeyPrefix.SEPARATOR + userId
                + CacheKeyPrefix.SEPARATOR + scene;
        return cacheService.sCard(key);
    }

    /**
     * 分享信息的近况
     * @param userId
     * @param shopId
     * @return
     */
    public List<ShareRecentDTO> getShareRecents(Long userId, Long shopId) {
        List<ShareTemp> userShares = getUserShare(shopId, userId);
        if (CollectionUtils.isEmpty(userShares)) {
            return null;
        }

        List<ShareRecentDTO> shareRecentDTOS = userShares.stream().map(item -> {
            Long gainUserCount = countGainUser(shopId, userId, item.scene);
            ShareRecentDTO shareRecentDTO = new ShareRecentDTO();
            BeanUtils.copyProperties(item, shareRecentDTO);
            shareRecentDTO.setGainCount(gainUserCount);
            return shareRecentDTO;
        }).collect(Collectors.toList());

        return shareRecentDTOS;
    }

    public String shareRedirect(Long userId, Long shopId, String scene) {
        if (scene.equals("0")) {
            return null;
        }

        return shareRedirectDispatcher.redirect(userId, shopId, scene);
    }

    /**
     * 访问分享，涉及统计行为
     * @param userId
     * @param shopId
     * @param scene
     */
    public void visitShare(Long userId, Long shopId, String scene) {
        if (scene.equals("0")) {
            return;
        }

        shareRedirectDispatcher.visitShare(userId, shopId, scene);
    }

    /**
     * 用户+商品 ：分享者
     * @param userId
     * @param itemId
     * @param shareUserId
     */
    public void saveUserItemAndSharer(Long userId, Long itemId, Long shareUserId) {
        String key = CacheKeyPrefix.SHARE_ITEM_RELATIONSHIP_HASH;
        String field = userId + CacheKeyPrefix.SEPARATOR + itemId;
        String val = shareUserId.toString();
        ServiceManager.cacheService.hSet(key, field, val);
        cacheService.expire(key, CacheExpire.DAYS_30);
    }

    /**
     * 用户+商品 对应的 分享者
     * @param userId
     * @param itemId
     * @return
     */
    public Long getSharer(Long userId, Long itemId) {
        String key = CacheKeyPrefix.SHARE_ITEM_RELATIONSHIP_HASH;
        String field = userId + CacheKeyPrefix.SEPARATOR + itemId;
        String s = ServiceManager.cacheService.hGet(key, field, String.class);
        if (StringUtils.isNotBlank(s) && NumberUtils.isDigits(s)) {
            return NumberUtils.toLong(s);
        }
        return null;
    }

    public void incrShare(@NotNull Long shopId, String appId, Long userId, Long itemId) {
        String key = CacheKeyPrefix.SHARE_COUNT_HASH;
        // shop
        String field = shopId.toString();
        ServiceManager.cacheService.hIncrement(key, field, 1);
        // shop app
        /*if (StringUtils.isNotBlank(appId)) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + appId;
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
        // shop user
        /*if (userId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + userId.toString();
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
        // shop app user
        /*if (StringUtils.isNotBlank(appId) && userId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + appId + CacheKeyPrefix.SEPARATOR + userId.toString();
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
        // shop item
        /*if (itemId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + itemId;
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
        // shop item user
        /*if (itemId != null && userId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + itemId + CacheKeyPrefix.SEPARATOR + userId;
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
    }

    public void incrWeiSalesShare(@NotNull Long shopId, String appId, Long userId, Long itemId) {
        String key = CacheKeyPrefix.WEISALES_SHARE_COUNT_HASH;
        // shop
        String field = shopId.toString();
        ServiceManager.cacheService.hIncrement(key, field, 1);
        // shop app
        /*if (StringUtils.isNotBlank(appId)) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + appId;
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
        // shop user
        if (userId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + userId.toString();
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }
        // shop app user
        /*if (StringUtils.isNotBlank(appId) && userId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + appId + CacheKeyPrefix.SEPARATOR + userId.toString();
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
        // shop item
        /*if (itemId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + itemId;
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
        // shop item user
        /*if (itemId != null && userId != null) {
            field = shopId.toString() + CacheKeyPrefix.SEPARATOR + itemId + CacheKeyPrefix.SEPARATOR + userId;
            ServiceManager.cacheService.hIncrement(key, field, 1);
        }*/
    }

    /**
     * 分享商品
     * @return
     * @throws BusServiceException
     */
    public ServiceResult<ShareDTO> shareItem(ShareDTO shareParam) throws Exception {
        Long shopId = shareParam.getShopId();

        // 微导购功能权限
        boolean weiSales = shopPowerService.hasValidWeiSales(shopId);

        // 检查限流
        AccessLimitTypeEnum accessLimitTypeEnum = weiSales ?
                AccessLimitTypeEnum.WMALL_SHOP_WEISALES_SHARE :
                AccessLimitTypeEnum.WMALL_SHOP_SHARE;
        ServiceManager.accessLimitService
                .checkAccessLimit(shopId, accessLimitTypeEnum, null, accessLimitTypeEnum.getDesc());

        String appId = shareParam.getAppId();

        // check app
        ShopAppDO shopAppDO = ServiceManager.merchantService.selectShopAppWithValidate(shopId, appId);

        ShareDTO shareDTOCache = getShareImgCache(weiSales, shareParam);
        if (null != shareDTOCache) {
            return ServiceResult.success(shareDTOCache);
        }

        Long itemId = shareParam.getItemId();
        Long userId = shareParam.getUserId();

        WeixinACodeParam weixinACodeParam = prepareWeixinACodeParam(weiSales, shareParam);

        byte[] bytes = genShareImage(weiSales, shopAppDO, shareParam, weixinACodeParam);

        // add local cache
        addShareImgsCache(weiSales, shareParam, bytes);

        // 增加限流
        ServiceManager.accessLimitService.incrAccessLimit(shopId, accessLimitTypeEnum, null);

        // 计数
        ServiceManager.shareService.incrShare(shopAppDO.getShopId(), shopAppDO.getAppId(), userId, itemId);
        if (weiSales) {
            ServiceManager.shareService.incrWeiSalesShare(shopAppDO.getShopId(), shopAppDO.getAppId(), userId, itemId);
        }

        ShareDTO shareDTO = new ShareDTO();
        shareDTO.setImgBytes(bytes);

        if (weiSales && null != weixinACodeParam) {
            addUserShare(shopId, userId, weixinACodeParam.getScene());
        }

        return ServiceResult.success(shareDTO);
    }

    /**
     * 生成分享图片
     * @param weiSales
     * @param shopAppDO
     * @param shareParam
     * @param weixinACodeParam
     * @return
     * @throws Exception
     */
    private byte[] genShareImage(boolean weiSales, ShopAppDO shopAppDO, ShareDTO shareParam,
            WeixinACodeParam weixinACodeParam) throws Exception {
        byte[] bytes = ServiceManager.weixinMiniProgramService.genCode(shopAppDO, weixinACodeParam);

        if (weiSales) {
            ShareUtils.ShareItemParam shareItemParam = new ShareUtils.ShareItemParam();
            shareItemParam.code = bytes;
            shareItemParam.imgKey = weixinACodeParam.getShortUrlDTO().getItemImg();
            shareItemParam.nick = shareParam.getNick();
            shareItemParam.title = weixinACodeParam.getShortUrlDTO().getItemTitle();
            shareItemParam.userImg = shareParam.getUserImg();
            bytes = ShareUtils.getShareImg4Item(shareItemParam);
        }
        else {
            ShareUtils.ShareItemParam shareItemParam = new ShareUtils.ShareItemParam();
            shareItemParam.code = bytes;
            shareItemParam.title = shareParam.getShopName();
            shareItemParam.nick = shareParam.getNick();
            shareItemParam.userImg = shareParam.getUserImg();
            bytes = ShareUtils.getShareImg4Shop(shareItemParam);
        }

        return bytes;
    }

    private void addShareImgsCache(boolean weiSales, ShareDTO shareParam, byte[] bytes) {
        String key = getShareImgCacheKey(weiSales, shareParam);
        shareAppMapCode.put(key, new ShareImgsCache(System.currentTimeMillis() + CacheExpire.HOUR_1 * 1000, bytes));
    }

    /**
     * 组织生成微信小程序码的参数
     * @param weiSales
     * @param shareParam
     * @return
     * @throws BusServiceException
     */
    private WeixinACodeParam prepareWeixinACodeParam(boolean weiSales, ShareDTO shareParam) throws BusServiceException {
        if (weiSales) {
            Long itemId = shareParam.getItemId();
            Long userId = shareParam.getUserId();
            Long shopId = shareParam.getShopId();

            @NotNull ItemQueryDTO itemQueryDTO = new ItemQueryDTO();
            itemQueryDTO.setShopId(shopId);
            itemQueryDTO.setId(itemId);
            itemQueryDTO.setNeedFillCacheData(false);
            itemQueryDTO.setJustOutline(true);
            ServiceResult<List<ItemDTO>> listServiceResult = itemService.selectItems(itemQueryDTO);
            if (CollectionUtils.isEmpty(listServiceResult.getData()) || listServiceResult.getData().get(0) == null) {
                throw new BusServiceException(ServiceFailTypeEnum.NOT_EXIST.getDesc());
            }

            ItemDTO itemDTO = listServiceResult.getData().get(0);
            String itemImg = itemDTO.getFirstImg();
            if (StringUtils.isBlank(itemImg)) {
                throw new BusServiceException("商品缺少首图");
            }
            String itemTitle = itemDTO.getTitle();

            // 生成短字符串:分享模型(店铺ID 用户ID 商品ID 分享时间)
            ShortUrlDTO shortUrlDTO = new ShortUrlDTO();
            shortUrlDTO.setShopId(shopId);
            shortUrlDTO.setTime(new Date());
            shortUrlDTO.setUserId(userId);
            shortUrlDTO.setItemId(itemId);
            shortUrlDTO.setItemTitle(itemTitle);
            shortUrlDTO.setItemImg(itemImg);
            shortUrlDTO.setType(ShortUrlDTO.ModelType.share_item.getCode());
            String shortStr = ServiceManager.shortUrlService.save(shortUrlDTO);

            WeixinACodeParam weixinACodeParam = new WeixinACodeParam();
            weixinACodeParam.setShortUrlDTO(shortUrlDTO);
            weixinACodeParam.setScene(shortStr);

            return weixinACodeParam;
        }

        return null;
    }

    /**
     * 分享的图片 本地cache
     * @param weiSales
     * @param shareParam
     * @return
     */
    private ShareDTO getShareImgCache(boolean weiSales, ShareDTO shareParam) {
        String key = getShareImgCacheKey(weiSales, shareParam);

        ShareImgsCache cache = shareAppMapCode.get(key);

        if (null != cache) {
            byte[] bytes = cache.bytes;
            if (cache.expireMs < System.currentTimeMillis() || null == bytes || bytes.length <= 200) {
                shareAppMapCode.remove(key);
            }
            else {
                ShareDTO shareDTO = new ShareDTO();
                shareDTO.setImgBytes(bytes);
                return shareDTO;
            }
        }

        return null;
    }

    private String getShareImgCacheKey(boolean weiSales, ShareDTO shareParam) {
        String key;
        if (weiSales) {
            key = shareParam.getAppId() + CacheKeyPrefix.SEPARATOR + shareParam.getUserId() + CacheKeyPrefix.SEPARATOR
                    + shareParam.getItemId();
        }
        else {
            key = shareParam.getAppId();
        }
        return key;
    }

    /**
     * 添加到用户分享的短字符串集合
     * @param shopId
     * @param userId
     * @param scene
     */
    private void addUserShare(Long shopId, Long userId, String scene) {
        String key = CacheKeyPrefix.SHARE_SHOP_USER_SHARE_SET + shopId + CacheKeyPrefix.SEPARATOR + userId;
        double score = Double.valueOf(DateFormatUtils.format(new Date(), DateConstants.format2));
        CacheService.@NotNull ZSetEle<String> tzSetEle = new CacheService.ZSetEle<String>(scene, score);
        cacheService.zAdd(key, tzSetEle);
        cacheService.expire(key, CacheExpire.DAYS_30);
    }

    /**
     * 用户分享的短字符串集合
     * @param shopId
     * @param userId
     * @return
     */
    public List<ShareTemp> getUserShare(Long shopId, Long userId) {
        String key = CacheKeyPrefix.SHARE_SHOP_USER_SHARE_SET + shopId + CacheKeyPrefix.SEPARATOR + userId;
        Set<CacheService.ZSetEle<String>> zSetEles = cacheService.zScan(key, String.class);
        if (CollectionUtils.isEmpty(zSetEles)) {
            return Collections.EMPTY_LIST;
        }

        List<ShareTemp> shareTemps = zSetEles.stream().sorted((o1, o2) -> o1.getScore() > o2.getScore() ? -1 : 0)
                .map(item -> {
                    ShareTemp shareTemp = new ShareTemp();
                    shareTemp.setScene(item.getVal());
                    shareTemp.setTime(Double.valueOf(item.getScore()).longValue() + "");
                    return shareTemp;
                }).collect(Collectors.toList());
        return shareTemps;
    }

    @Data
    public static class ShareTemp {

        String time;

        String scene;
    }

    @Data
    @AllArgsConstructor
    class ShareImgsCache {

        Long expireMs;

        byte[] bytes;
    }
}
