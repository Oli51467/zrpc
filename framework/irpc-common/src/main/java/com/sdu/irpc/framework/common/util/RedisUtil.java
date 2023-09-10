package com.sdu.irpc.framework.common.util;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sdu.irpc.framework.common.constant.RedisKey.LUA_INCR_EXPIRE;

@Slf4j
public class RedisUtil {

    private static StringRedisTemplate stringRedisTemplate;
    private static RedisTemplate<String, Object> redisTemplate;

    static {
        RedisUtil.redisTemplate = SpringUtil.getBean("redisTemplate");
        RedisUtil.stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
    }

    public static boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public static Set<String> getWithPrefixKey(String key) {
        return stringRedisTemplate.keys(key.concat("*"));
    }

    public static int incrementInt(final String key) {
        Long increment = stringRedisTemplate.opsForValue().increment(key, 1);
        if (null != increment) return increment.intValue();
        else return Integer.parseInt(Objects.requireNonNull(get(key)));
    }

    public static void decrementInt(final String key) {
        stringRedisTemplate.opsForValue().decrement(key, 1);
    }

    public static Long getExpire(String key, TimeUnit timeUnit) {
        return stringRedisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 指定缓存失效时间
     *
     * @param key      键
     * @param time     时间(秒)
     * @param timeUnit 单位
     */
    public static Boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                stringRedisTemplate.expire(key, time, timeUnit);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 删除缓存
     *
     * @param keys 键
     */
    public static void del(String... keys) {
        if (keys != null && keys.length > 0) {
            if (keys.length == 1) {
                Boolean result = stringRedisTemplate.delete(keys[0]);
                log.debug("删除缓存：" + keys[0] + "，结果：" + result);
            } else {
                Set<String> keySet = new HashSet<>();
                for (String key : keys) {
                    Set<String> stringSet = stringRedisTemplate.keys(key);
                    if (Objects.nonNull(stringSet) && !stringSet.isEmpty()) {
                        keySet.addAll(stringSet);
                    }
                }
                Long count = stringRedisTemplate.delete(keySet);
                log.debug("成功删除缓存：" + keySet);
                log.debug("缓存删除数量：" + count + "个");
            }
        }
    }

    public static void oSet(String key, Object value, Long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    public static Object oGet(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public static String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public static <T> T get(String key, Class<T> tClass) {
        String s = get(key);
        return toBeanOrNull(s, tClass);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public static Boolean set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set Error: {}, key: {}", e.getMessage(), key);
            return false;
        }
    }

    /**
     * 过期时间放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public static Boolean setExpTime(String key, String value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                stringRedisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis setExpTime error: {}, key: {}", e.getMessage(), key);
            return false;
        }
    }

    /**
     * ------------------Set相关操作--------------------------------
     */
    public static void sAdd(String key, Object value) {
        stringRedisTemplate.opsForSet().add(key, value.toString());
    }

    public static void sAdds(String key, Object... values) {
        try {
            if (values.length == 0) return;
            String[] s = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                s[i] = String.valueOf(values[i]);
            }
            stringRedisTemplate.opsForSet().add(key, s);
        } catch (Exception e) {
            log.error("Redis sAdd Error: {}, key: {}", e.getMessage(), key);
        }
    }

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     */
    public static Set<String> sGet(String key) {
        try {
            return stringRedisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis sGet Error: {}, key: {}", e.getMessage(), key);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public static Boolean sHasKey(String key, Object value) {
        try {
            return stringRedisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("Redis sHasKey Error: {}, key: {}", e.getMessage(), key);
            return false;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return set缓存的长度
     */
    public static Integer sSize(String key) {
        try {
            if (!hasKey(key)) return 0;
            return Objects.requireNonNull(stringRedisTemplate.opsForSet().size(key)).intValue();
        } catch (Exception e) {
            log.error("Redis sSize Error: {}, key: {}", e.getMessage(), key);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     */
    public static void sRemove(String key, Object... values) {
        try {
            if (values.length == 0) return;
            stringRedisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("Redis sRemove Error: {}, key: {}", e.getMessage(), key);
        }
    }

    public static Set<String> sInter(String key1, String key2) {
        return stringRedisTemplate.opsForSet().intersect(key1, key2);
    }

    /**
     * ------------------zSet相关操作--------------------------------
     */

    public static void zAdd(String key, Object value, double score) {
        stringRedisTemplate.opsForZSet().add(key, value.toString(), score);
    }

    public static void zRemove(String key, Object value) {
        stringRedisTemplate.opsForZSet().remove(key, value.toString());
    }

    public static Boolean zIsMember(String key, Object value) {
        return Objects.nonNull(stringRedisTemplate.opsForZSet().score(key, value.toString()));
    }

    public static Set<ZSetOperations.TypedTuple<String>> zGetReverseRangeWithScores(String key) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public static Object hGet(String key, String item) {
        return stringRedisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public static Map<Object, Object> hMultiGet(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public static Boolean hMultiSet(String key, Map<String, Object> map) {
        try {
            stringRedisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public static Boolean hMultiSet(String key, Map<String, Object> map, long time) {
        try {
            stringRedisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public static Boolean hSet(String key, String item, Object value) {
        try {
            stringRedisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public static Boolean hSet(String key, String item, Object value, long time) {
        try {
            stringRedisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public static void hDelete(String key, Object... item) {
        stringRedisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public static Boolean hHasKey(String key, String item) {
        return stringRedisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public static Boolean expire(String key, long time) {
        try {
            if (time > 0) {
                stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * ------------------相关业务操作--------------------------------
     */

    public static <T> List<T> mGet(Collection<String> keys, Class<T> tClass) {
        List<String> list = stringRedisTemplate.opsForValue().multiGet(keys);
        if (Objects.isNull(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(o -> toBeanOrNull(o, tClass)).collect(Collectors.toList());
    }

    static <T> T toBeanOrNull(String json, Class<T> tClass) {
        return json == null ? null : JsonUtil.toObj(json, tClass);
    }

    public static void inc(String key, int time, TimeUnit unit) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_INCR_EXPIRE, Long.class);
        stringRedisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(unit.toSeconds(time)));
    }

}
