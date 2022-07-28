package cn.sinohealth.flowlimit.springboot.starter.aspect.impl;

import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimitAspect;
import org.aspectj.lang.JoinPoint;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/28 10:07
 * @Description: TODO
 */
public abstract class MysqlFlowLimitAspectImpl extends AbstractFlowLimitAspect
        implements IFlowLimitAspect {
    @Override
    public boolean limitProcess(JoinPoint joinPoint) {
        System.out.println("MySql限流模式开启");
        return false;
    }

    @Override
    public Class<? extends IFlowLimit> getStrategyClass() {
        return MysqlFlowLimitAspectImpl.class;
    }

    @Override
    protected Object resetLimiter(JoinPoint joinPoint) {
        return null;
    }
}
