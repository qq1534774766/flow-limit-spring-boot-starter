package cn.sinohealth.flowlimit.springboot.starter.service;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import lombok.Data;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 14:32
 * @Description: TODO
 */
public class RedisFlowLimitService {

    private FlowLimitProperties.RedisLimitFlowAspectProperties redisLimitFlowAspectProperties;


    public RedisFlowLimitService(FlowLimitService flowLimitService) {
        redisLimitFlowAspectProperties = flowLimitService.getFlowLimitProperties().getRedisLimitFlowAspect();
        int size1 = flowLimitService.getFlowLimitProperties().getRedisLimitFlowAspect().getCounterLimitNumber().size();
        int size2 = flowLimitService.getFlowLimitProperties().getRedisLimitFlowAspect().getCounterHoldingTime().size();
        int size3 = flowLimitService.getFlowLimitProperties().getRedisLimitFlowAspect().getCounterKeys().size();
        if (!(size1 == size2 && size1 == size3)) {
            throw new IllegalArgumentException("redis的key数量与配置值数量不一致！");
        }
    }

    public FlowLimitProperties.RedisLimitFlowAspectProperties getRedisLimitFlowAspectProperties() {
        return redisLimitFlowAspectProperties;
    }

    public void setRedisLimitFlowAspectProperties(FlowLimitProperties.RedisLimitFlowAspectProperties redisLimitFlowAspectProperties) {
        this.redisLimitFlowAspectProperties = redisLimitFlowAspectProperties;
    }
}
