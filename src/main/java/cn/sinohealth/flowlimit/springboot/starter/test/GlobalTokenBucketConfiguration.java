package cn.sinohealth.flowlimit.springboot.starter.test;

import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractGlobalTokenBucketFlowLimitAspect;
import org.aspectj.lang.JoinPoint;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/22 14:09
 * @Description: TODO
 */
//@Configuration
public class GlobalTokenBucketConfiguration extends AbstractGlobalTokenBucketFlowLimitAspect {
    @Override
    protected boolean filterRequest(JoinPoint obj) {
        return false;
    }

    @Override
    protected Object rejectHandle(JoinPoint obj) throws Throwable {
        return null;
    }

    @Override
    public void pointcut() {

    }
}
