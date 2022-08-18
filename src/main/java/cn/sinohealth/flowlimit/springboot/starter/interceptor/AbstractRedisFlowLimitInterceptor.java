package cn.sinohealth.flowlimit.springboot.starter.interceptor;

import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractRedisFlowLimitAspect;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
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
 * @Description: 使用基于类的适配器模式，在RedisAspect基础上改造
 */
public abstract class AbstractRedisFlowLimitInterceptor extends AbstractRedisFlowLimitAspect
        implements IFlowLimitInterceptor, WebMvcConfigurer {

    //region 成员变量
    /**
     * 存放HttpServletRequest，HttpServletResponse
     */
    private final ThreadLocal<Map<String, Object>> threadLocalMap = new ThreadLocal<>();
    /**
     * 拦截器自己，在AutoConfiguration中获取用户实现的拦截器
     */
    private AbstractRedisFlowLimitInterceptor own;
    //endregion

    //region 拦截器方法
    @Override
    public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!isEnabled()) return true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("request", request);
        map.put("response", response);
        map.put("handler", handler);
        threadLocalMap.set(map);
        try {
            return (boolean) flowLimitProcess(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocalMap.remove();//防止内存泄漏
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册用户的拦截器
        setInterceptorPathPatterns(registry.addInterceptor(getOwn()));
    }

    @Override
    public String appendCounterKeyWithMode() {
        return "interceptor:";
    }

    /**
     * 设置拦截器的拦截配置，比如路径配置等
     *
     * @param registry
     */
    public abstract void setInterceptorPathPatterns(InterceptorRegistration registry);
    //endregion

    //region 适配器方法，为了拦截器方法能适配AOP的方法
    @Override
    protected boolean filterRequest(JoinPoint obj) {
        return filterRequest(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                getHandlerFromThreadLocalSafely());
    }

    @Override
    protected boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint obj) {
        return beforeLimitingHappenWhetherContinueLimit(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                getHandlerFromThreadLocalSafely());
    }

    @Override
    protected Object rejectHandle(JoinPoint obj) throws Throwable {
        rejectHandle(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                getHandlerFromThreadLocalSafely());
        return false;
    }


    @Override
    protected String appendCounterKeyWithUserId(JoinPoint joinPoint) {
        return AbstractRedisFlowLimitInterceptor.this.appendCounterKeyWithUserId(getRequestFromThreadLocalSafely(), getResponseFromThreadLocalSafely(),
                getHandlerFromThreadLocalSafely());
    }

    //endregion
    @Override
    protected Object otherHandle(JoinPoint obj, boolean isReject, Object rejectResult) throws Throwable {
        //true放行
        if (ObjectUtils.isNotEmpty(rejectResult) && rejectResult instanceof Boolean) {
            return rejectResult;
        }
        //被拒绝 isReject=true，返回false
        //没有被拒绝
        return !isReject;
    }

    //region 适配时需要的转化方法，从ThreadLocal取出拦截器需要的字段
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
    //endregion

    //region 其他方法

    /**
     * 最终方法，因为拦截器适配了AOP 因此本方法失去了意义
     */
    @Override
    public final void pointcut() {
    }

    public AbstractRedisFlowLimitInterceptor getOwn() {
        return own;
    }

    public void setOwn(AbstractRedisFlowLimitInterceptor own) {
        this.own = own;
    }
    //endregion
}
