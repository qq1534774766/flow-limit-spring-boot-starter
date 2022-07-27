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

    private RedisFlowLimitAspectProperties redisFlowLimitAspectProperties;

    @Data
    public static class RedisFlowLimitAspectProperties {
        /**
         * 是否开启同步计数，如不要求精准限流，则无需开启。
         * <br/>
         * 开启后，将使用严格计数，开销一定性能。
         */
        private boolean enabledSyncCount = false;

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
         * 每个计数器的保持时长，单位是毫秒
         */
        private List<Long> counterHoldingTime;

        /**
         * 每个计数器对应的限流次数，即接口调用次数限制
         */
        private List<Integer> counterLimitNumber;


    }

}
