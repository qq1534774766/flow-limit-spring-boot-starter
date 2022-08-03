package cn.sinohealth.flowlimit.springboot.starter.config.redisSerializeConfig;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * redis配置
 *
 * @author LiGuo
 * @since 2019-01-28
 */
@Configuration
@Order(Integer.MAX_VALUE)
public class RedisConfig {
    /**
     * RedisSerializer.json() = new GenericJackson2JsonRedisSerializer()
     *
     * @return [
     * "java.util.ArrayList"
     * [
     * "4adff99730ce6acdfd6571de9248bb2b"
     * "47bc7eacef0145cbb9fcd4d4c16e6793"
     * "178d6382d81aabae07c04d630dcb4e32"
     * "0a3ac33b13a400887a37ae6a16c2810f"
     * "16314bf7af134faf8fca92094a6b1cf8"
     * ]
     * ]
     */
    @Bean
    public GenericJackson2JsonRedisSerializer createGenericJackson2JsonRedisSerializer() {
//        RedisSerializer.json()
        return new GenericJackson2JsonRedisSerializer();
    }


    /**
     * Jackson2JsonRedisSerializer
     * <p>
     * [
     * "4adff99730ce6acdfd6571de9248bb2b"
     * "47bc7eacef0145cbb9fcd4d4c16e6793"
     * "178d6382d81aabae07c04d630dcb4e32"
     * "0a3ac33b13a400887a37ae6a16c2810f"
     * "16314bf7af134faf8fca92094a6b1cf8"
     * ]
     *
     * @return
     */
    @Bean
    public RedisSerializer<Object> jackson2JsonRedisSerializer() {
        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        serializer.setObjectMapper(mapper);
        return serializer;
    }


    /**
     * @param
     * @return
     */
//    @Bean
//    @ConditionalOnMissingBean
//    public RedisTemplate<String, Object> userInfoRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(factory);
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer();
//        redisTemplate.setKeySerializer(stringRedisSerializer);
//        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);
//        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
//        return redisTemplate;
//    }


}
