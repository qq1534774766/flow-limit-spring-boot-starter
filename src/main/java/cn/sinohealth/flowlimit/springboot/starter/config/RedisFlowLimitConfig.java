package cn.sinohealth.flowlimit.springboot.starter.config;

import cn.sinohealth.flowlimit.springboot.starter.service.aspect.impl.RedisLimitFlowAspect;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 17:56
 * @Description: TODO
 */
@Configuration
@Aspect
public class RedisFlowLimitConfig extends RedisLimitFlowAspect {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    protected void pointcut() {
    }

    @Override
    protected void restructureCounterKey(List<String> counterKey) {

    }


}
