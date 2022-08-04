package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.IFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.AbstractRedisFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.IFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.interceptor.AbstractRedisFlowLimitInterceptor;
import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.utils.RedisFlowLimitTemplateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
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
import java.util.concurrent.atomic.AtomicBoolean;

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
    @ConditionalOnClass({RedisOperations.class})
    @AutoConfigureAfter({RedisAutoConfiguration.class})
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class RedisFlowLimitConfiguration implements ApplicationContextAware {

        @Bean
        @ConditionalOnClass(RedisConnectionFactory.class)
        public RedisFlowLimitTemplateHelper redisFlowLimitTemplateHelper(RedisConnectionFactory redisConnectionFactory) {
            return new RedisFlowLimitTemplateHelper(redisConnectionFactory);
        }

        @Bean
        @ConditionalOnBean({IFlowLimit.class})
        public FlowLimitProperties.RedisFlowLimitProperties redisFlowLimitProperties(FlowLimitProperties flowLimitProperties) {
            FlowLimitProperties.RedisFlowLimitProperties redisFlowLimitProperties = flowLimitProperties.getRedisFlowLimitProperties();
            if (ObjectUtils.isEmpty(redisFlowLimitProperties)) return null;
            int size1 = redisFlowLimitProperties.getCounterLimitNumber().size();
            int size2 = redisFlowLimitProperties.getCounterHoldingTime().size();
            int size3 = Optional.ofNullable(redisFlowLimitProperties.getCounterKeys()).map(List::size).orElse(0);
            if (size3 == 0) {
                log.error("未指定计数器的key，建议在application.yaml指定，否则默认计数器的key使用的是UUID");
                log.error("可在flowlimit->redis-flow-limit-properties->counter-keys指定");
            }
            if (size1 != size2) {
                throw new IllegalArgumentException("redis计数器的key数量与相应配置值数量不一致！");
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
                    for (IFlowLimitAspect i : iFlowLimitAspectMap.values()) {
                        log.info("流量限制启动成功！实现类：{}", i.getClass().getName());
                    }
                    for (IFlowLimitInterceptor i : iFlowLimitInterceptorMap.values()) {
                        log.info("流量限制启动成功！实现类：{}", i.getClass().getName());
                    }
                }
            }
        }

        @Autowired(required = false)
        public void redisFlowLimitInterceptor(AbstractRedisFlowLimitInterceptor redisFlowLimitInterceptor,
                                              FlowLimitProperties flowLimitProperties,
                                              RedisFlowLimitTemplateHelper redisHelper) {
            AbstractRedisFlowLimitAspect redisFlowLimitAspect = redisFlowLimitInterceptor.getRedisFlowLimitAspect();
            redisFlowLimitAspect.setRedisTemplate(redisHelper)
                    .setCounterKeyProperties(flowLimitProperties.getRedisFlowLimitProperties());
            redisFlowLimitAspect.initBeanProperties();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "flowlimit", value = {"enabled"}, havingValue = "true")
    static class MysqlFlowLimitConfiguration {
    }
}
