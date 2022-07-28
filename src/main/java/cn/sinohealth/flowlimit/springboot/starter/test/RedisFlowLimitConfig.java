package cn.sinohealth.flowlimit.springboot.starter.test;

import cn.sinohealth.flowlimit.springboot.starter.test.Result;
import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.RedisFlowLimitAspectImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Random;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 17:56
 * @Description: TODO
 */
//@Configuration
//@Aspect
public final class RedisFlowLimitConfig extends RedisFlowLimitAspectImpl {

    @Pointcut("within(cn.sinohealth.flowlimit.springboot.starter.test.TestController)" +
            "&&@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void pointcut() {
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
        return new Result(403, "接口调用频繁");
    }


    @Override
    protected String appendCounterKeyWithUserId(JoinPoint joinPoint) {
        return new Random().nextInt(1000) + "";
    }
}