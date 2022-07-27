package cn.sinohealth.flowlimit.springboot.starter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    private Integer code;
    private Object data;
}