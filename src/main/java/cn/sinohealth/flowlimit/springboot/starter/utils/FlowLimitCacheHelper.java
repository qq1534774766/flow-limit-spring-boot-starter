package cn.sinohealth.flowlimit.springboot.starter.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/3 13:32
 * @Description: 为了不影响其他项目Redis的序列化配置，于是聚合本starter所必须的RedisTemplate以避免对其他类造成影响
 * 策略模式！
 */
public class FlowLimitCacheHelper {
    /**
     * 缓存使用的策略：1.redis 2.local
     */
    private static CacheDataSourceTypeEnum strategy;

    private final CacheHelperFactory cacheHelperFactory;

    public FlowLimitCacheHelper(CacheDataSourceTypeEnum strategy, RedisConnectionFactory redisConnectionFactory/*,Map<Long, Caffeine<Object, Object>> caffeineMap*/) {
        //指定策略
        FlowLimitCacheHelper.strategy = strategy;
        //构建缓存
        //工厂初始化
        this.cacheHelperFactory = new CacheHelperFactory();
        //Redis策略初始化
        RedisStrategyService redisStrategyService = new RedisStrategyService(redisConnectionFactory);
        //本地缓存策略初始化
        LocalStrategyService localStrategyService = new LocalStrategyService(/*caffeineMap*/);
        //构建缓存对象
//        if(CacheDataSourceTypeEnum.Local==strategy){
//            localStrategyService.buildCache();
//        }
        //设置工厂策略
        this.cacheHelperFactory.addStrategyService(CacheDataSourceTypeEnum.Redis, redisStrategyService);
        this.cacheHelperFactory.addStrategyService(CacheDataSourceTypeEnum.Local, localStrategyService);
    }

    public interface IFlowLimitStrategyService {
        Integer getOne(String key) throws Exception;

        Boolean setOne(String key, Integer value, Long timeOut, TimeUnit timeUnit) throws Exception;

        void deleteKey(String key) throws Exception;

        void increaseKey(String key) throws Exception;

        Boolean increaseKeySafely(String key, Long timeout, Integer CountMax) throws Exception;
    }

    public static class RedisStrategyService implements IFlowLimitStrategyService {
        private final RedisTemplate<String, Object> redisTemplate;
        private static final String LUA_INC_SCRIPT_TEXT =
                "local counterKey = KEYS[1]; " +
                        "local timeout = ARGV[1]; " +
                        "local countMax = ARGV[2]; " +
                        "local currentCount = redis.call('get', counterKey); " +
                        "if currentCount and tonumber(currentCount) >= tonumber(countMax) then " +
                        "return 0; " +
                        "end " +
                        "currentCount = redis.call('incr',counterKey); " +
                        "if tonumber(currentCount) == 1 then " +
                        "redis.call('pexpire', counterKey, timeout); " +
                        "end " +
                        "return 1; ";
        private static final DefaultRedisScript<Long> REDIS_INC_SCRIPT = new DefaultRedisScript<>(LUA_INC_SCRIPT_TEXT, Long.class);

        @Override
        public Integer getOne(String key) throws Exception {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj instanceof Integer) {
                return (Integer) obj;
            }
            if (obj instanceof String) {
                return Integer.valueOf((String) obj);
            }
            return null;
        }

        @Override
        public Boolean setOne(String key, Integer value, Long timeOut, TimeUnit timeUnit) throws Exception {
            redisTemplate.opsForValue().set(key, value, timeOut, timeUnit);
            return true;
        }

        @Override
        public void deleteKey(String key) throws Exception {
            redisTemplate.delete(key);
        }

        @Override
        public void increaseKey(String key) throws Exception {
            redisTemplate.opsForValue().increment(key);
        }

        @Override
        public Boolean increaseKeySafely(String key, Long timeout, Integer CountMax) throws Exception {
            Long result = execute(Collections.singletonList(key), timeout, CountMax);
            return Optional.ofNullable(result).orElse(1L) == 0L;
        }

        public Long execute(List<String> keys, Object... args) {
            return redisTemplate.execute(REDIS_INC_SCRIPT, keys, args);
        }

        public RedisStrategyService(RedisConnectionFactory redisConnectionFactory) {
            this.redisTemplate = userInfoRedisTemplate(redisConnectionFactory);
        }

        private RedisTemplate<String, Object> userInfoRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.setKeySerializer(stringRedisSerializer);
            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            redisTemplate.setHashKeySerializer(stringRedisSerializer);
            redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        }

        private RedisSerializer<Object> jackson2JsonRedisSerializer() {
            //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
            Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
            serializer.setObjectMapper(mapper);
            return serializer;
        }
    }

    public static class LocalStrategyService implements IFlowLimitStrategyService {
//        private Map<Long, Caffeine<Object, Object>> caffeineMap;
//
//        private Map<Long, Cache<String,Integer>> cacheMap;

        public LocalStrategyService() {
        }

//        public LocalStrategyService(Map<Long, Caffeine<Object, Object>> caffeineMap) {
//            this.caffeineMap = caffeineMap;
//        }

        /**
         * 构建缓存
         */
//        public synchronized void buildCache(){
//            this.cacheMap = new HashMap<>();
//            caffeineMap.forEach((HoldTimeKey, value)-> this.cacheMap.put(HoldTimeKey,value.build()));
//        }
        @Override
        public Integer getOne(String key) throws Exception {
            return null;
        }

        @Override
        public Boolean setOne(String key, Integer value, Long timeOut, TimeUnit timeUnit) throws Exception {
            return null;
        }

        @Override
        public void deleteKey(String key) throws Exception {

        }

        @Override
        public void increaseKey(String key) throws Exception {

        }


        @Override
        public Boolean increaseKeySafely(String key, Long timeout, Integer CountMax) throws Exception {
            return null;
        }
    }

    public static class CacheHelperFactory {
        private Map<CacheDataSourceTypeEnum, IFlowLimitStrategyService> map = new HashMap<>();
        private static final Timer CHANGE_STRATEGY_TIMER = new Timer();


        public void addStrategyService(CacheDataSourceTypeEnum dataSourceTypeEnum, IFlowLimitStrategyService strategyService) {
            map.put(dataSourceTypeEnum, strategyService);
        }

        public Integer getOne(String key) {
            return Optional.ofNullable(map.get(strategy))
                    .map(o -> {
                        try {
                            return o.getOne(key);
                        } catch (Exception e) {
                            changeStrategy();
                            return -1;
                        }
                    })
                    .orElse(-1);

        }

        public Boolean setOne(String key, Integer value, Long timeOut, TimeUnit timeUnit) {
            return Optional.ofNullable(map.get(strategy))
                    .map(o -> {
                        try {
                            return o.setOne(key, value, timeOut, timeUnit);
                        } catch (Exception e) {
                            changeStrategy();
                            return false;
                        }
                    })
                    .orElse(false);
        }

        public void deleteKey(String key) {
            Optional.ofNullable(map.get(strategy))
                    .ifPresent(o -> {
                        try {
                            o.deleteKey(key);
                        } catch (Exception e) {
                            changeStrategy();
                        }
                    });
        }

        public void increaseKey(String key) {
            Optional.ofNullable(map.get(strategy))
                    .ifPresent(o -> {
                        try {
                            o.increaseKey(key);
                        } catch (Exception e) {
                            changeStrategy();
                        }
                    });
        }

        public Boolean increaseKeySafely(String key, Long timeout, Integer CountMax) {
            return Optional.ofNullable(map.get(strategy))
                    .map(o -> {
                        try {
                            return o.increaseKeySafely(key, timeout, CountMax);
                        } catch (Exception e) {
                            changeStrategy();
                            return false;
                        }
                    })
                    .orElse(false);
        }

        /**
         * 切换策略，加锁。默认使用Redis作为数据源。如果Redis宕机，则自动切换使用本地缓存，一个小时之后切换回Redis缓存。
         *
         * @return
         */
        public synchronized void changeStrategy() {
            strategy = CacheDataSourceTypeEnum.Local;
            //构建缓存对象
//            Optional.ofNullable(map.get(CacheDataSourceTypeEnum.Local))
//                    .ifPresent(o->((LocalStrategyService)o).buildCache());
            //取消之前的定时器
            CHANGE_STRATEGY_TIMER.cancel();
            //开启新的定时器
            CHANGE_STRATEGY_TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    strategy = CacheDataSourceTypeEnum.Redis;
                }
            }, 3600 * 1000);
        }
    }


    public Boolean increaseKeySafely(String key, Long timeout, Integer CountMax) {
        return cacheHelperFactory.increaseKeySafely(key, timeout, CountMax);
    }

    public Integer getOne(String key) {
        return cacheHelperFactory.getOne(key);
    }

    public void deleteKey(String key) {
        cacheHelperFactory.deleteKey(key);
    }
}
