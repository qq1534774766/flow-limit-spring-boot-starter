package cn.sinohealth.flowlimit.springboot.starter.interceptor;

import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.RedisFlowLimitAspect;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/29 11:07
 * @Description: 使用适配器模式，在RedisAspect基础上改造
 */
@Data
public abstract class RedisFlowLimitInterceptor
        implements IFlowLimitInterceptor, WebMvcConfigurer, ApplicationContextAware {

    private RedisFlowLimitAspect redisFlowLimitAspect = new RedisFlowLimitAspectImpl();
    private ThreadLocal<Map<String, Object>> threadLocalMap = new ThreadLocal<>();

    public class RedisFlowLimitAspectImpl extends RedisFlowLimitAspect {

        @Override
        protected boolean filterRequest(JoinPoint joinPoint) {
            return RedisFlowLimitInterceptor.this.filterRequest((HttpServletRequest) threadLocalMap.get().get("request"),
                    (HttpServletResponse) threadLocalMap.get().get("response"),
                    threadLocalMap.get().get("handler"));
        }

        @Override
        protected boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint joinPoint) {
            return RedisFlowLimitInterceptor.this.beforeLimitingHappenWhetherContinueLimit((HttpServletRequest) threadLocalMap.get().get("request"),
                    (HttpServletResponse) threadLocalMap.get().get("response"),
                    threadLocalMap.get().get("handler"));
        }

        @Override
        protected Object rejectHandle(JoinPoint joinPoint) throws Throwable {
            RedisFlowLimitInterceptor.this.rejectHandle((HttpServletRequest) threadLocalMap.get().get("request"),
                    (HttpServletResponse) threadLocalMap.get().get("response"),
                    threadLocalMap.get().get("handler"));
            return null;
        }

        @Override
        protected String appendCounterKeyWithUserId(JoinPoint joinPoint) {
            return RedisFlowLimitInterceptor.this.appendCounterKeyWithUserId((HttpServletRequest) threadLocalMap.get().get("request"),
                    (HttpServletResponse) threadLocalMap.get().get("response"),
                    threadLocalMap.get().get("handler"));
        }

        @Override
        protected Object otherHandle(JoinPoint joinPoint, boolean isReject, Object rejectResult) throws Throwable {
            return true;
        }

        @Override
        public final void pointcut() {
        }
    }

    @Override
    public boolean limitProcess(JoinPoint joinPoint) {
        return redisFlowLimitAspect.limitProcess(joinPoint);
    }

    @Override
    public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!redisFlowLimitAspect.isEnabled()) return true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("request", request);
        map.put("response", response);
        map.put("handler", handler);
        threadLocalMap.set(map);
        try {
            return (boolean) redisFlowLimitAspect.flowLimitProcess(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        threadLocalMap.remove();//防止内存泄漏
        IFlowLimitInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    Map<String, RedisFlowLimitInterceptor> beansOfType;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //取出用户实现的拦截器
        this.beansOfType = applicationContext.getBeansOfType(RedisFlowLimitInterceptor.class);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册用户的拦截器
        beansOfType.values().forEach(it -> setInterceptorPathPatterns(registry.addInterceptor(it)));
    }

    /**
     * 设置拦截器的拦截配置，比如路径配置等
     *
     * @param registry
     */
    public abstract void setInterceptorPathPatterns(InterceptorRegistration registry);
}
