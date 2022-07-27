package cn.sinohealth.flowlimit.springboot.starter.service.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/25 10:02
 * @Description: 限流、反爬抽象类。 《模板方法模式》，子类可以继承该类，以实现不同的限制策略
 * <br/>
 */
public abstract class AbstractFlowLimitAspect {
    /**
     * 定义切入点，子类必须重写并指定连接点
     */
    @Pointcut()
    protected abstract void pointcut();

    /**
     * 定义增强方式。
     * <br/>
     * 默认前置增强
     */
    @Before("pointcut()")
    protected Object advice(JoinPoint joinPoint) throws Throwable {
        return flowLimitProcess(joinPoint);
    }

    /**
     * 定义模板方法，禁止子类重写方法
     */
    protected final Object flowLimitProcess(JoinPoint joinPoint) throws Throwable {
        //限流逻辑
        boolean isReject = limitProcess(joinPoint);
        //被限流, 回调拒绝策略方法
        Object rejectResult = null;
        if (isReject) {
            rejectResult = rejectHandle(joinPoint);
        }
        //其他操作，如验证通过重置限制计数器等。最后返回执行结果
        return otherHandle(joinPoint, isReject, rejectResult);
    }


    /**
     * 限流逻辑，如计数器方法、漏桶法、令牌桶等。
     *
     * @return true:当前请求被限制,即被拒绝。
     */
    protected abstract boolean limitProcess(JoinPoint joinPoint);

    /**
     * 拒绝策略，当被limitProcess返回TRUE被调用。
     * <br/>
     * 可以进行拒绝操作,如抛出异常。亦或者验证码通过后重置计数器等
     *
     * @return 拒绝策略执行结果，供其他操作使用
     */
    protected abstract Object rejectHandle(JoinPoint joinPoint) throws Throwable;

    /**
     * 其他操作，拒绝策略选择抛出异常的形式，则该方法不会被执行到！
     * <br/>
     * 用于放行环绕增强@Around正常调用操作。
     * <br/>
     * 当未执行拒绝策略时，且是环绕增强时，会自动执行相应方法。
     * <br/>
     * 你也可以重写该方法，自定义实现。
     *
     * @param joinPoint    连接点
     * @param rejectResult 拒绝策略执行结果。当且仅当拒绝策略被执行才不为null
     * @return 可以是ProcessJoinPoint.process()的方法执行结果，前提是使用的是环绕增强！
     */
    protected Object otherHandle(JoinPoint joinPoint, boolean isReject, Object rejectResult) throws Throwable {
        if (isReject && joinPoint instanceof ProceedingJoinPoint) {
            //默认：拒绝策略未执行或执行了但选择放行，rejectResult即为null，若使用的是AOP中的环绕增强，则执行
            return ((ProceedingJoinPoint) joinPoint).proceed();
        }
        //执行拒绝策略并拒绝  -->取消调用接口
        // 非环绕方法。  -->无需调用，即null
        return null;
    }

    /**
     * 重置计数器、限流等
     *
     * @param joinPoint 连接点
     * @return 保留返回、按需使用
     */
    protected Object resetLimiter(JoinPoint joinPoint) {
        return null;
    }
}
