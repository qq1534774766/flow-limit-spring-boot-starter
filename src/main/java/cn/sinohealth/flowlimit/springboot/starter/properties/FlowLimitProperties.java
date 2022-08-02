package cn.sinohealth.flowlimit.springboot.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
     * Redis流量限制配置属性
     */
    private RedisFlowLimitProperties redisFlowLimitProperties;

    @Data
    public static class RedisFlowLimitProperties {

        /**
         * 是否启用全局限制，即所有用户所有操作均被一起计数限制.
         * <br/>
         * 不启用：则需要实现appendCounterKeyWithUserId()方法，并返回当前登录用户的ID。
         */
        private boolean enabledGlobalLimit = true;

        /**
         * 即计数器的key前缀
         * <br/>形式：
         * "icecream::innovative-medicine:desktop-web:xxx"
         */
        private String prefixKey;

        /**
         * 每个计数器的Key，注意计数器的key数量与相应配置值要一致
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
