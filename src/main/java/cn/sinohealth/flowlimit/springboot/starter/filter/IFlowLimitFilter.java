package cn.sinohealth.flowlimit.springboot.starter.filter;

import cn.sinohealth.flowlimit.springboot.starter.IFlowLimit;

import javax.servlet.Filter;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/19 14:06
 * @Description: TODO
 */
public interface IFlowLimitFilter extends Filter, IFlowLimit {


}
