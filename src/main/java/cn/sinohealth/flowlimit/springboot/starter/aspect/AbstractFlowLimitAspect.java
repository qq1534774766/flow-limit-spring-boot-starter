package cn.sinohealth.flowlimit.springboot.starter.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/25 10:02
 * @Description: 限流、反爬抽象类。 《模板方法模式》，子类可以继承该类，以实现不同的限制策略
 * <br/>
 */
public abstract class AbstractFlowLimitAspect
        implements IFlowLimit, IFlowLimitAspect {
    /**
     * 是否启用流量限制
     */
    protected static boolean enabled;

    /**
     * 定义切入点，子类<strong>必须</strong>重写并指定连接点
     */
    public void pointcut() {
    }

    /**
     * 定义增强方式，默认使用环绕增强
     * <br/>
     * 不建议子类重写。如需重写，则<strong>必须</strong>回调父类的 flowLimitProcess(joinPoint)方法！
     */
    @Around("pointcut()")
    public Object adviceMode(JoinPoint joinPoint) throws Throwable {
        return flowLimitProcess(joinPoint);
    }

    /**
     * 过滤不进行限制的请求，比如登录注册、文件下载、静态资源等
     *
     * @return true:表示过滤该请求，即不限制该请求，false限制该请求
     */
    protected abstract boolean filterRequest(JoinPoint joinPoint);

    /**
     * 定义模板方法，禁止子类重写方法
     */
    protected final Object flowLimitProcess(JoinPoint joinPoint) throws Throwable {
        if (!enabled) {
            return otherHandle(joinPoint, false, null);
        }
        if (filterRequest(joinPoint)) {
            return otherHandle(joinPoint, false, null);
        }
        //限流逻辑
        Object rejectResult = null;
        boolean isReject = false;
        //1.限流计数器计数。
        if (limitProcess(joinPoint)) {
            // 2.限流前置操作
            if (beforeLimitingHappenWhetherContinueLimit(joinPoint)) {
                resetLimiter(joinPoint);
            } else {
                //执行拒绝策略
                isReject = true;
                rejectResult = rejectHandle(joinPoint);
            }
        }
        //其他操作，如验证通过重置限制计数器等。最后返回执行结果
        return otherHandle(joinPoint, isReject, rejectResult);
    }


    /**
     * 在限制发生之前是否继续限制
     * <br/>
     * 可以反馈客户端滑动验证码，手机验证码登录验证操作。
     *
     * @return TRUE：用户完成验证->清空计数器->放行。FALSE：未完成验证，执行拒绝策略。
     */
    protected abstract boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint joinPoint);

    /**
     * 拒绝策略，真正执行拒绝操作
     * <br/>
     * 可以进行拒绝操作,如 1.抛出异常，或者2.返回错误信息。
     * @return 1.抛出异常：无需返回任何东西 <br/>
     * 2.错误信息：返回的类型与Controller返回类型<strong>必须</strong>一致
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
        if (!isReject && joinPoint instanceof ProceedingJoinPoint) {
            //默认：拒绝策略未执行或执行了但选择放行，rejectResult即为null，若使用的是AOP中的环绕增强，则执行
            return ((ProceedingJoinPoint) joinPoint).proceed();
        }
        //执行拒绝策略并拒绝  -->取消调用接口
        // 非环绕方法。  -->无需调用，即null
        return rejectResult;
    }

    /**
     * 重置计数器、限流等
     *
     * @param joinPoint 连接点
     * @return 保留返回、按需使用
     */
    protected abstract Object resetLimiter(JoinPoint joinPoint);
}
