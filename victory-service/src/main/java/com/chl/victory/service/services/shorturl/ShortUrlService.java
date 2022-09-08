package com.chl.victory.service.services.shorturl;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.shortUrlService;

/**
 * @author ChenHailong
 * @date 2020/8/3 14:00
 **/
@Service
@Slf4j
public class ShortUrlService {

    /**
     * 存储短字符串与模型关系
     * @param model
     * @return
     * @throws BusServiceException
     */
    public String save(ShortUrlDTO model) throws BusServiceException {
        String random = RandomStringUtils.randomAlphanumeric(5);
        String val = JSONObject.toJSONString(model);
        boolean setOk = cacheService.hSetNX(CacheKeyPrefix.SHORT_STR_HASH, random, val);
        if (!setOk) {
            String key = CacheKeyPrefix.SHORT_STR_REPET_COUNT_HASH;
            Long increment = ServiceManager.cacheService.hIncrement(key, random, 1);
            random += increment.toString();
            setOk = cacheService.hSetNX(CacheKeyPrefix.SHORT_STR_HASH, random, val);
            if (!setOk) {
                throw new BusServiceException("存储短字符串失败");
            }
        }
        cacheService.expire(CacheKeyPrefix.SHORT_STR_HASH, CacheExpire.DAYS_30);
        return random;
    }

    public ShortUrlDTO get(String shortUrl){
        ShortUrlDTO shortUrlDTO = cacheService.hGet(CacheKeyPrefix.SHORT_STR_HASH, shortUrl, ShortUrlDTO.class);
        return shortUrlDTO;
    }

    public void cleanExpired(Integer stayDays){
        String key = CacheKeyPrefix.NX_LOCK + "SHORT_URL_CLEAN_EXPIRED";
        String nxLock = cacheService.getNXLock(key, CacheExpire.SECONDS_30);
        if (StringUtils.isNotBlank(nxLock)) {
            try {
                cacheService.hScanAndDeal(CacheKeyPrefix.SHORT_STR_HASH,
                                (Function<Map.Entry<String, String>, Boolean>) entry -> {
                                    String filed = entry.getKey();
                                    ShortUrlDTO val = JSONObject.parseObject(entry.getValue(), ShortUrlDTO.class);
                                    boolean del = false;
                                    if (null == val.getTime()) {
                                        del = true;
                                    }
                                    if (!del && DateUtils.addDays(val.getTime(), stayDays).before(new Date())) {
                                        del = true;
                                    }
                                    if (del) {
                                        shortUrlService.del(filed);
                                    }
                                    return true;
                                });
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("cleanExpired", e);
                }
            } finally {
                cacheService.releaseNXLock(key, nxLock);
            }

        }

    }

    private void del(String filed) {
        cacheService.hDel(CacheKeyPrefix.SHORT_STR_HASH, filed);
    }
}
