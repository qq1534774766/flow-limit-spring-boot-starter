package cn.sinohealth.flowlimit.springboot.starter;

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
    public String index() {
        return "{'code':200,'data':null}";
    }
}
