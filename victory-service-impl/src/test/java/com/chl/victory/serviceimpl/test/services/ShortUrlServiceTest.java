package com.chl.victory.serviceimpl.test.services;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/8/3 14:34
 **/
public class ShortUrlServiceTest extends BaseTest {
    @Test
    public void test(){
        // 生成短字符串:分享模型(店铺ID 用户ID 商品ID 分享时间)
        ShortUrlDTO shortUrlDTO = new ShortUrlDTO();
        shortUrlDTO.setShopId(111111111111L);
        shortUrlDTO.setUserId(111222111111L);
        shortUrlDTO.setItemId(343242322222222L);
        shortUrlDTO.setType(ShortUrlDTO.ModelType.share_item.getCode());
        shortUrlDTO.setTime(new Date());

        Map<String, Integer> countMap = new HashMap<>();
        List<Long> timeList = new LinkedList<>();
        for (int i = 0; i < 10000; i++) {
            try {
                long s = System.nanoTime();
                String shortStr = ServiceManager.shortUrlService.save(shortUrlDTO);
                long t = System.nanoTime() - s;
                timeList.add(t);

                ShortUrlDTO shortUrlDTO1 = ServiceManager.shortUrlService.get(shortStr);
                System.out.println(shortUrlDTO1);

                boolean containsKey = countMap.containsKey(shortStr);
                if (containsKey) {
                    Integer integer = countMap.get(shortStr);
                    countMap.put(shortStr, integer + 1);
                }
                else {
                    countMap.put(shortStr, 1);
                }
            } catch (BusServiceException e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        Long max = timeList.stream().max(Long::compareTo).get();
        Long min = timeList.stream().min(Long::compareTo).get();
        long sum = timeList.stream().collect(Collectors.summingLong(value -> value)).longValue();
        long count = timeList.stream().count();
        long avg = sum / count;
        System.out.println(max);
        System.out.println(min);
        System.out.println(avg);
    }

    @Test
    public void testCleanExpired(){
        ServiceManager.shortUrlService.cleanExpired(1);
    }
}
