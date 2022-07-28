package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.RedisFlowLimitAspectImpl;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/28 13:48
 * @Description: 对外提供给用户使用，
 */

@Data
public class FlowLimitConfigurer {
    public static Class<? extends IFlowLimit> flowLimitStrategyImplClassName;
    public static FlowLimitStrategyFactory flowLimitStrategyFactory;

    public abstract static class RedisAspectFlowLimitConfigurer
            extends RedisFlowLimitAspectImpl {
        @Override
        @Around("pointcut()")
        public Object adviceMode(JoinPoint joinPoint) throws Throwable {
            //调用工厂方法，由工厂替我们做出选择
            return flowLimitStrategyFactory.limitProcess(joinPoint, flowLimitStrategyImplClassName);
        }

        @Override
        public final Class<? extends IFlowLimit> getStrategyClass() {
            return RedisFlowLimitAspectImpl.class;
        }

    }

    public abstract static class InterceptorFlowLimitConfigurer {

    }
}
