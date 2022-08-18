package cn.sinohealth.flowlimit.springboot.starter.utils;

import lombok.Data;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/8/18 11:42
 * @Description: TODO
 */
public enum CacheDataSourceTypeEnum {
    Redis(1, "Redis 数据源"),
    Local(2, "本地  数据源");

    private final Integer code;
    private final String describe;

    CacheDataSourceTypeEnum(Integer code, String describe) {
        this.code = code;
        this.describe = describe;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescribe() {
        return describe;
    }

}
