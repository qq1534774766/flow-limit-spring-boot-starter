package cn.sinohealth.flowlimit.springboot.starter.service;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import lombok.Data;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 14:14
 * @Description: TODO
 */
public class FlowLimitService {
    private FlowLimitProperties flowLimitProperties;


    public FlowLimitService() {
    }

    public FlowLimitService(FlowLimitProperties flowLimitService) {
        this.flowLimitProperties = flowLimitService;

    }

    public FlowLimitProperties getFlowLimitProperties() {
        return flowLimitProperties;
    }

    public void setFlowLimitProperties(FlowLimitProperties flowLimitProperties) {
        this.flowLimitProperties = flowLimitProperties;
    }
}
