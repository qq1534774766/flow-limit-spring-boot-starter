package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.service.FlowLimitService;
import cn.sinohealth.flowlimit.springboot.starter.service.RedisFlowLimitService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FlowLimitConfig {
    private final FlowLimitProperties flowLimitProperties;

    public FlowLimitConfig(FlowLimitProperties flowLimitProperties) {
        this.flowLimitProperties = flowLimitProperties;
    }

    @Bean
    public FlowLimitService flowLimitService() {
        return new FlowLimitService(flowLimitProperties);
    }

    @Bean
    public RedisFlowLimitService redisFlowLimitService(FlowLimitService flowLimitService) {
        return new RedisFlowLimitService(flowLimitService);
    }


}
