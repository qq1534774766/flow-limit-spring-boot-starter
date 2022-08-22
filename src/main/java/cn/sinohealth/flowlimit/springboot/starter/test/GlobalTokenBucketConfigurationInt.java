package cn.sinohealth.flowlimit.springboot.starter.test;

import cn.sinohealth.flowlimit.springboot.starter.interceptor.AbstractGlobalTokenBucketFlowLimitInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/22 14:45
 * @Description: TODO
 */
@Component
public class GlobalTokenBucketConfigurationInt extends AbstractGlobalTokenBucketFlowLimitInterceptor {
    @Override
    public void setInterceptorPathPatterns(InterceptorRegistration registry) {
        registry.addPathPatterns("/**/**");
    }

    @Override
    public boolean filterRequest(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return false;
    }


    @Override
    public boolean beforeLimitingHappenWhetherContinueLimit(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return false;
    }

    @Override
    public Object rejectHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json;");
        response.getWriter().write("接口调用频繁");
        response.setStatus(500);
        return handler;
    }
}
