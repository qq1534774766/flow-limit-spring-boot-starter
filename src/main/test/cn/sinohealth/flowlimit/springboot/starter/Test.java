package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.utils.RedisFlowLimitTemplateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/3 13:31
 * @Description: TODO
 */
@SpringBootTest
public class Test {
    @Autowired
    private RedisFlowLimitTemplateHelper redisHelper;
    private static final String LUA_INC_SCRIPT_TEXT =
            "local counterKey = KEYS[1]; " +
                    "local timeout = ARGV[1]; " +
                    "local countMax = ARGV[2]; " +
                    "local currentCount = redis.call('get', counterKey); " +
                    "if currentCount and tonumber(currentCount) > tonumber(countMax) then " +
                    "return 0; " +
                    "end " +
                    "currentCount = redis.call('incr',counterKey); " +
                    "if tonumber(currentCount) == 1 then " +
                    "redis.call('pexpire', counterKey, timeout); " +
                    "end " +
                    "return 1; ";

    /*                    "local setSuccessfully = redis.call('set',counterKey,1,'px',timeout,'nx');" +
                        " if(setSuccessfully) then" +
                            " return 1;" +
                        " else" +
                            " redis.call('incr',counterKey);" +
                            " local keyTtl = redis.call('ttl',counterKey);" +
                            " if(keyTtl==-1) then" +
                                " redis.call('set',counterKey,1,'px',timeout,'xx');" +
                                " return 1;" +
                            " end" +
                        " end" +
                        "local currentCount = redis.call('get', counterKey);" +
                        "if currentCount and tonumber(currentCount) > countMax then" +
                                "" +
                        " return tonumber(redis.call('get',counterKey));";*/
    private static final DefaultRedisScript<Long> REDIS_INC_SCRIPT = new DefaultRedisScript<>(LUA_INC_SCRIPT_TEXT, Long.class);


    @org.junit.jupiter.api.Test
    void test() {
        //设置key成功: 1
        // 原来的key自增失败，重设新的key: 2
        // key自增成功: 3
//        countMax
        for (int i = 0; i < 10; i++) {
            Long k1 = redisHelper.execute(REDIS_INC_SCRIPT, Collections.singletonList("k1"), 10000, 8);
            System.out.println(k1);
        }
    }
}
