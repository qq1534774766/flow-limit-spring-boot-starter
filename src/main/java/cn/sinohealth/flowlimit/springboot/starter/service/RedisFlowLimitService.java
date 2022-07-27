package cn.sinohealth.flowlimit.springboot.starter.service;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 14:32
 * @Description: TODO
 */
public class RedisFlowLimitService {

    private FlowLimitProperties.RedisFlowLimitAspectProperties redisLimitFlowAspectProperties;


    public RedisFlowLimitService(FlowLimitService flowLimitService) {
        redisLimitFlowAspectProperties = flowLimitService.getFlowLimitProperties().getRedisFlowLimitAspectProperties();
        int size1 = flowLimitService.getFlowLimitProperties().getRedisFlowLimitAspectProperties().getCounterLimitNumber().size();
        int size2 = flowLimitService.getFlowLimitProperties().getRedisFlowLimitAspectProperties().getCounterHoldingTime().size();
        int size3 = flowLimitService.getFlowLimitProperties().getRedisFlowLimitAspectProperties().getCounterKeys().size();
        if (size1 == 0) {
            throw new IllegalArgumentException("redis计数器的key数量最少为1");
        }
        if (!(size1 == size2 && size1 == size3)) {
            throw new IllegalArgumentException("redis计数器的key数量与相应配置值数量不一致！");
        }
    }

    public FlowLimitProperties.RedisFlowLimitAspectProperties getRedisLimitFlowAspectProperties() {
        return redisLimitFlowAspectProperties;
    }

    public void setRedisLimitFlowAspectProperties(FlowLimitProperties.RedisFlowLimitAspectProperties redisLimitFlowAspectProperties) {
        this.redisLimitFlowAspectProperties = redisLimitFlowAspectProperties;
    }
}
