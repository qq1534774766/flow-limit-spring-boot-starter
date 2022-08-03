package cn.sinohealth.flowlimit.springboot.starter.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/3 13:32
 * @Description: 为了不影响其他项目Redis的序列化配置，于是聚合本starter所必须的RedisTemplate亿避免对其他类造成影响
 */
public class RedisFlowLimitTemplateHelper {

    private final RedisTemplate<String, Object> redisTemplate;


    public Long execute(RedisScript<Long> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }

    public Integer getOne(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof String) {
            return Integer.valueOf((String) obj);
        }
        return null;
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    public RedisFlowLimitTemplateHelper(RedisConnectionFactory redisConnectionFactory) {
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
