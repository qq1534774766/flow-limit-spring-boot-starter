package cn.sinohealth.flowlimit.springboot.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 10:59
 * @Description: 统一配置类
 */
@ConfigurationProperties(prefix = "flowlimit")
@Component
@Data
public class FlowLimitProperties {
    /**
     * 是否启用流量限制
     */
    private boolean enabled = false;
    /**
     * 限流策略类型
     */
    private Class<?> flowLimitStrategyImplClassName;


    private RedisFlowLimitAspectProperties redisFlowLimitAspectProperties;

    @Data
    public static class RedisFlowLimitAspectProperties {

        /**
         * 是否全局限制，即所有用户所有操作均被计数限制.
         * <br/>
         * FALSE则需要传递获取用户ID的方法。
         */
        private boolean enabledGlobalLimit = true;

        /**
         * baseKey，即全局key前缀
         * <br/>形式：
         * xxx:xxx:xxx:
         */
        private String prefixKey;

        /**
         * 每个计数器的Key
         */
        private List<String> counterKeys;

        /**
         * 每个计数器的保持时长，单位是秒
         */
        private List<Long> counterHoldingTime;

        /**
         * 每个计数器对应的限流次数，即接口调用次数限制
         */
        private List<Integer> counterLimitNumber;


    }

}
