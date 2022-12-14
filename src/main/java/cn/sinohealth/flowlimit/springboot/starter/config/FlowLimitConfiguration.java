package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.IFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractGlobalTokenBucketFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.AbstractGlobalTokenBucketFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.AbstractRedisFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.utils.FlowLimitCacheHelper;
import cn.sinohealth.flowlimit.springboot.starter.utils.StartTipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/27 9:55
 * @Description: 流量限制自动配置类，具体装配类
 */
@Slf4j
abstract class FlowLimitConfiguration {


    @Configuration
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class RedisFlowLimitConfiguration implements ApplicationContextAware {
        /**
         * 初始化缓存帮助器，初始化了Redis和local两种数据源
         *
         * @param counterFlowLimitProperties
         * @param redisConnectionFactory
         * @return
         */
        @Bean
        @ConditionalOnBean({IFlowLimit.class})
        public FlowLimitCacheHelper redisFlowLimitHelper(FlowLimitProperties.CounterFlowLimitProperties counterFlowLimitProperties,
                                                         @Autowired(required = false) RedisConnectionFactory redisConnectionFactory) {
            FlowLimitCacheHelper flowLimitCacheHelper = null;
            flowLimitCacheHelper = new FlowLimitCacheHelper(counterFlowLimitProperties.getDataSourceType());
            if (ObjectUtils.isNotEmpty(redisConnectionFactory)) {
                //redis可用
                flowLimitCacheHelper.initRedisStrategyService(redisConnectionFactory);
            }
            //初始化本地缓存
            flowLimitCacheHelper.initLocalStrategyService(counterFlowLimitProperties.getCounterHoldingTime(), counterFlowLimitProperties.getCounterHoldingTimeUnit());
            return flowLimitCacheHelper;
        }

        /**
         * 判断计数器的key是否合法
         *
         * @param flowLimitProperties
         * @return
         */
        @Bean
        @ConditionalOnBean({IFlowLimit.class})
        public FlowLimitProperties.CounterFlowLimitProperties redisFlowLimitProperties(FlowLimitProperties flowLimitProperties) {
            return StartTipUtil.tipCounterKeyAndProperties(flowLimitProperties);
        }

        /**
         * 发现本启动器，被实现类的子类有什么，打印日志
         *
         * @param applicationContext
         * @throws BeansException
         */
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            StartTipUtil.findFlowLimitInstance(applicationContext);
        }

        /**
         * 设置拦截器的自我字段，自我字段保存的是用户的实现类，为了将用户实现的列注册到MVC中
         *
         * @param redisFlowLimitInterceptor
         */
        @Autowired(required = false)
        public void redisFlowLimitInterceptor(AbstractRedisFlowLimitInterceptor redisFlowLimitInterceptor) {
            redisFlowLimitInterceptor.setOwn(redisFlowLimitInterceptor);
        }

        @Autowired(required = false)
        public void globalTokenBucketFlowLimitInterceptor(AbstractGlobalTokenBucketFlowLimitInterceptor interceptor) {
            interceptor.setOwn(interceptor);
        }
    }

    @Configuration
    @EnableCaching
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class CacheConfiguration {

    }

    @Configuration
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class GlobalTokenBucketConfiguration {
        /**
         * 全局令牌桶启动器配置
         *
         * @param flowLimitProperties
         * @return
         */
        @Bean
        @ConditionalOnBean({AbstractGlobalTokenBucketFlowLimitAspect.class})
        public FlowLimitProperties.GlobalTokenBucketFlowLimitProperties globalTokenBucketFlowLimitProperties(FlowLimitProperties flowLimitProperties) {
            return flowLimitProperties.getGlobaltokenBucketFlowLimitProperties();
        }

    }
}
