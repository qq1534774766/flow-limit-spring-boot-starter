package cn.sinohealth.flowlimit.springboot.starter.test;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class Result {
    private Integer code;
    private Object data;


}