package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.service.FlowLimitService;
import cn.sinohealth.flowlimit.springboot.starter.service.RedisFlowLimitService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 11:01
 * @Description: 流量限制自动配置类，会被springboot自动加载
 */
@Configuration
@EnableConfigurationProperties(FlowLimitProperties.class)
@ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
public class FlowLimitAutoConfig {
    private final FlowLimitProperties flowLimitProperties;

    public FlowLimitAutoConfig(FlowLimitProperties flowLimitProperties) {
        this.flowLimitProperties = flowLimitProperties;
    }

    @Bean
    public FlowLimitService flowLimitService() {
        return new FlowLimitService(flowLimitProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "flowlimit", name = "redis-limit-flow-aspect.prefix-key", matchIfMissing = false)
    public RedisFlowLimitService redisFlowLimitService(FlowLimitService flowLimitService) {
        return new RedisFlowLimitService(flowLimitService);
    }


}
