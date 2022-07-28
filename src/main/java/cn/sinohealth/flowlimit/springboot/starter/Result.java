package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.aspect.IFlowLimit;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.aspectj.lang.JoinPoint;

@Data
@AllArgsConstructor
public class Result {
    private Integer code;
    private Object data;


}