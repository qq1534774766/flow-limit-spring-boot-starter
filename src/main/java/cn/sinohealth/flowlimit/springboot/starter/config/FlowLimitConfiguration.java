package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.FlowLimitConfigurer;
import cn.sinohealth.flowlimit.springboot.starter.FlowLimitStrategyFactory;
import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.MysqlFlowLimitAspectImpl;
import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/27 9:55
 * @Description: TODO
 */
abstract class FlowLimitConfiguration {


    @Configuration
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class BaseFlowLimitConfiguration {
        @Bean
        public FlowLimitStrategyFactory flowLimitStrategyFactory() {
            return new FlowLimitStrategyFactory();
        }

        @Bean
        public FlowLimitConfigurer flowLimitConfigurer(FlowLimitProperties flowLimitProperties,
                                                       FlowLimitStrategyFactory flowLimitStrategyFactory) {
            FlowLimitConfigurer.flowLimitStrategyImplClassName = flowLimitProperties.getFlowLimitStrategyImplClass();
            FlowLimitConfigurer.flowLimitStrategyFactory = flowLimitStrategyFactory;
            return new FlowLimitConfigurer();
        }
    }

    @Configuration
    @AutoConfigureAfter({RedisAutoConfiguration.class})
    @ConditionalOnBean(FlowLimitStrategyFactory.class)
    static class RedisFlowLimitConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "flowlimit", name = "redis-flow-limit-properties.prefix-key")
        public FlowLimitProperties.RedisFlowLimitProperties redisFlowLimitProperties(FlowLimitProperties flowLimitProperties) {
            FlowLimitProperties.RedisFlowLimitProperties redisFlowLimitAspectProperties = flowLimitProperties.getRedisFlowLimitProperties();
            int size1 = redisFlowLimitAspectProperties.getCounterLimitNumber().size();
            int size2 = redisFlowLimitAspectProperties.getCounterHoldingTime().size();
            int size3 = redisFlowLimitAspectProperties.getCounterKeys().size();
            if (size1 == 0) {
                throw new IllegalArgumentException("redis计数器的key数量最少为1");
            }
            if (!(size1 == size2 && size1 == size3)) {
                throw new IllegalArgumentException("redis计数器的key数量与相应配置值数量不一致！");
            }
            return redisFlowLimitAspectProperties;
        }
    }

    @Configuration
    @ConditionalOnBean(FlowLimitStrategyFactory.class)
    static class MysqlFlowLimitConfiguration {
//        @Bean
//        public MysqlFlowLimitAspectImpl mysqlFlowLimitAspect(FlowLimitProperties flowLimitProperties) {
//            return new MysqlFlowLimitAspectImpl();
//        }
    }
}
