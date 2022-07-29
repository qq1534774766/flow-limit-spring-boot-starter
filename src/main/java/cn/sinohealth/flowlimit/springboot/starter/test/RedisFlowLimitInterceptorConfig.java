package cn.sinohealth.flowlimit.springboot.starter.test;

import cn.sinohealth.flowlimit.springboot.starter.interceptor.RedisFlowLimitInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/29 11:10
 * @Description: TODO
 */
@Component
public class RedisFlowLimitInterceptorConfig extends RedisFlowLimitInterceptor {


    @Override
    public boolean filterRequest(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return false;
    }

    @Override
    public String appendCounterKeyWithUserId(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return null;
    }

    @Override
    public boolean beforeLimitingHappenWhetherContinueLimit(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return false;
    }

    @Override
    public void rejectHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        throw new Exception("拦截器拦截成功！");
    }

    @Override
    public void setInterceptorPathPatterns(InterceptorRegistration registry) {
        registry.addPathPatterns("/**/**");
    }
}
