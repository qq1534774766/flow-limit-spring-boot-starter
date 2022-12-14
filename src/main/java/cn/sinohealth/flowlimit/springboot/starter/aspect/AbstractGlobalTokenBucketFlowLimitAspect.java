package cn.sinohealth.flowlimit.springboot.starter.aspect;

import cn.sinohealth.flowlimit.springboot.starter.AbstractFlowLimit;
import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.utils.StartTipUtil;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/19 14:25
 * @Description: 全局流量限制，使用的是Google包下的RateLimiter类，本质就是 令牌桶方式限流.
 * 因为令牌桶，令牌是无状态的，无法记录用户的信息，因此只能作为全局限流使用。
 */
public abstract class AbstractGlobalTokenBucketFlowLimitAspect
        extends AbstractFlowLimit<JoinPoint> implements IFlowLimitAspect<JoinPoint> {
    private static RateLimiter rateLimiter;
    private static Long timeout;

    private static Integer tokenAcquire = 1;

    public AbstractGlobalTokenBucketFlowLimitAspect() {
    }

    @Autowired(required = false)
    public void initRateLimiter(FlowLimitProperties.GlobalTokenBucketFlowLimitProperties tokenBucketProperties) {
        rateLimiter = RateLimiter.create(tokenBucketProperties.getPermitsPerSecond(), tokenBucketProperties.getWarmupPeriod(), TimeUnit.MILLISECONDS);
        timeout = tokenBucketProperties.getTimeout();
        setEnabled(ObjectUtils.isNotEmpty(rateLimiter));
        if (isEnabled()) StartTipUtil.showBanner();
    }

    @Around("pointcut()")
    public Object adviceMode(JoinPoint joinPoint) throws Throwable {
        return this.flowLimitProcess(joinPoint);
    }

    /**
     * 获取令牌成功返回FALSE，失败则返回TRUE。
     *
     * @param obj
     * @return FALSE 不限流，TRUE：限流
     */
    @Override
    public boolean limitProcess(JoinPoint obj) {
        return !rateLimiter.tryAcquire(tokenAcquire, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 对外提供设置限速器的速度的方法
     *
     * @param permitsPerSecond
     */
    public void setRateLimiterRate(double permitsPerSecond) {
        AbstractGlobalTokenBucketFlowLimitAspect.rateLimiter.setRate(permitsPerSecond);
    }

    /**
     * 对外提供，设置获取令牌的超时时长
     *
     * @param timeout
     */
    public static void setTimeout(Long timeout) {
        AbstractGlobalTokenBucketFlowLimitAspect.timeout = timeout;
    }

    /**
     * 对外提供，设置单次请求需要消耗令牌数量
     *
     * @param tokenAcquire
     */
    public static void setTokenAcquire(Integer tokenAcquire) {
        AbstractGlobalTokenBucketFlowLimitAspect.tokenAcquire = tokenAcquire;
    }

    /**
     * 令牌桶算法不能重置限速器。
     *
     * @param obj 连接点
     * @return
     */
    @Override
    public final Object resetLimiter(JoinPoint obj) {
        return null;
    }

    /**
     * 既然不能重构限速器，那么本方法同样没有意义
     *
     * @param obj
     * @return
     */
    @Override
    protected final boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint obj) {
        return false;
    }

}
