package com.chl.victory.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.BaseTest;
import com.chl.victory.common.redis.CacheService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author ChenHailong
 * @date 2019/11/25 16:40
 **/
public class CacheServiceTest extends BaseTest {

    @Resource
    CacheService cacheService;

    @Resource
    RedisTemplate redisTemplate;

    @Test
    public void testHashScan() {
        Map<Integer, Integer> map = new HashMap<>();
        int size = 100;
        for (int i = 0; i < size; i++) {
            map.put(i, i);
        }
        String key = "testHash";
        cacheService.hSet(key, map);
        Long hLen = cacheService.hLen(key);
        Assert.assertTrue(size == hLen);

        Integer newVal = 100;
        cacheService.hSet(key, 1, newVal);
        Integer val = cacheService.hGet(key, "1", Integer.class);
        Assert.assertTrue(val.equals(newVal));

        boolean hSetNX = cacheService.hSetNX(key, 1, 200);
        Assert.assertFalse(hSetNX);

        hSetNX = cacheService.hSetNX(key, 200, 200);
        Assert.assertTrue(hSetNX);

        map = cacheService.hScan(key, Integer.class, Integer.class);
        Assert.assertTrue(map.size() >= size);

        boolean hDel = cacheService.hDel(key, 2);
        Assert.assertTrue(hDel);

        boolean b = cacheService.hExist(key, 3);
        Assert.assertTrue(b);

        redisTemplate.delete(key);
    }

    @Test
    public void testSet() {
        String key = "setTest";
        int size = 100;
        for (int i = 0; i < size; i++) {
            cacheService.sAdd(key, i);
        }

        Long card = cacheService.sCard(key);
        Assert.assertTrue(size - card == 0);

        boolean isMember = cacheService.sIsMember(key, 88);
        Assert.assertTrue(isMember);

        Set<Integer> scan = cacheService.sScan(key, Integer.class);
        Assert.assertTrue(scan.size() == size);

        Long sRem = cacheService.sRem(key, 70);
        Assert.assertTrue(sRem > 0);

        String key1 = "setTest1";
        size = 10;
        for (int i = 0; i < size; i++) {
            cacheService.sAdd(key1, i);
        }

        Set<Integer> integers = cacheService.sInter(key, key1, Integer.class);
        Assert.assertTrue(integers.size() == 10);
        Assert.assertTrue(integers.contains(1));
        Assert.assertTrue(integers.contains(9));
        Assert.assertFalse(integers.contains(10));

        Set<Integer> diff = cacheService.sDiff(key, key1, Integer.class);
        Assert.assertFalse(diff.contains(1));
        Assert.assertFalse(diff.contains(9));
        Assert.assertTrue(diff.contains(10));
        Assert.assertTrue(diff.contains(99));

        redisTemplate.delete(key);
        redisTemplate.delete(key1);
    }

    @Test
    public void testList() {
        String key = "keyList";
        int max = 10;
        for (Integer i = 0; i < max; i++) {
            cacheService.lLPush(key, i);
        }
        List<Integer> list = new ArrayList<>();
        for (Integer i = max; i < max + 90; i++) {
            list.add(i);
        }
        Long lPush = cacheService.lLPush(key, list);
        Assert.assertTrue(lPush == 90);

        Long len = cacheService.lLen(key);
        Assert.assertTrue(len == 100);

        List<Integer> integers = cacheService.lRange(key, Integer.class);
        Assert.assertTrue(integers.size() == len);

        Long lRem = cacheService.lRem(key, 10, 1);
        Assert.assertTrue(lRem == 1);

        List<Integer> integers1 = cacheService.lRange(key, 10, -1, Integer.class);
        System.out.println(integers1.get(0));
        System.out.println(integers1.get(integers1.size() - 1));
        System.out.println(integers1.size());
        Assert.assertTrue(integers1.size() == 89);

        redisTemplate.delete(key);
    }

    @Test
    public void TestZSet() {
        String key = "testZset";
        int max = 10;
        Random random = new Random(10000);
        for (int i = 0; i < max; i++) {
            cacheService.zAdd(key, new CacheService.ZSetEle<>(i, random.nextDouble()));
        }

        Set<CacheService.ZSetEle<Integer>> setEles = new HashSet<>();
        for (int i = max; i < max + 90; i++) {
            setEles.add(new CacheService.ZSetEle<>(i, random.nextDouble()));
        }
        Long zAdd = cacheService.zAdd(key, setEles);
        Assert.assertTrue(zAdd == 90);

        Long zCard = cacheService.zCard(key);
        Assert.assertTrue(zCard == 100);

        Long zCount = cacheService.zCount(key, 100.00, 1000.00);
        Set<CacheService.ZSetEle<Integer>> setEles1 = cacheService.zRangeByScore(key, 100.00, 1000.00, Integer.class);
        Assert.assertTrue(zCount == setEles1.size());

        Long zRem = cacheService.zRem(key, 20);
        Assert.assertTrue(zRem == 1);

        Set<CacheService.ZSetEle<Integer>> setEles2 = cacheService.zScan(key, Integer.class);
        Assert.assertTrue(setEles2.size() == 99);

        Long zRevRank = cacheService.zRevRank(key, 50);
        System.out.println(zRevRank);
        Assert.assertTrue(zRevRank > 0);

        Long zRemRangeByScore = cacheService.zRemRangeByScore(key, 100.00, 1000.00);
        System.out.println(zRemRangeByScore);
    }

    @Test
    public void testTransaction() {

        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();

        redisTemplate.execute((RedisCallback) connection -> {
            connection.set(keySerializer.serialize("mk1"), valueSerializer.serialize("v"));
            System.out.println(connection.getNativeConnection());
            //LettuceConnection.class.getDeclaredField("")
            return null;
        });
        redisTemplate.execute((RedisCallback) connection -> {
            connection.set(keySerializer.serialize("mk1"), valueSerializer.serialize("v"));
            System.out.println(connection.getNativeConnection());
            return null;
        });

        List<Object> executeResult = (List<Object>) redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
            connection.multi();
            connection.set(keySerializer.serialize("mk1"), valueSerializer.serialize("v"));
            List<Object> execResult = connection.exec();
            System.out.println(connection.getNativeConnection());
            return execResult;
        });
        System.out.println(executeResult);

        executeResult = (List<Object>) redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
            connection.multi();
            connection.set(keySerializer.serialize("mk1"), valueSerializer.serialize("v"));
            List<Object> execResult = connection.exec();
            System.out.println(connection.getNativeConnection());
            return execResult;
        });
        System.out.println(executeResult);
    }

    @Test
    public void testHash() {
        // jedis 10s
        // lettuce 12s

        String key = "testHash";
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            System.out.println(JSONObject.toJSONString(new Person("name" + i, i)));
            map.put(i + "", JSONObject.toJSONString(new Person("name" + i, i)));
        }
        redisTemplate.opsForHash().putAll(key, map);
        long s = System.nanoTime();
        for (int j = 0; j < 1000; j++) {
            //redisTemplate.opsForHash().putAll(key, map);
            for (int i = 0; i < 10; i++) {
                String v = (String) redisTemplate.opsForHash().get(key, i + "");
                /*Person person = JSONObject.parseObject(v,Person.class);
                System.out.println("testHash " + i + ":" + person);*/
            }
            //redisTemplate.delete(key);
        }
        long e = System.nanoTime();
        System.out.println((e - s));
    }

    @Test
    public void getNXLock() {
        String key = "locktest";
        Long expireS = 100L;
        String randomVal = cacheService.getNXLock(key, expireS);
        Assert.assertNotNull(randomVal);
        String randomVal1 = cacheService.getNXLock(key, expireS);
        Assert.assertNull(randomVal1);
        cacheService.releaseNXLock(key, randomVal);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Person {

        String name;

        Integer age;
    }

    @Test
    public void testSave() {
        String k = "keyTest";
        String v = "vTest";
        boolean save = cacheService.save(k, v, 30);
        String val = cacheService.get(k);
        Assert.assertEquals(val, v);
        String k1 = "keyTest2";
        Long vl = 10023234324L;
        save = cacheService.save(k1, vl, 30);
        Long vll = cacheService.get(k1, Long.class);
        Assert.assertEquals(vl, vll);
    }

}
