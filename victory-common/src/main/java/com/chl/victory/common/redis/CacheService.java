package com.chl.victory.common.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

/**
 * 缓存服务
 * @author ChenHailong
 * @date 2019/4/22 15:38
 **/
@Service
@Validated
public class CacheService {

    final byte[] bytes4NX = "NX".getBytes();

    final byte[] bytes4EX = "EX".getBytes();

    final String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end";

    final String limitAccessScript = "local limit = redis.call('get', KEYS[1]);  if ( limit == false or ARGV[1] - limit > 0  ) then local current = redis.call('incr',KEYS[1]) if ( limit == false ) then redis.call('expire',KEYS[1],ARGV[2]) end  return current  else return -1 end";
    final String checkLimitAccessScript = "local limit = redis.call('get', KEYS[1]);  if ( limit == false or ARGV[1] - limit > 0  ) then return 1  else return -1 end";

    final long scanCount = 10L;

    @Resource
    RedisTemplate redisTemplate;

    ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    /**
     * key 是否存在
     * @param key
     * @return
     */
    public boolean existsKey(@NotNull @NotEmpty String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        if (null == hasKey) {
            return false;
        }
        return hasKey;
    }

    public boolean expire(@NotNull @NotEmpty String key, Integer seconds) {
        if (seconds != null) {
            Boolean expire = redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
            if (null == expire) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除key
     * @param key
     * @return
     */
    public boolean delKey(@NotNull @NotEmpty String key) {
        Boolean delete = redisTemplate.delete(key);
        if (null == delete) {
            return false;
        }
        return delete;
    }

    /**
     * 排它锁
     * @param key key
     * @param expireSeconds 超时秒
     * @return 成功则返回随机数 用来确定归属，否则返回null
     */
    public String getNXLock(@NotNull @NotEmpty String key, @Positive long expireSeconds) {
        String value = System.nanoTime() + CacheKeyPrefix.SEPARATOR + threadLocalRandom.nextInt();
        final String nxKey = CacheKeyPrefix.NX_LOCK + key;

        boolean got = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            Object obj = connection
                    .execute("set", keySerializer.serialize(nxKey), valueSerializer.serialize(value), bytes4NX,
                            bytes4EX, Long.valueOf(expireSeconds).toString().getBytes());
            return obj != null;
        });

        if (got) {
            return value;
        }

        return null;
    }

    /**
     * 释放任务排它锁
     * @param randomForLock
     */
    public void releaseNXLock(@NotNull @NotEmpty String key, @NotNull @NotEmpty String randomForLock) {
        List<String> keys = Arrays.asList(key);
        // 这里returnType的选择，需结合实际与org.springframework.data.redis.connection.ReturnType
        RedisScript redisScript = RedisScript.of(script, Long.class);
        redisTemplate.execute(redisScript, keys, randomForLock);
    }
    /**
     * 限流,加1
     * @return true:已到达限流
     */
    public boolean checkAndIncrLimitAccess(@NotNull @NotEmpty String key, @NotNull Integer limitMax, @NotNull Integer seconds) {
        return doLimitAccessScript(key, limitMax, seconds, limitAccessScript);
    }

    /**
     * 仅限流
     * @param key
     * @param limitMax
     * @param seconds
     * @return
     */
    public boolean checkLimitAccess(@NotEmpty String key, @NotNull Integer limitMax, @NotNull Integer seconds) {
        return doLimitAccessScript(key, limitMax, seconds, checkLimitAccessScript);
    }

    private boolean doLimitAccessScript(String key, Integer limitMax, Integer seconds, String script) {
        List<String> keys = Arrays.asList(key);
        // 这里returnType的选择，需结合实际与org.springframework.data.redis.connection.ReturnType
        RedisScript<Long> redisScript = RedisScript.of(script, Long.class);
        Long result = (Long)redisTemplate.execute(redisScript, keys, limitMax.toString(), seconds.toString());
        if (result == null || result < 0) {
            return true;
        }
        return false;
    }

    public static <T> String transferToString(T v) {
        String val;
        if (v instanceof String) {
            val = (String) v;
        }
        else if (ClassUtils.isPrimitiveOrWrapper(v.getClass())) {
            val = v.toString();
        }
        else {
            val = JSONObject.toJSONString(v);
        }
        return val;
    }

    /**
     * String 类型操作
     * @param k
     * @param v
     * @param seconds
     * @return
     */
    public <T> boolean save(@NotNull @NotEmpty String k, @NotNull T v, @Positive int seconds) {
        String val = transferToString(v);
        redisTemplate.opsForValue().set(k, val, seconds, TimeUnit.SECONDS);
        return true;
    }
    public <T> boolean saveNX(@NotNull @NotEmpty String k, @NotNull T v, @Positive int seconds) {
        String val = transferToString(v);
        return redisTemplate.opsForValue().setIfAbsent(k, val, seconds, TimeUnit.SECONDS);
    }

    public <T> boolean save(@NotNull @NotEmpty String k, @NotNull T v) {
        String val = transferToString(v);
        redisTemplate.opsForValue().set(k, val);
        return true;
    }

    /**
     * String 类型操作
     * @param k
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(@NotNull @NotEmpty String k, @NotNull Class<T> clazz) {
        String v = (String) redisTemplate.opsForValue().get(k);
        T val = transferTo(v, clazz);
        return val;
    }

    private <T> T transferTo(String v, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return (T) v;
        }

        if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
            try {
                return clazz.getConstructor(String.class).newInstance(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        T result = JSONObject.parseObject(v, clazz);
        return result;
    }

    private <T> List<T> transferTo(List<String> v, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return (List<T>) v;
        }

        List<T> list = new ArrayList<>();
        for (String s : v) {
            T result = JSONObject.parseObject(s, clazz);
            list.add(result);
        }

        return list;
    }

    public String get(@NotNull @NotEmpty String k) {
        String v = (String) redisTemplate.opsForValue().get(k);
        return v;
    }

    /**
     * 所有list数据,需指明具体类型
     * @param k
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> lRange(@NotNull @NotEmpty String k, @NotNull Class<T> clazz) {
        List<String> range = redisTemplate.opsForList().range(k, 0, -1);
        if (CollectionUtils.isEmpty(range)) {
            return Collections.EMPTY_LIST;
        }

        List<T> result = transferTo(range, clazz);

        return result;
    }

    public <T> List<T> lRange(@NotNull @NotEmpty String k, long start, long end, @NotNull Class<T> clazz) {
        List<String> range = redisTemplate.opsForList().range(k, start, end);
        if (CollectionUtils.isEmpty(range)) {
            return Collections.EMPTY_LIST;
        }

        List<T> result = transferTo(range, clazz);

        return result;
    }

    public void lTrim(@NotNull @NotEmpty String k, long start, long end) {
        redisTemplate.opsForList().trim(k, start, end);
    }

    /**
     * lPush + lTrim
     * @param k
     * @param ele
     * @param max
     * @param <T>
     */
    public <T> void lPushAndTrim(@NotEmpty String k, @NotNull T ele, Integer max, Integer expireSeconds){
        String val = transferToString(ele);
        //redisTemplate.multi();
        lLPush(k, val);
        lTrim(k, 0, max);
        expire(k, expireSeconds);
        //redisTemplate.exec();
    }

    /**
     * @param k
     * @return
     */
    public List<String> lRange(@NotNull @NotEmpty String k) {
        List<String> range = redisTemplate.opsForList().range(k, 0, -1);
        if (CollectionUtils.isEmpty(range)) {
            return Collections.EMPTY_LIST;
        }

        return range;
    }

    /**
     * 从左加入一批数据
     * @param k
     * @param eles
     * @param <T>
     * @return list元素数量
     */
    public <T> Long lLPushAll(@NotNull @NotEmpty String k, @NotNull @NotEmpty List<T> eles) {
        List<String> vals = new ArrayList<>();
        for (T ele : eles) {
            String val = transferToString(ele);
            vals.add(val);
        }

        Long size = redisTemplate.opsForList().leftPushAll(k, vals);

        return size;
    }

    /**
     * 从左加入一个数据
     * @param k
     * @param ele
     * @param <T>
     * @return
     */
    public <T> Long lLPush(@NotNull @NotEmpty String k, @NotNull T ele) {
        String val = transferToString(ele);

        Long size = redisTemplate.opsForList().leftPush(k, val);

        return size;
    }

    /**
     * 长度
     * @param k
     * @return
     */
    public Long lLen(@NotNull @NotEmpty String k) {
        Long size = redisTemplate.opsForList().size(k);
        return size;
    }

    /**
     * 删除数据为val的元素，由count控制数量和方向
     * @param k
     * @param count count>0，从左到右删除；count <0 从右到左删除； count=0，删除所有匹配元素
     * @param val
     * @param <T>
     * @return
     */
    public <T> Long lRem(@NotNull @NotEmpty String k, int count, @NotNull T val) {
        String temp = transferToString(val);
        Long removeCount = redisTemplate.opsForList().remove(k, count, temp);
        return removeCount;
    }

    /**
     * 向set添加元素
     * @param k
     * @param ele
     * @param <T>
     * @return 实际添加元素数量
     */
    public <T> Long sAdd(@NotNull @NotEmpty String k, @NotNull T ele) {
        String val = transferToString(ele);
        Long addCount = redisTemplate.opsForSet().add(k, val);
        return addCount;
    }

    public <T> void sAddAll(@NotNull @NotEmpty String k, @NotNull Set<T> eles) {
        for (T ele : eles) {
            sAdd(k, ele);
        }
    }

    /**
     * 从set删除元素
     * @param k
     * @param ele
     * @param <T>
     * @return 实际删除数量
     */
    public <T> Long sRem(@NotNull @NotEmpty String k, @NotNull T ele) {
        String val = transferToString(ele);
        Long removeCount = redisTemplate.opsForSet().remove(k, val);
        return removeCount;
    }

    /**
     * set中元素数量
     * @param k
     * @return
     */
    public Long sCard(@NotNull @NotEmpty String k) {
        Long size = redisTemplate.opsForSet().size(k);
        return size == null ? 0: size;
    }

    /**
     * 元素是否在set中
     * @param k
     * @param ele
     * @param <T>
     * @return
     */
    public <T> boolean sIsMember(@NotNull @NotEmpty String k, @NotNull T ele) {
        String val = transferToString(ele);
        Boolean isMember = redisTemplate.opsForSet().isMember(k, val);
        if (null == isMember) {
            return false;
        }
        return isMember;
    }

    /**
     * 通过sscan获取set中所有元素
     * @param k
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Set<T> sScan(@NotNull @NotEmpty String k, @NotNull Class<T> clazz) {
        ScanOptions scanOptions = ScanOptions.scanOptions().count(scanCount).build();
        Cursor scan = redisTemplate.opsForSet().scan(k, scanOptions);
        Set<T> set = new HashSet<>();
        while (scan.hasNext()) {
            Object next = scan.next();
            T ele = transferTo((String) next, clazz);
            set.add(ele);
        }

        return set;
    }

    public <T> Set<T> sDiff(@NotNull @NotEmpty String k1, @NotNull @NotEmpty String k2, @NotNull Class<T> clazz) {
        Set difference = redisTemplate.opsForSet().difference(k1, k2);
        if (CollectionUtils.isEmpty(difference)) {
            return Collections.EMPTY_SET;
        }

        Set<T> set = transferTo(difference, clazz);

        return set;
    }

    public <T> Set<T> sInter(@NotNull @NotEmpty String k1, @NotNull @NotEmpty String k2, @NotNull Class<T> clazz) {
        Set intersect = redisTemplate.opsForSet().intersect(k1, k2);
        if (CollectionUtils.isEmpty(intersect)) {
            return Collections.EMPTY_SET;
        }

        Set<T> set = transferTo(intersect, clazz);

        return set;
    }

    public <T> Set<T> sInter(@NotNull @NotEmpty String k1, @NotNull @NotEmpty Set<T> otherSet,
            @NotNull Class<T> clazz) {
        Set intersect = redisTemplate.opsForSet().intersect(k1, otherSet);
        if (CollectionUtils.isEmpty(intersect)) {
            return Collections.EMPTY_SET;
        }

        Set<T> set = transferTo(intersect, clazz);

        return set;
    }

    public <T> Set<T> sUnion(@NotNull @NotEmpty String k1, @NotNull @NotEmpty String k2, @NotNull Class<T> clazz) {
        Set union = redisTemplate.opsForSet().union(k1, k2);
        if (CollectionUtils.isEmpty(union)) {
            return Collections.EMPTY_SET;
        }

        Set<T> set = transferTo(union, clazz);

        return set;
    }

    /**
     * hash
     * @param k
     * @param vals
     * @param <K>
     * @param <V>
     */
    public <K, V> void hSet(@NotNull @NotEmpty String k, @NotNull @NotEmpty Map<K, V> vals) {
        Map<String, String> map = transferToStringMap(vals);
        redisTemplate.opsForHash().putAll(k, map);
    }

    public <K, V> void hSet(@NotNull @NotEmpty String k, @NotNull K field, @NotNull V val) {
        redisTemplate.opsForHash().put(k, transferToString(field), transferToString(val));
    }

    public <K, V> void hSet(@NotNull @NotEmpty String k, @NotNull @NotEmpty Map<K, V> vals, Integer seconds) {
        hSet(k, vals);
        expire(k, seconds);
    }

    public <K, V> void hSet(@NotNull @NotEmpty String k, @NotNull K field, @NotNull V val, Integer seconds) {
        hSet(k, field, val);
        expire(k, seconds);
    }

    public Long hIncrement(@NotNull @NotEmpty String k, @NotNull String field, @NotNull Integer val){
        return redisTemplate.opsForHash().increment(k, field, val);
    }

    /**
     * 仅当key field不存在时增加数据
     * @param k
     * @param field
     * @param val
     * @param <K>
     * @param <V>
     */
    public <K, V> boolean hSetNX(@NotNull @NotEmpty String k, @NotNull K field, @NotNull V val) {
        Boolean aBoolean = redisTemplate.opsForHash().putIfAbsent(k, transferToString(field), transferToString(val));
        if (null == aBoolean) {
            return false;
        }
        return aBoolean;
    }

    public <K> boolean hExist(@NotNull @NotEmpty String k, @NotNull K field) {
        Boolean exist = redisTemplate.opsForHash().hasKey(k, transferToString(field));
        if (null == exist) {
            return false;
        }
        return exist;
    }

    public <V> V hGet(@NotNull @NotEmpty String k, @NotNull String field, Class<V> clazz) {
        String val = (String) redisTemplate.opsForHash().get(k, transferToString(field));
        if (null == val) {
            return null;
        }

        V v = transferTo(val, clazz);

        return v;
    }
    public <K, F, V> List<V> hMGet(@NotNull @NotEmpty String k, @NotNull List<F> fields, Class<V> clazz) {
        List<String> collect = fields.stream().map(JSONObject::toJSONString).collect(Collectors.toList());
        List<String> vals = redisTemplate.opsForHash().multiGet(k, collect);
        if (!CollectionUtils.isEmpty(vals)) {
            List<V> vList = vals.stream().map(item -> transferTo(item, clazz)).collect(Collectors.toList());
            return vList;
        }
        return null;
    }

    public <K, V> Map<K, V> hScan(@NotNull @NotEmpty String k, Class<K> fieldClazz, Class<V> valClazz) {
        Cursor scan = redisTemplate.opsForHash().scan(k, ScanOptions.scanOptions().count(scanCount).build());
        if (null == scan) {
            return Collections.EMPTY_MAP;
        }

        Map<K, V> resultMap = new HashMap<>();
        while (scan.hasNext()) {
            Map.Entry<String, String> next = (Map.Entry) scan.next();
            resultMap.put(transferTo(next.getKey(), fieldClazz), transferTo(next.getValue(), valClazz));
        }

        return resultMap;
    }

    public Set<String> hFields(@NotNull @NotEmpty String k) {
        Set<String> fields = redisTemplate.opsForHash().keys(k);
        return fields;
    }

    /**
     * @param k
     * @param function
     */
    public void hScanAndDeal(@NotNull @NotEmpty String k, Function function) {
        Cursor scan = redisTemplate.opsForHash().scan(k, ScanOptions.scanOptions().count(scanCount).build());
        if (null != scan) {
            while (scan.hasNext()) {
                Map.Entry<String, String> next = (Map.Entry) scan.next();
                function.apply(next);
            }
        }
    }

    /**
     * 删除hash中某个field
     * @param k
     * @param field
     * @param <K>
     * @return
     */
    public <K> boolean hDel(@NotNull @NotEmpty String k, @NotNull K field) {
        Long delete = redisTemplate.opsForHash().delete(k, transferToString(field));
        return delete > 0;
    }

    /**
     * hash field数量
     * @param k
     * @return
     */
    public Long hLen(@NotNull @NotEmpty String k) {
        Long size = redisTemplate.opsForHash().size(k);
        return size;
    }

    /**
     * zset add
     * @param k
     * @param ele
     * @param <T>
     * @return
     */
    public <T> boolean zAdd(@NotNull @NotEmpty String k, @NotNull ZSetEle<T> ele) {
        Boolean add = redisTemplate.opsForZSet().add(k, transferToString(ele.getVal()), ele.getScore());
        if (null == add) {
            return false;
        }
        return add;
    }

    public <T> Long zAdd(@NotNull @NotEmpty String k, @NotNull Set<ZSetEle<T>> eles) {
        Set<ZSetOperations.TypedTuple> tupleSet = new HashSet<>();
        for (ZSetEle<T> ele : eles) {
            DefaultTypedTuple<String> tuple = new DefaultTypedTuple<>(transferToString(ele.getVal()), ele.getScore());
            tupleSet.add(tuple);
        }
        Long addCount = redisTemplate.opsForZSet().add(k, tupleSet);
        return addCount;
    }

    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public void setBit(String key, Long offset, boolean val) {
        redisTemplate.opsForValue().setBit(key, offset, val);
    }

    public boolean getBit(String key, Long offset) {
        Boolean val = redisTemplate.opsForValue().getBit(key, offset);
        if (val == null) {
            val = false;
        }
        return val;
    }

    /**
     * zset remove
     * @param k
     * @param val
     * @param <T>
     * @return
     */
    public <T> Long zRem(@NotNull @NotEmpty String k, @NotNull T val) {
        Long remove = redisTemplate.opsForZSet().remove(k, transferToString(val));
        return remove;
    }

    /**
     * 按照score范围进行删除
     * @param k
     * @param minScore
     * @param maxScore
     * @return
     */
    public Long zRemRangeByScore(@NotNull @NotEmpty String k, double minScore, double maxScore) {
        Long aLong = redisTemplate.opsForZSet().removeRangeByScore(k, minScore, maxScore);
        return aLong;
    }

    /**
     * zset 元素数量
     * @param k
     * @return
     */
    public Long zCard(@NotNull @NotEmpty String k) {
        Long size = redisTemplate.opsForZSet().zCard(k);
        return size;
    }

    /**
     * zset score范围的元素数量
     * @param k
     * @param minScore
     * @param maxScore
     * @return
     */
    public Long zCount(@NotNull @NotEmpty String k, double minScore, double maxScore) {
        Long count = redisTemplate.opsForZSet().count(k, minScore, maxScore);
        return count;
    }

    /**
     * zset score范围匹配元素
     * @param k
     * @param minScore
     * @param maxScore
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Set<ZSetEle<T>> zRangeByScore(@NotNull @NotEmpty String k, double minScore, double maxScore,
            Class<T> clazz) {
        Set<ZSetOperations.TypedTuple<String>> tupleSet = redisTemplate.opsForZSet()
                .rangeByScoreWithScores(k, minScore, maxScore);
        Set<ZSetEle<T>> eles = new HashSet<>();
        for (ZSetOperations.TypedTuple<String> tuple : tupleSet) {
            eles.add(new ZSetEle<T>(transferTo(tuple.getValue(), clazz), tuple.getScore()));
        }
        return eles;
    }

    /**
     * zset score正序 index范围的元素
     * <br/>
     * index说明
     * index>0 0代表第一条 1代表第二条
     * index<0 -1代表最后一条 -2倒数第二条
     * @param k
     * @param startIndex
     * @param endIndex
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Set<ZSetEle<T>> zRange(@NotNull @NotEmpty String k, long startIndex, long endIndex, Class<T> clazz) {
        Set<ZSetOperations.TypedTuple<String>> tupleSet = redisTemplate.opsForZSet()
                .rangeWithScores(k, startIndex, endIndex);
        Set<ZSetEle<T>> eles = new HashSet<>();
        for (ZSetOperations.TypedTuple<String> tuple : tupleSet) {
            eles.add(new ZSetEle<T>(transferTo(tuple.getValue(), clazz), tuple.getScore()));
        }
        return eles;
    }

    /**
     * zset score倒序 元素所在index based-0
     * @param k
     * @param ele
     * @param <T>
     * @return
     */
    public <T> Long zRevRank(@NotNull @NotEmpty String k, T ele) {
        Long index = redisTemplate.opsForZSet().rank(k, transferToString(ele));
        return index;
    }

    /**
     * zset scan 所有元素
     * @param k
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Set<ZSetEle<T>> zScan(@NotNull @NotEmpty String k, Class<T> clazz) {
        Cursor scan = redisTemplate.opsForZSet().scan(k, ScanOptions.scanOptions().count(scanCount).build());
        if (null == scan) {
            return Collections.EMPTY_SET;
        }

        Set<ZSetEle<T>> set = new HashSet<>();
        while (scan.hasNext()) {
            Object next = scan.next();
            ZSetOperations.TypedTuple<String> tuple = (ZSetOperations.TypedTuple<String>) next;
            set.add(new ZSetEle<>(transferTo(tuple.getValue(), clazz), tuple.getScore()));
        }

        return set;
    }

    private <V, K> Map<String, String> transferToStringMap(Map<K, V> sourceMap) {
        Map<String, String> map = new HashMap<>();

        for (Map.Entry<K, V> kvEntry : sourceMap.entrySet()) {
            K key = kvEntry.getKey();
            V value = kvEntry.getValue();

            if (key instanceof String && value instanceof String) {
                map = (Map<String, String>) sourceMap;
                break;
            }

            map.put(transferToString(key), transferToString(value));
        }

        return map;
    }

    private <T> Set<T> transferTo(Set<String> sourceSet, Class<T> clazz) {
        Set<T> set = new HashSet<>();
        for (Object o : sourceSet) {
            T ele = transferTo((String) o, clazz);
            set.add(ele);
        }
        return set;
    }

    /**
     * zset 元素
     * @param <T>
     */
    @Data
    @AllArgsConstructor
    public static class ZSetEle<T> {

        T val;

        double score;
    }

}
