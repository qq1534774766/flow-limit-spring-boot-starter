package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.service.FlowLimitService;
import cn.sinohealth.flowlimit.springboot.starter.service.RedisFlowLimitService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/27 9:55
 * @Description: TODO
 */
abstract class FlowLimitConfiguration {


    @Configuration
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class BaseConfiguration {
        @Bean
        public FlowLimitService flowLimitService(FlowLimitProperties flowLimitProperties) {
            return new FlowLimitService(flowLimitProperties);
        }
    }

    @Configuration
    @AutoConfigureAfter({RedisAutoConfiguration.class})
    static class RedisFlowLimitConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "flowlimit", name = "redis-flow-limit-aspect-properties.prefix-key")
        @ConditionalOnBean(FlowLimitService.class)
        public RedisFlowLimitService redisFlowLimitService(FlowLimitService flowLimitService) {
            return new RedisFlowLimitService(flowLimitService);
        }

    }
}
