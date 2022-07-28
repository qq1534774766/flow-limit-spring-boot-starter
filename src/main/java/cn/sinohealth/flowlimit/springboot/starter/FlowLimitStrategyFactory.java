package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.RedisFlowLimitAspectImpl;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/28 10:40
 * @Description: 限制策略工厂，交给工厂来选择类，从而处理限制逻辑
 */
public class FlowLimitStrategyFactory implements ApplicationContextAware {
    private static final Class<? extends IFlowLimit> DEFAULT_FLOW_LIMIT_STRATEGY_CLASS = RedisFlowLimitAspectImpl.class;
    private Map<Class<? extends IFlowLimit>, IFlowLimit> strategyMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IFlowLimit> beansOfType = applicationContext.getBeansOfType(IFlowLimit.class);
        beansOfType.values().forEach(bean -> strategyMap.put(bean.getStrategyClass(), bean));
    }

    /**
     * 限流逻辑，如计数器方法、漏桶法、令牌桶等。
     *
     * @param joinPoint     连接点
     * @param strategyClass 限流策略实现类
     * @return true:当前请求达到上限。
     */
    public boolean limitProcess(JoinPoint joinPoint, Class<? extends IFlowLimit> strategyClass) {
        return Optional.ofNullable(Optional.ofNullable(strategyMap.get(strategyClass))
                        .orElse(strategyMap.get(DEFAULT_FLOW_LIMIT_STRATEGY_CLASS)))
                .map(o -> o.limitProcess(joinPoint)).orElse(false);
    }
}
