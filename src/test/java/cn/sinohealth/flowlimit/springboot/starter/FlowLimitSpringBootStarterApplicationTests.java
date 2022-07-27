package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.utils.RedisCacheUtil;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class FlowLimitSpringBootStarterApplicationTests {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;


    @Test
    void contextLoads() throws Throwable {
        String key = "k2";
        String LUA_INC_SCRIPT_TEXT = "return 3";
        DefaultRedisScript<Long> REDIS_INC_SCRIPT = new DefaultRedisScript<>(LUA_INC_SCRIPT_TEXT, Long.class);
        ArrayList<String> strings = new ArrayList<>();
        strings.add(key);
        Long execute = redisTemplate.execute(REDIS_INC_SCRIPT, strings, 100);
        System.out.println(execute);

    }

    @Test
    void test() throws Throwable {
        String key = "key1";
//        redisTemplate.opsForValue().setIfAbsent(key,1,1L,TimeUnit.SECONDS);
//        Thread.sleep(1500);
//        Object o = redisTemplate.opsForValue().get(key);
//        System.out.println(o);
        redisTemplate.opsForValue().increment(key);
        Object o = redisTemplate.opsForValue().get(key);
        System.out.println(o);


    }

}
