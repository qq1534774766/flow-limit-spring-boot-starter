package cn.sinohealth.flowlimit.springboot.starter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: wenqiaogang
 * @DateTime: 2022/7/26 18:01
 * @Description: TODO
 */
@RestController
public class Cont {
    /**
     * adsad
     *
     * @return
     */
    @RequestMapping("/")
    public String index() {
        return "123321312";
    }
}
