package cn.sinohealth.flowlimit.springboot.starter.aspect.impl;

import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimit;
import org.aspectj.lang.JoinPoint;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/28 10:07
 * @Description: TODO
 */
public class MysqlFlowLimitAspectImpl extends AbstractFlowLimitAspect {
    @Override
    protected void pointcut() {

    }

    @Override
    protected boolean filterRequest(JoinPoint joinPoint) {
        return false;
    }

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
    protected boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint joinPoint) {
        return false;
    }

    @Override
    protected Object rejectHandle(JoinPoint joinPoint) throws Throwable {
        return null;
    }

}
