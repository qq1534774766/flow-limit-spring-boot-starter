package cn.sinohealth.flowlimit.springboot.starter.service.aspect.impl;

import cn.sinohealth.flowlimit.springboot.starter.service.RedisFlowLimitService;
import cn.sinohealth.flowlimit.springboot.starter.service.aspect.AbstractLimitFlowAspect;
import cn.sinohealth.flowlimit.springboot.starter.utils.RedisCacheUtil;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/25 10:33
 * @Description: Redis数据源，计数器的方式限流。排除未登录用户
 */
@Component
@Data
@ConditionalOnBean({RedisCacheUtil.class, RedisFlowLimitService.class})
public abstract class RedisLimitFlowAspect extends AbstractLimitFlowAspect {

    private RedisCacheUtil redisCacheUtil;


    /**
     * 是否开启同步计数，如不要求精准限流，则无需开启。
     * <br/>
     * 开启后，将使用严格计数，开销一定性能。
     */
    private boolean enabledSyncCount;

    /**
     * 是否全局限制，即所有用户所有操作均被计数限制.
     * <br/>
     * FALSE则需要传递获取用户ID的方法。
     */
    private boolean enabledGlobalLimit;

    private static class CounterKeyProperties {
        /**
         * baseKey，即全局key前缀
         * <br/>形式：
         * 注意：这里的每个key已经加到counterKey中
         */
        private static String prefixKey;

        /**
         * 计数器的数量
         */
        private static Integer keyNumber;

        /**
         * 每个计数器的Key。
         * <pre>
         * 注意：这里的每个key已经有全局的前缀prefixKey
         *  <pre/>
         */
        private static List<String> counterKeys;

        /**
         * 每个计数器的保持时长，单位是毫秒
         */
        private static List<Long> counterHoldingTime;

        /**
         * 每个计数器对应的限流次数，即接口调用次数限制
         */
        private static List<Integer> counterLimitNumber;
        /**
         * 超时单位，毫秒
         */
        private static final TimeUnit HOLDING_TIME_UNIT = TimeUnit.MILLISECONDS;
    }


    /**
     * 【当用户超出限流时，需要判断key的存在性】
     * <br/>
     * key存在，表示用户通过了验证码的验证，重置限流计数器。
     * <br/>
     * key不存在，用户尚未完成验证，继续限流。
     */
    public static final String OVERSTEP_FLOW_VERIFICATION = "overstep:flow:verification";

    public RedisLimitFlowAspect() {

    }

    @Autowired
    public void setRedisCacheUtil(RedisCacheUtil redisCacheUtil) {
        this.redisCacheUtil = redisCacheUtil;
    }

    @Autowired
    public void setRedisFlowLimitService(RedisFlowLimitService redisFlowLimitService) {
        //封装公共属性
        this.enabledGlobalLimit = redisFlowLimitService.getRedisLimitFlowAspectProperties().isEnabledGlobalLimit();
        this.enabledSyncCount = redisFlowLimitService.getRedisLimitFlowAspectProperties().isEnabledSyncCount();
        //封装properties
        CounterKeyProperties.prefixKey = redisFlowLimitService.getRedisLimitFlowAspectProperties().getPrefixKey();
        CounterKeyProperties.counterKeys = redisFlowLimitService.getRedisLimitFlowAspectProperties().getCounterKeys().stream()
                .map(key -> CounterKeyProperties.prefixKey + key).collect(Collectors.toList());
        CounterKeyProperties.counterHoldingTime = redisFlowLimitService.getRedisLimitFlowAspectProperties().getCounterHoldingTime();
        CounterKeyProperties.counterLimitNumber = redisFlowLimitService.getRedisLimitFlowAspectProperties().getCounterLimitNumber();
        CounterKeyProperties.keyNumber = redisFlowLimitService.getRedisLimitFlowAspectProperties().getCounterKeys().size();
    }

    /**
     * 定义切入点
     */
    @Override
    @Pointcut()
    protected abstract void pointcut();


    @Override
    protected final boolean limitProcess(JoinPoint joinPoint) {
        List<String> counterKey = CounterKeyProperties.counterKeys;
        if (!enabledGlobalLimit) {
            //未开启全局计数，即计数器要拼接的用户ID，对每一个用户单独限流
            restructureCounterKey(counterKey);
        }
        List<Long> counterHoldingTime = CounterKeyProperties.counterHoldingTime;
        List<Integer> counterLimitNumber = CounterKeyProperties.counterLimitNumber;
        //当前计数器是否限制？
        boolean currentIsLimit = false;
        //遍历计数器
        if (!enabledSyncCount) {
            //未开启严格计数
            for (int i = 0; i < counterKey.size() && !currentIsLimit; i++) {
                currentIsLimit = counterProcess(counterKey.get(i), counterHoldingTime.get(i), counterLimitNumber.get(i));
            }
        } else {
            //开启严格计数
            for (int i = 0; i < counterKey.size() && !currentIsLimit; i++) {
                currentIsLimit = counterProcessWithLock(counterKey.get(i), counterHoldingTime.get(i), counterLimitNumber.get(i),
                        CounterKeyProperties.prefixKey + "lock:");
            }
        }
        //当且仅当所有计数器都返回false才不限制
        return currentIsLimit;
    }

    /**
     * 重构计数器的key，未开启全局计数，即计数器要拼接的用户ID，对每一个用户单独限流
     */
    protected abstract void restructureCounterKey(List<String> counterKey);

    /**
     * 对key进行细粒的操作
     *
     * @param key
     * @param timeout
     * @param countMax
     * @return ture 当前key的计数器超出限制，禁止访问
     */
    private boolean counterProcess(String key, long timeout, Integer countMax) {
        boolean success = redisCacheUtil.setCacheObjectIfAbsent(key, 1L, timeout, CounterKeyProperties.HOLDING_TIME_UNIT);
        if (!success) {
            //key已经存在，获取当前计数
            Object cacheObject = redisCacheUtil.getCacheObject(key);
            Integer alreadyCount = (Integer) cacheObject;
            if (ObjectUtils.isNotEmpty(alreadyCount) && alreadyCount >= countMax) {
                //超出限流，限制
                return true;
            } else {
                //未超出，不限制
                redisCacheUtil.increaseValue(key);
                return false;
            }
        }
        //第一次设置key，不限制
        return false;
    }

    /**
     * 对key进行细粒的操作,加锁
     *
     * @param key
     * @param timeout
     * @param countMax
     * @return ture 当前key的计数器超出限制，禁止访问
     */
    private boolean counterProcessWithLock(String key, long timeout, Integer countMax, String lockKey) {
        boolean success = redisCacheUtil.setCacheObjectIfAbsent(key, 1L, timeout, CounterKeyProperties.HOLDING_TIME_UNIT);
        if (!success) {
            //key已经存在，获取当前计数
            //加锁
            while (true) {
                Boolean lock = redisCacheUtil.setCacheObjectIfAbsent(lockKey, 1L, 100L, TimeUnit.MILLISECONDS);
                if (lock) {
                    Object cacheObject = redisCacheUtil.getCacheObject(key);
                    Integer alreadyCount = (Integer) cacheObject;
                    if (ObjectUtils.isNotEmpty(alreadyCount) && alreadyCount >= countMax) {
                        //超出限流，限制
                        redisCacheUtil.deleteObject(lockKey);
                        return true;
                    } else {
                        //未超出，不限制
                        redisCacheUtil.increaseValue(key);
                        redisCacheUtil.deleteObject(lockKey);
                        return false;
                    }
                }
            }
        }
        //第一次设置key，不限制
        return false;
    }


    /**
     * 拒绝策略
     *
     * @return 按需返回
     * @throws Throwable
     */
    @Override
    protected Object rejectHandle(JoinPoint joinPoint) throws Throwable {
        //获取用户是否通过人机验证（滑动验证码等）
        if (isVerificationSucceeded()) {
            //完成验证，清空计数器
            resetLimiter(joinPoint);
            return null;
        }
        //未完成验证
        throw new Exception("接口调用频繁，稍后重试");
    }

    /**
     * 如果用户遭到限流，那么需要进行验证码验证。
     * 验证结果推荐通过Redis获取。
     *
     * @return TRUE：完成验证。FALSE：未完成验证
     */
    private boolean isVerificationSucceeded() {
        return false;
    }

    /**
     * 重置所有的流量限制的计数器
     */
    @Override
    protected final Object resetLimiter(JoinPoint joinPoint) {
        List<String> counterKey = CounterKeyProperties.counterKeys;
        for (String key : counterKey) {
            redisCacheUtil.deleteObject(key);
        }
        return null;
    }
}
