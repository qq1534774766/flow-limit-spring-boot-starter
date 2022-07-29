package cn.sinohealth.flowlimit.springboot.starter.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 18:01
 * @Description: TODO
 */
@RestController
public class TestController {
    /**
     * adsad
     *
     * @return
     */
    @RequestMapping("/")
    public Result index() {
        return new Result(200, null);
    }
}

