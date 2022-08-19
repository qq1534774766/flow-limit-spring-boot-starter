package cn.sinohealth.flowlimit.springboot.starter.test;

import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractGlobalTokenBucketFlowLimitAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/19 14:35
 * @Description: TODO
 */
@Component
@Aspect
public class TokenBucketConfig extends AbstractGlobalTokenBucketFlowLimitAspect {
    @Override
    protected boolean filterRequest(JoinPoint obj) {
        return false;
    }


    @Override
    protected Object rejectHandle(JoinPoint obj) throws Throwable {
        throw new Exception("流量高峰期，服务器正在紧急处理，请稍后再试！");
    }

    @Override
    @Pointcut("within(cn.sinohealth.flowlimit.springboot.starter.test.TestController)" +
            "&&@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void pointcut() {
    }
}
