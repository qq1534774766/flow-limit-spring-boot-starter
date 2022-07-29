package cn.sinohealth.flowlimit.springboot.starter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;

@SpringBootTest
class FlowLimitSpringBootStarterApplicationTests {


    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    private static final String LUA_INC_SCRIPT_TEXT =
            " if(redis.call('set',KEYS[1],1,'ex',ARGV[1],'nx')) then" +
                    " return -1;" +
                    " else" +
                    " redis.call('incr',KEYS[1]);" +
                    " local keyTtl = redis.call('ttl',KEYS[1]);" +
                    " if(keyTtl==-1) then" +
                    " redis.call('set',KEYS[1],1,'ex',ARGV[1],'xx');" +
                    " return -2;" +
                    " end" +
                    " end;" +
                    " return redis.call('get',KEYS[1]);";

    @Test
    void contextLoads() throws Throwable {
        String key = "k8";
//        String LUA_INC_SCRIPT_TEXT = "redis.call('set',KEYS[1],1);local a = redis.call('get',KEYS[1]);return a";
        String LUA_INC_SCRIPT_TEXT2 = " return redis.call('get',KEYS[1]);";
        ;
        DefaultRedisScript<Long> REDIS_INC_SCRIPT = new DefaultRedisScript<>(LUA_INC_SCRIPT_TEXT, Long.class);
        ArrayList<String> strings = new ArrayList<>();
        strings.add(key);
        Long execute = redisTemplate.execute(REDIS_INC_SCRIPT, strings, 1000);
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
        System.out.println(o.getClass().getName());


    }

}
