package cn.sinohealth.flowlimit.springboot.starter.service;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import lombok.Data;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 14:32
 * @Description: TODO
 */
@Data
public class RedisFlowLimitService {

    private FlowLimitProperties.RedisFlowLimitAspectProperties redisLimitFlowAspectProperties;


    public RedisFlowLimitService(FlowLimitProperties.RedisFlowLimitAspectProperties redisLimitFlowAspectProperties) {
        this.redisLimitFlowAspectProperties = redisLimitFlowAspectProperties;
        int size1 = redisLimitFlowAspectProperties.getCounterLimitNumber().size();
        int size2 = redisLimitFlowAspectProperties.getCounterHoldingTime().size();
        int size3 = redisLimitFlowAspectProperties.getCounterKeys().size();
        if (size1 == 0) {
            throw new IllegalArgumentException("redis计数器的key数量最少为1");
        }
        if (!(size1 == size2 && size1 == size3)) {
            throw new IllegalArgumentException("redis计数器的key数量与相应配置值数量不一致！");
        }
    }

}
