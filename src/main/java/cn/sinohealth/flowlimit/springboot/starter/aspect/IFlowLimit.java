package cn.sinohealth.flowlimit.springboot.starter.aspect;

import org.aspectj.lang.JoinPoint;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/28 9:54
 * @Description: 流量限制顶级接口，策略模式，单一职责
 */
public interface IFlowLimit {

    /**
     * 限流逻辑，如计数器方法、漏桶法、令牌桶等。
     *
     * @param joinPoint
     * @return true:当前请求达到计数/限流上限。
     */
    boolean limitProcess(JoinPoint joinPoint);

    /**
     * 当前策略之一,获取当前策略类型
     *
     * @return 策略具体类
     */
    Class<? extends IFlowLimit> getStrategyClass();
}
