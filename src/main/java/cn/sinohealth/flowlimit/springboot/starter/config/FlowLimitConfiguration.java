package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.IFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractRedisFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.IFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.AbstractRedisFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.utils.FlowLimitCacheHelper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/27 9:55
 * @Description: TODO
 */
@Slf4j
abstract class FlowLimitConfiguration {


    @Configuration
    @ConditionalOnClass({RedisOperations.class})
    @AutoConfigureAfter({RedisAutoConfiguration.class})
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class RedisFlowLimitConfiguration implements ApplicationContextAware {

        @Bean
        @ConditionalOnClass(RedisConnectionFactory.class)
        public FlowLimitCacheHelper redisFlowLimitTemplateHelper(FlowLimitProperties.CounterFlowLimitProperties counterFlowLimitProperties, RedisConnectionFactory redisConnectionFactory) {

            FlowLimitCacheHelper flowLimitCacheHelper = new FlowLimitCacheHelper(counterFlowLimitProperties.getDataSourceType());
            flowLimitCacheHelper.initRedisStrategyService(redisConnectionFactory);
            flowLimitCacheHelper.initLocalStrategyService(caffeineMapBuilder(counterFlowLimitProperties));
            return flowLimitCacheHelper;
        }

        /**
         * key:当前计数器的保持时长，Caffeine 缓存对象
         *
         * @param counterFlowLimitProperties
         * @return
         */
        public Map<Long, Caffeine<Object, Object>> caffeineMapBuilder(FlowLimitProperties.CounterFlowLimitProperties counterFlowLimitProperties) {
            List<Long> counterHoldingTime = counterFlowLimitProperties.getCounterHoldingTime();
            TimeUnit timeUnit = counterFlowLimitProperties.getCounterHoldingTimeUnit();
            return counterHoldingTime.stream()
                    .collect(Collectors.toMap(timeUnit::toMillis, holdingTime -> {
                        return Caffeine.newBuilder()
                                .initialCapacity(Short.MAX_VALUE) //初始大小
                                .maximumSize(Long.MAX_VALUE)  //最大大小
                                .expireAfterAccess(timeUnit.toMillis(holdingTime), TimeUnit.MILLISECONDS); //时间单位
                    }));

        }

        @Bean
        @ConditionalOnBean({IFlowLimit.class})
        public FlowLimitProperties.CounterFlowLimitProperties redisFlowLimitProperties(FlowLimitProperties flowLimitProperties) {
            FlowLimitProperties.CounterFlowLimitProperties redisFlowLimitProperties = flowLimitProperties.getRedisFlowLimitProperties();
            if (ObjectUtils.isEmpty(redisFlowLimitProperties)) return null;
            int size1 = redisFlowLimitProperties.getCounterLimitNumber().size();
            int size2 = redisFlowLimitProperties.getCounterHoldingTime().size();
            int size3 = Optional.ofNullable(redisFlowLimitProperties.getCounterKeys()).map(List::size).orElse(0);
            if (size3 == 0) {
                log.error("未指定计数器的key，建议在application.yaml指定，否则默认计数器的key使用的是UUID");
                log.error("可在flowlimit->redis-flow-limit-properties->counter-keys指定");
            }
            if ((size3 != 0 && (!(size1 == size2 && size1 == size3))) || size1 != size2) {
                log.error("1.Redis流量限制器未启动!");
                log.error("application.yaml中，redis计数器的key数量与相应配置的属性数量不一致！");
            }
            return redisFlowLimitProperties;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            Map<String, IFlowLimit> iFlowLimitMap = applicationContext.getBeansOfType(IFlowLimit.class);
            Map<String, IFlowLimitAspect> iFlowLimitAspectMap = applicationContext.getBeansOfType(IFlowLimitAspect.class);
            Map<String, IFlowLimitInterceptor> iFlowLimitInterceptorMap = applicationContext.getBeansOfType(IFlowLimitInterceptor.class);
            Map<String, FlowLimitProperties> flowLimitProperties = applicationContext.getBeansOfType(FlowLimitProperties.class);
            AtomicBoolean enableRedisFlowLimit = new AtomicBoolean(false);
            flowLimitProperties.values().forEach(it -> enableRedisFlowLimit.set(ObjectUtils.isEmpty(it.getRedisFlowLimitProperties())));
            if (iFlowLimitMap.isEmpty()) {
                log.error("1.Redis流量限制器未启动!");
                if (iFlowLimitAspectMap.isEmpty()) {
                    log.error("2.请确保{}被继承实现，且子类被Spring托管", AbstractRedisFlowLimitAspect.class.getSimpleName());
                } else if (iFlowLimitInterceptorMap.isEmpty()) {
                    log.error("2.请确保{}被继承实现，且子类被Spring托管", AbstractRedisFlowLimitInterceptor.class.getSimpleName());
                }
            } else {
                if (!enableRedisFlowLimit.get()) {
                    for (IFlowLimit i : iFlowLimitMap.values()) {
                        log.info("发现[流量限制启动器]实现类：{}", i.getClass().getName());
                    }
                }
            }
        }

        @Autowired(required = false)
        public void redisFlowLimitInterceptor(AbstractRedisFlowLimitInterceptor redisFlowLimitInterceptor) {
            redisFlowLimitInterceptor.setOwn(redisFlowLimitInterceptor);
        }
    }

    @Configuration
    @EnableCaching
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class CacheConfiguration {
        @Bean(name = "oneHourCacheManager")
        public CacheManager oneHourCacheManager() {
            Caffeine caffeine = Caffeine.newBuilder()
                    .initialCapacity(10) //初始大小
                    .maximumSize(11)  //最大大小
                    .expireAfterWrite(1, TimeUnit.HOURS); //写入/更新之后1小时过期

            CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
            caffeineCacheManager.setAllowNullValues(true);
            caffeineCacheManager.setCaffeine(caffeine);
//            caffeineCacheManager.
            return caffeineCacheManager;
        }
    }
}
