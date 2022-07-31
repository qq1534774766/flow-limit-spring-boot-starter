package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.IFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.RedisFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.IFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.RedisFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/27 9:55
 * @Description: TODO
 */
@Slf4j
abstract class FlowLimitConfiguration {


    @Configuration
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class BaseFlowLimitConfiguration {


    }

    @Configuration
    @AutoConfigureAfter({RedisAutoConfiguration.class})
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class RedisFlowLimitConfiguration implements ApplicationContextAware {
        @Autowired
        public void redisFlowLimitBootTest(RedisTemplate<String, Object> redisTemplate) {
            if (ObjectUtils.isEmpty(redisTemplate)) {
                log.error("Redis流量限制器未启动：RedisTemplate<String, Object> Bean 不存在");
            }
        }

        @Bean
        @ConditionalOnBean({IFlowLimit.class})
        public FlowLimitProperties.RedisFlowLimitProperties redisFlowLimitProperties(FlowLimitProperties flowLimitProperties) {
            FlowLimitProperties.RedisFlowLimitProperties redisFlowLimitProperties = flowLimitProperties.getRedisFlowLimitProperties();
            int size1 = redisFlowLimitProperties.getCounterLimitNumber().size();
            int size2 = redisFlowLimitProperties.getCounterHoldingTime().size();
            int size3 = redisFlowLimitProperties.getCounterKeys().size();
            if (size1 == 0) {
                throw new IllegalArgumentException("redis计数器的key数量最少为1");
            }
            if (!(size1 == size2 && size1 == size3)) {
                throw new IllegalArgumentException("redis计数器的key数量与相应配置值数量不一致！");
            }
            return redisFlowLimitProperties;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            if (ObjectUtils.isEmpty(applicationContext.getBeansOfType(IFlowLimit.class))) {
                log.error("1.Redis流量限制器未启动!");
                if (ObjectUtils.isEmpty(applicationContext.getBeansOfType(IFlowLimitAspect.class))) {
                    log.error("2.请确保{}被继承实现，且子类被Spring托管", RedisFlowLimitAspect.class.getSimpleName());
                }
                if (ObjectUtils.isEmpty(applicationContext.getBeansOfType(IFlowLimitInterceptor.class))) {
                    log.error("2.请确保{}被继承实现，且子类被Spring托管", RedisFlowLimitInterceptor.class.getSimpleName());
                }
            }
        }

        @Autowired(required = false)
        public void redisFlowLimitInterceptor(RedisFlowLimitInterceptor redisFlowLimitInterceptor,
                                              FlowLimitProperties flowLimitProperties,
                                              RedisTemplate<String, Object> redisTemplate) {
            RedisFlowLimitAspect redisFlowLimitAspect = redisFlowLimitInterceptor.getRedisFlowLimitAspect();
            redisFlowLimitAspect.setRedisTemplate(redisTemplate)
                    .setCounterKeyProperties(flowLimitProperties.getRedisFlowLimitProperties());
            redisFlowLimitAspect.initBeanProperties();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class MysqlFlowLimitConfiguration {
    }
}
