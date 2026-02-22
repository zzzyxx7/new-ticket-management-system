package com.fuzhou.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类：基于 RedisTemplate<String, Object> 封装，支持多数据类型操作
 * 特点：1. 异常捕获（避免 Redis 操作失败导致业务中断）；2. 类型安全（明确泛型）；3. 覆盖常用操作
 */
@Component // 交给 Spring 管理，后续可通过 @Autowired 注入使用
@Slf4j
public class RedisUtil {

    // 注入你配置好的 RedisTemplate（泛型<String, Object> 与配置一致）
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper JSON_MAPPER;
    static {
        JSON_MAPPER = new ObjectMapper();
        JSON_MAPPER.findAndRegisterModules();
        JavaTimeModule timeModule = new JavaTimeModule();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
        JSON_MAPPER.registerModule(timeModule);
    }

    /**
     * 构建缓存 Key（统一格式，参考 ticket-system）
     * @param parts 各段，如 buildKey("home", "shows", "北京") -> "home:shows:北京"
     */
    public String buildKey(String... parts) {
        return String.join(":", parts);
    }

    /**
     * 按 key 前缀删除（用于首页等多城市缓存失效）
     * @param pattern 如 "home:shows:*"
     */
    public long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return 0;
            }
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("Redis deleteByPattern 失败: pattern={}", pattern, e);
            return 0;
        }
    }

    /**
     * 存 JSON 并设置过期时间（用于 List/复杂对象，避免泛型擦除）
     */
    public <T> boolean setJson(String key, T value, long timeout, TimeUnit unit) {
        try {
            String json = JSON_MAPPER.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
            return true;
        } catch (Exception e) {
            log.warn("Redis setJson 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 取 JSON 并反序列化为指定类型（支持 List&lt;ShowVO&gt; 等）
     */
    public <T> T getJson(String key, TypeReference<T> typeRef) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return JSON_MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            log.warn("Redis getJson 失败: key={}", key, e);
            return null;
        }
    }

    // ============================ 通用操作 ============================
    /**
     * 判断 Key 是否存在
     * @param key Redis Key
     * @return true=存在，false=不存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis 判断 Key 存在失败：key={}", key, e);
            return false; // 异常时返回 false，不影响业务
        }
    }

    /**
     * 删除 Key（支持批量删除）
     * @param keys 一个或多个 Key
     * @return 删除成功的 Key 数量
     */
    public long delete(String... keys) {
        try {
            if (keys == null || keys.length == 0) {
                return 0;
            }
            return redisTemplate.delete(Arrays.asList(keys));
        } catch (Exception e) {
            log.error("Redis 删除 Key 失败：keys={}", Arrays.toString(keys), e);
            return 0;
        }
    }

    /**
     * 设置 Key 过期时间
     * @param key Redis Key
     * @param timeout 过期时间（默认单位：秒）
     * @return true=设置成功，false=失败
     */
    public boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置 Key 过期时间（指定时间单位）
     * @param key Redis Key
     * @param timeout 过期时间
     * @param unit 时间单位（TimeUnit.SECONDS/Minutes/Hours/Days）
     * @return true=设置成功，false=失败
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (timeout <= 0) {
                log.warn("Redis 过期时间非法：timeout={}", timeout);
                return false;
            }
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("Redis 设置 Key 过期时间失败：key={}, timeout={}, unit={}", key, timeout, unit, e);
            return false;
        }
    }

    /**
     * 获取 Key 剩余过期时间
     * @param key Redis Key
     * @return 剩余时间（单位：秒），-1=永久有效，-2=Key 不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire == null ? -2 : expire;
        } catch (Exception e) {
            log.error("Redis 获取 Key 过期时间失败：key={}", key, e);
            return -2;
        }
    }

    // ============================ String 类型操作 ============================
    /**
     * 新增：获取指定类型的 Redis 数据（解决 LinkedHashMap 转自定义对象问题）
     * @param key Redis Key
     * @param clazz 目标对象类型
     * @param <T> 目标类型
     * @return 目标对象（不存在返回 null）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }

            // 复用 RedisConfig 中配置的时间格式化规则
            ObjectMapper om = new ObjectMapper();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            JavaTimeModule timeModule = new JavaTimeModule();
            timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
            timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
            om.registerModule(timeModule);
            om.findAndRegisterModules();
            // 忽略未知字段（避免id等字段报错）
            om.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // 兼容数组格式的旧数据
            Object realValue = value;
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (list.size() >= 2) {
                    realValue = list.get(1);
                } else {
                    log.warn("Redis 数组格式数据异常：key={}", key);
                    return null;
                }
            }

            return om.convertValue(realValue, clazz);
        } catch (Exception e) {
            log.error("Redis 获取指定类型数据失败：key={}, clazz={}", key, clazz.getName(), e);
            return null;
        }
    }
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis 存储 String 失败：key={}, value={}", key, value, e);
            return false;
        }
    }

    /**
     * 存储 String 类型数据（带过期时间）
     * @param key Redis Key
     * @param value 存储的值
     * @param timeout 过期时间（单位：秒）
     * @return true=存储成功，false=失败
     */
    public boolean set(String key, Object value, long timeout) {
        return set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 存储 String 类型数据（带过期时间，指定时间单位）
     * @param key Redis Key
     * @param value 存储的值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true=存储成功，false=失败
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("Redis 存储 String（带过期时间）失败：key={}, value={}, timeout={}, unit={}",
                    key, value, timeout, unit, e);
            return false;
        }
    }

    /**
     * 获取 String 类型数据
     * @param key Redis Key
     * @param <T> 返回值类型（自动反序列化）
     * @return 存储的值（不存在返回 null）
     */
    @SuppressWarnings("unchecked") // 抑制 unchecked 警告（泛型转换安全）
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis 获取 String 失败：key={}", key, e);
            return null;
        }
    }

    /**
     * 自增（常用于计数器、限流）
     * @param key Redis Key
     * @param delta 自增步长（正数=自增，负数=自减）
     * @return 自增后的值（Key 不存在时，先初始化为 0 再自增）
     */
    public long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis 自增失败：key={}, delta={}", key, delta, e);
            return -1; // 异常时返回 -1，需业务层处理
        }
    }

    // ============================ Hash 类型操作 ============================
    /**
     * 存储 Hash 类型数据（单个字段）
     * @param key Redis Key（Hash 表名称）
     * @param hashKey Hash 表中的字段名
     * @param value 字段值
     * @return true=存储成功，false=失败
     */
    public boolean hSet(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            return true;
        } catch (Exception e) {
            log.error("Redis 存储 Hash 失败：key={}, hashKey={}, value={}", key, hashKey, value, e);
            return false;
        }
    }

    /**
     * 存储 Hash 类型数据（批量字段）
     * @param key Redis Key（Hash 表名称）
     * @param map 包含多个 hashKey-value 的 Map
     * @return true=存储成功，false=失败
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        try {
            if (map == null || map.isEmpty()) {
                return false;
            }
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("Redis 批量存储 Hash 失败：key={}, map={}", key, map, e);
            return false;
        }
    }

    /**
     * 获取 Hash 表中单个字段的值
     * @param key Redis Key（Hash 表名称）
     * @param hashKey Hash 表中的字段名
     * @param <T> 返回值类型
     * @return 字段值（不存在返回 null）
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String hashKey) {
        try {
            return (T) redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error("Redis 获取 Hash 字段失败：key={}, hashKey={}", key, hashKey, e);
            return null;
        }
    }

    /**
     * 获取 Hash 表中所有字段和值
     * @param key Redis Key（Hash 表名称）
     * @return 包含所有 hashKey-value 的 Map（不存在返回空 Map）
     */
    public Map<String, Object> hGetAll(String key) {
        try {
            // 获取原始 Map<Object, Object>
            Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
            // 转换为 Map<String, Object>（强制转换 + 安全校验）
            Map<String, Object> resultMap = new HashMap<>();
            for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
                // 确保 key 是 String 类型（符合我们的 HashKey 序列化规则）
                if (entry.getKey() instanceof String) {
                    resultMap.put((String) entry.getKey(), entry.getValue());
                } else {
                    log.warn("Redis Hash 键类型非法（非 String）：key={}, hashKey={}", key, entry.getKey());
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("Redis 获取 Hash 所有字段失败：key={}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 删除 Hash 表中的指定字段（支持批量删除）
     * @param key Redis Key（Hash 表名称）
     * @param hashKeys 一个或多个 Hash 字段名
     * @return 删除成功的字段数量
     */
    public long hDelete(String key, Object... hashKeys) {
        try {
            if (hashKeys == null || hashKeys.length == 0) {
                return 0;
            }
            return redisTemplate.opsForHash().delete(key, hashKeys);
        } catch (Exception e) {
            log.error("Redis 删除 Hash 字段失败：key={}, hashKeys={}", key, Arrays.toString(hashKeys), e);
            return 0;
        }
    }

    // ============================ List 类型操作 ============================
    /**
     * 向 List 左侧添加元素（队列：先进先出）
     * @param key Redis Key（List 名称）
     * @param values 一个或多个元素
     * @return 添加后的 List 长度
     */
    public long lPush(String key, Object... values) {
        try {
            if (values == null || values.length == 0) {
                return 0;
            }
            return redisTemplate.opsForList().leftPushAll(key, values);
        } catch (Exception e) {
            log.error("Redis List 左侧添加元素失败：key={}, values={}", key, Arrays.toString(values), e);
            return 0;
        }
    }

    /**
     * 向 List 右侧添加元素（栈：先进后出）
     * @param key Redis Key（List 名称）
     * @param values 一个或多个元素
     * @return 添加后的 List 长度
     */
    public long rPush(String key, Object... values) {
        try {
            if (values == null || values.length == 0) {
                return 0;
            }
            return redisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            log.error("Redis List 右侧添加元素失败：key={}, values={}", key, Arrays.toString(values), e);
            return 0;
        }
    }

    /**
     * 获取 List 中指定范围的元素
     * @param key Redis Key（List 名称）
     * @param start 起始索引（0=第一个元素，-1=最后一个元素）
     * @param end 结束索引（start=0, end=-1 表示获取所有元素）
     * @return 元素列表（不存在返回空 List）
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end) {
        try {
            return (List<T>) redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis 获取 List 元素失败：key={}, start={}, end={}", key, start, end, e);
            return Collections.emptyList();
        }
    }

    /**
     * 从 List 左侧弹出一个元素（并删除该元素）
     * @param key Redis Key（List 名称）
     * @param <T> 元素类型
     * @return 弹出的元素（List 为空返回 null）
     */
    @SuppressWarnings("unchecked")
    public <T> T lPop(String key) {
        try {
            return (T) redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("Redis List 左侧弹出元素失败：key={}", key, e);
            return null;
        }
    }

    // ============================ Set 类型操作 ============================
    /**
     * 向 Set 中添加元素（自动去重）
     * @param key Redis Key（Set 名称）
     * @param values 一个或多个元素
     * @return 添加成功的元素数量（重复元素不计入）
     */
    public long sAdd(String key, Object... values) {
        try {
            if (values == null || values.length == 0) {
                return 0;
            }
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis Set 添加元素失败：key={}, values={}", key, Arrays.toString(values), e);
            return 0;
        }
    }

    /**
     * 获取 Set 中所有元素
     * @param key Redis Key（Set 名称）
     * @return 元素集合（不存在返回空 Set）
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> sMembers(String key) {
        try {
            return (Set<T>) redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis 获取 Set 所有元素失败：key={}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 判断元素是否在 Set 中
     * @param key Redis Key（Set 名称）
     * @param value 元素
     * @return true=存在，false=不存在
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("Redis 判断 Set 元素存在失败：key={}, value={}", key, value, e);
            return false;
        }
    }

    // ============================ ZSet 类型操作（有序集合） ============================
    /**
     * 向 ZSet 中添加元素（按 score 排序）
     * @param key Redis Key（ZSet 名称）
     * @param value 元素
     * @param score 排序权重（数值越大，排名越靠后）
     * @return true=添加成功，false=失败（已存在该元素时返回 false）
     */
    public boolean zAdd(String key, Object value, double score) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
        } catch (Exception e) {
            log.error("Redis ZSet 添加元素失败：key={}, value={}, score={}", key, value, score, e);
            return false;
        }
    }

    /**
     * 获取 ZSet 中指定排名范围的元素（按 score 升序）
     * @param key Redis Key（ZSet 名称）
     * @param start 起始排名（0=第一名）
     * @param end 结束排名（start=0, end=9 表示前 10 名）
     * @return 元素列表（按 score 升序排列）
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> zRange(String key, long start, long end) {
        try {
            return (Set<T>) redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis 获取 ZSet 元素（升序）失败：key={}, start={}, end={}", key, start, end, e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取 ZSet 中元素的排名（按 score 升序）
     * @param key Redis Key（ZSet 名称）
     * @param value 元素
     * @return 排名（0=第一名，-1=元素不存在）
     */
    public long zRank(String key, Object value) {
        try {
            Long rank = redisTemplate.opsForZSet().rank(key, value);
            return rank == null ? -1 : rank;
        } catch (Exception e) {
            log.error("Redis 获取 ZSet 元素排名失败：key={}, value={}", key, value, e);
            return -1;
        }
    }

    // ------------------------------ 黑名单相关 ------------------------------
    /**
     * 将 Token 加入黑名单（Key：token，Value："invalid"，过期时间=Token剩余有效期）
     */
    public void addToBlacklist(String token, long expireSeconds) {
        stringRedisTemplate.opsForValue()
                .set("jwt:blacklist:" + token, "invalid", expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isInBlacklist(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey("jwt:blacklist:" + token));
    }

    // ------------------------------ Refresh Token 相关 ------------------------------
    /**
     * 存储 Refresh Token 到 Redis（Key：refresh_token，Value：userId，过期时间=Refresh Token 有效期）
     */
    public void saveRefreshToken(String refreshToken, Long userId, long expireSeconds) {
        stringRedisTemplate.opsForValue()
                .set("jwt:refresh:" + refreshToken, userId.toString(), expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 验证 Refresh Token 是否存在（且有效）
     */
    public Long getUserIdByRefreshToken(String refreshToken) {
        String userIdStr = stringRedisTemplate.opsForValue().get("jwt:refresh:" + refreshToken);
        return userIdStr == null ? null : Long.valueOf(userIdStr);
    }

    /**
     * 删除 Refresh Token（注销登录时用）
     */
    public void deleteRefreshToken(String refreshToken) {
        stringRedisTemplate.delete("jwt:refresh:" + refreshToken);
    }
}