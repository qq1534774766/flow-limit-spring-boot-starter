package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.utils.RedisCacheUtil;
import org.junit.jupiter.api.Test;
//import cn.sinohealth.flowlimit.springboot.starter.service.aspect.impl.RedisFlowLimitAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class FlowLimitSpringBootStarterApplicationTests {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
        String key = "key1211109";
        redisTemplate.opsForValue().set(key, 1, 10L, TimeUnit.SECONDS);

        redisTemplate.opsForValue().increment(key);

    }

}
