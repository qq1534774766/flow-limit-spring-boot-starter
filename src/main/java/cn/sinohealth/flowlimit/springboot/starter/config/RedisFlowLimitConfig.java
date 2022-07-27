package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.service.aspect.impl.RedisFlowLimitAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 17:56
 * @Description: TODO
 */
@Configuration
@Aspect
public class RedisFlowLimitConfig extends RedisFlowLimitAspect {

    @Pointcut("within(cn.sinohealth.flowlimit.springboot.starter.TestController)" +
            "&&@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    protected void pointcut() {
    }

    @Override
    protected boolean filterRequest(JoinPoint joinPoint) {
        return false;
    }

    @Override
    protected boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint joinPoint) {
        return false;
    }

    @Override
    protected Object rejectHandle(JoinPoint joinPoint) throws Throwable {
        throw new Exception("接口调用频繁");
    }


    @Override
    protected String appendCounterKeyWithUserId(JoinPoint joinPoint) {
        return null;
    }
}
