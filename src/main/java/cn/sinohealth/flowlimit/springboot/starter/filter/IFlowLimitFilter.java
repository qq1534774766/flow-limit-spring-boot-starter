package cn.sinohealth.flowlimit.springboot.starter.filter;

import cn.sinohealth.flowlimit.springboot.starter.IFlowLimit;

import javax.servlet.Filter;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/19 14:06
 * @Description: 过滤器顶级接口
 */
public interface IFlowLimitFilter extends Filter, IFlowLimit {


}
