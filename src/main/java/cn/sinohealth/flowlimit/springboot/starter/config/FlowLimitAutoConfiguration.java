package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.service.FlowLimitService;
import cn.sinohealth.flowlimit.springboot.starter.service.RedisFlowLimitService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 11:01
 * @Description: 流量限制自动配置类，会被springboot自动加载
 */
@Configuration
@EnableConfigurationProperties(FlowLimitProperties.class)
@Import({FlowLimitConfiguration.BaseConfiguration.class,
        FlowLimitConfiguration.RedisFlowLimitConfiguration.class})
public class FlowLimitAutoConfiguration {


}
