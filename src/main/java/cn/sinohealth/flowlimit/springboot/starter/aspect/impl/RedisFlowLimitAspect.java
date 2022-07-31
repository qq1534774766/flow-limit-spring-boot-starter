package cn.sinohealth.flowlimit.springboot.starter.aspect.impl;

import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimitAspect;
import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import cn.sinohealth.flowlimit.springboot.starter.aspect.AbstractFlowLimitAspect;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/25 10:33
 * @Description: Redis数据源，计数器的方式限流。排除未登录用户
 */
@Slf4j
public abstract class RedisFlowLimitAspect extends AbstractFlowLimitAspect
        implements IFlowLimitAspect {

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 是否全局限制，即所有用户所有操作均被计数限制.
     * <br/>
     * FALSE则需要传递获取用户ID的方法。
     */
    private boolean enabledGlobalLimit;

    /**
     * baseKey，即全局key前缀
     * <br/>形式：
     * 注意：这里的每个key已经加到counterKey中
     */
    private String prefixKey;

    /**
     * 每个计数器的Key。
     * <pre>
     * 注意：这里的每个key已经有全局的前缀prefixKey
     *  <pre/>
     */
    private List<String> counterKeys;

    /**
     * 每个计数器的保持时长，单位是毫秒
     */
    private List<Long> counterHoldingTime;

    /**
     * 每个计数器对应的限流次数，即接口调用次数限制
     */
    private List<Integer> counterLimitNumber;


    /**
     * 【当用户超出限流时，需要判断key的存在性】
     * <br/>
     * key存在，表示用户通过了验证码的验证，重置限流计数器。
     * <br/>
     * key不存在，用户尚未完成验证，继续限流。
     */
    public static final String OVERSTEP_FLOW_VERIFICATION = "overstep:flow:verification";

    public RedisFlowLimitAspect() {

    }

    @Autowired(required = false)
    public RedisFlowLimitAspect setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        return this;
    }

    @Autowired(required = false)
    public RedisFlowLimitAspect setCounterKeyProperties(FlowLimitProperties.RedisFlowLimitProperties redisFlowLimitProperties) {
        //封装公共属性
        this.enabledGlobalLimit = redisFlowLimitProperties.isEnabledGlobalLimit();
        //封装properties
        RedisFlowLimitAspect.this.prefixKey = redisFlowLimitProperties.getPrefixKey();
        String appendKey = appendCounterKeyWithMode();
        counterKeys = redisFlowLimitProperties.getCounterKeys().stream()
                .map(key -> prefixKey + key + appendKey).collect(Collectors.toList());
        counterHoldingTime = redisFlowLimitProperties.getCounterHoldingTime();
        counterLimitNumber = redisFlowLimitProperties.getCounterLimitNumber();
        return this;
    }

    /**
     * 追加模式，有AOP模式和拦截器模式。前面要有个分号
     *
     * @return
     */
    public String appendCounterKeyWithMode() {
        return "aspect:";
    }

    @PostConstruct
    public RedisFlowLimitAspect initBeanProperties() {
        setEnabled(redisTemplate != null && !StringUtils.isEmpty(prefixKey));
        if (isEnabled()) {
            log.info("Redis流量限制器启动成功！");
        }
        return this;
    }


    @Override
    public final boolean limitProcess(JoinPoint joinPoint) {
        List<String> counterKey = counterKeys;
        if (!enabledGlobalLimit) {
            //未开启全局计数，即计数器要拼接的用户ID，对每一个用户单独限流
            counterKey = counterKey.stream()
                    .map(key ->
                            key.concat(Optional
                                    .ofNullable(appendCounterKeyWithUserId(joinPoint))
                                    .orElse("")))
                    .collect(Collectors.toList());
        }
        //当前计数器是否限制？
        boolean currentIsLimit = false;
        //遍历计数器
        for (int i = 0; i < counterKey.size() && !currentIsLimit; i++) {
            currentIsLimit = counterProcess(counterKey.get(i), counterHoldingTime.get(i), counterLimitNumber.get(i));
        }
        //当且仅当所有计数器都返回false才不限制
        return currentIsLimit;
    }


    /**
     * 重构计数器的key，未开启全局计数，即计数器要拼接的用户ID，对每一个用户单独限流
     *
     * @param joinPoint
     * @return 重构逻辑
     */
    protected abstract String appendCounterKeyWithUserId(JoinPoint joinPoint);

    private static final String LUA_INC_SCRIPT_TEXT =
            " local setSuccess = redis.call('set',KEYS[1],1,'ex',ARGV[1],'nx');" +
                    " if(type(setSuccess)=='table') then" +
                    " return 1;" +
                    " else" +
                    " redis.call('incr',KEYS[1]);" +
                    " local keyTtl = redis.call('ttl',KEYS[1]);" +
                    " if(keyTtl==-1) then" +
                    " redis.call('set',KEYS[1],1,'ex',ARGV[1],'xx');" +
                    " return 2;" +
                    " end" +
                    " end" +
                    " return 3;";
    private static final DefaultRedisScript<Long> REDIS_INC_SCRIPT = new DefaultRedisScript<>(LUA_INC_SCRIPT_TEXT, Long.class);

    /**
     * 对key进行细粒的操作,即计数器自增
     * 会用LUA脚本实现,如果Redis宕机，那么会拦截所有请求。
     *
     * @param key
     * @param timeout
     * @param countMax
     * @return ture 当前key的计数器超出限制，禁止访问
     */
    private boolean counterProcess(String key, long timeout, Integer countMax) {
        Long result = redisTemplate.execute(REDIS_INC_SCRIPT, Collections.singletonList(key), timeout);
        //设置key成功: 1
        // 原来的key自增失败，重设新的key: 2
        // key自增成功: 3
        if (Optional.ofNullable(result).orElse(-1L) < 3) {
            return false;
        }
        return Optional.ofNullable((Integer) redisTemplate.opsForValue().get(key))
                .map(alreadyCount -> alreadyCount > countMax)
                .orElse(false);
    }


    /**
     * 重置所有的流量限制的计数器
     */
    @Override
    protected final Object resetLimiter(JoinPoint joinPoint) {
        List<String> counterKey = counterKeys;
        for (String key : counterKey) {
            redisTemplate.delete(key);
        }
        return null;
    }


}
