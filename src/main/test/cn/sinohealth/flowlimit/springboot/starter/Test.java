package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.utils.FlowLimitCacheHelper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/3 13:31
 * @Description: TODO
 */
@SpringBootTest
public class Test {
    @Autowired
    FlowLimitCacheHelper flowLimitCacheHelper;

    @org.junit.jupiter.api.Test
    void test() throws Exception {
        @NonNull Cache<Object, Object> c = Caffeine.newBuilder()
                .initialCapacity(10)
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .build();
        CaffeineCache caffeineCache = new CaffeineCache("one", c);
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Collections.singleton(caffeineCache));
        org.springframework.cache.Cache cache = cacheManager.getCache("one");

        Thread.sleep(1002);
        System.out.println(cache.get(1));


    }

    @org.junit.jupiter.api.Test
    void test1() throws Exception {
        Integer i = null;
        System.out.println(i + 1);

    }
}
