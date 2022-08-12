package cn.sinohealth.flowlimit.springboot.starter.interceptor;

import cn.sinohealth.flowlimit.springboot.starter.aspect.impl.AbstractRedisFlowLimitAspect;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/29 11:07
 * @Description: 使用适配器模式，在RedisAspect基础上改造
 */
@Data
public abstract class AbstractRedisFlowLimitInterceptor
        implements IFlowLimitInterceptor, WebMvcConfigurer, ApplicationContextAware {

    private AbstractRedisFlowLimitAspect redisFlowLimitAspect = new RedisFlowLimitAspectImpl();
    private ThreadLocal<Map<String, Object>> threadLocalMap = new ThreadLocal<>();

    private class RedisFlowLimitAspectImpl extends AbstractRedisFlowLimitAspect {

        @Override
        protected boolean filterRequest(JoinPoint joinPoint) {
            return AbstractRedisFlowLimitInterceptor.this.filterRequest(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                    getHandlerFromThreadLocalSafely());
        }

        @Override
        protected boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint joinPoint) {
            return AbstractRedisFlowLimitInterceptor.this.beforeLimitingHappenWhetherContinueLimit(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                    getHandlerFromThreadLocalSafely());
        }

        @Override
        protected Object rejectHandle(JoinPoint joinPoint) throws Throwable {
            AbstractRedisFlowLimitInterceptor.this.rejectHandle(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                    getHandlerFromThreadLocalSafely());
            return false;
        }

        @Override
        public String appendCounterKeyWithMode() {
            return "interceptor:";
        }

        @Override
        protected String appendCounterKeyWithUserId(JoinPoint joinPoint) {
            return AbstractRedisFlowLimitInterceptor.this.appendCounterKeyWithUserId(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                    getHandlerFromThreadLocalSafely());
        }

        @Override
        protected Object otherHandle(JoinPoint joinPoint, boolean isReject, Object rejectResult) throws Throwable {
            //true放行
            if (ObjectUtils.isNotEmpty(rejectResult) && rejectResult instanceof Boolean) {
                return rejectResult;
            }
            //被拒绝 isReject=true，返回false
            //没有被拒绝
            return !isReject;
        }

        private HttpServletRequest getRequestFromThreadLocalSafely() {
            return (HttpServletRequest) Optional.ofNullable(threadLocalMap.get())
                    .map(o -> o.get("request"))
                    .orElse(null);
        }

        private HttpServletResponse getResponseFromThreadLocalSafely() {
            return (HttpServletResponse) Optional.ofNullable(threadLocalMap.get())
                    .map(o -> o.get("response"))
                    .orElse(null);
        }

        private Object getHandlerFromThreadLocalSafely() {
            return Optional.ofNullable(threadLocalMap.get())
                    .map(o -> o.get("handler"))
                    .orElse(null);
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
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocalMap.remove();//防止内存泄漏
    }

    private static Map<String, AbstractRedisFlowLimitInterceptor> beansOfType;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //取出用户实现的拦截器
        beansOfType = applicationContext.getBeansOfType(AbstractRedisFlowLimitInterceptor.class);
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
