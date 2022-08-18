package cn.sinohealth.flowlimit.springboot.starter.properties;

import cn.sinohealth.flowlimit.springboot.starter.utils.CacheDataSourceTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 10:59
 * @Description: 统一配置类
 */
@ConfigurationProperties(prefix = "flowlimit")
@Data
public class FlowLimitProperties {
    /**
     * 是否启用流量限制
     */
    private boolean enabled = false;


    /**
     * Redis流量限制配置属性
     */
    private CounterFlowLimitProperties redisFlowLimitProperties;


    public static class CounterFlowLimitProperties {

        /**
         * 是否启用全局限制，即所有用户所有操作均被一起计数限制.
         * <br/>
         * 不启用：则需要实现appendCounterKeyWithUserId()方法，并返回当前登录用户的ID。
         */
        private boolean enabledGlobalLimit = true;
        /**
         * 计数器模式下的数据源，默认是Redis数据源
         */
        private CacheDataSourceTypeEnum dataSourceType = CacheDataSourceTypeEnum.Redis;

        /**
         * 即计数器的key前缀，可以为空，但不建议
         * <br/>形式：
         * "icecream::innovative-medicine:desktop-web:xxx"
         */
        private String prefixKey;

        /**
         * 每个计数器的Key，注意计数器的key数量与相应配置值要一致，可以为空，但不建议。
         * 为空时候，key是UUID
         */
        private List<String> counterKeys;

        /**
         * 每个计数器的保持时长
         */
        private List<Long> counterHoldingTime;

        /**
         * 计数器的时间单位，默认是秒
         */
        private TimeUnit counterHoldingTimeUnit = TimeUnit.SECONDS;

        /**
         * 每个计数器对应的限流次数，即接口调用次数限制
         */
        private List<Integer> counterLimitNumber;

        public boolean isEnabledGlobalLimit() {
            return enabledGlobalLimit;
        }

        public void setEnabledGlobalLimit(boolean enabledGlobalLimit) {
            this.enabledGlobalLimit = enabledGlobalLimit;
        }

        public String getPrefixKey() {
            return prefixKey;
        }

        public void setPrefixKey(String prefixKey) {
            this.prefixKey = prefixKey;
        }

        public List<String> getCounterKeys() {
            return counterKeys;
        }

        public void setCounterKeys(List<String> counterKeys) {
            this.counterKeys = counterKeys;
        }

        public List<Long> getCounterHoldingTime() {
            return counterHoldingTime;
        }

        public void setCounterHoldingTime(List<Long> counterHoldingTime) {
            this.counterHoldingTime = counterHoldingTime;
        }

        public List<Integer> getCounterLimitNumber() {
            return counterLimitNumber;
        }

        public void setCounterLimitNumber(List<Integer> counterLimitNumber) {
            this.counterLimitNumber = counterLimitNumber;
        }

        public TimeUnit getCounterHoldingTimeUnit() {
            return counterHoldingTimeUnit;
        }

        public void setCounterHoldingTimeUnit(TimeUnit counterHoldingTimeUnit) {
            this.counterHoldingTimeUnit = counterHoldingTimeUnit;
        }

        public CacheDataSourceTypeEnum getDataSourceType() {
            return dataSourceType;
        }

        public void setDataSourceType(CacheDataSourceTypeEnum dataSourceType) {
            this.dataSourceType = dataSourceType;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CounterFlowLimitProperties getRedisFlowLimitProperties() {
        return redisFlowLimitProperties;
    }

    public void setRedisFlowLimitProperties(CounterFlowLimitProperties redisFlowLimitProperties) {
        this.redisFlowLimitProperties = redisFlowLimitProperties;
    }
}
