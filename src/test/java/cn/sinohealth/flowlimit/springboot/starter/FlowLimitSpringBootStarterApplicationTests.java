package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.service.aspect.impl.RedisLimitFlowAspect;
import org.junit.jupiter.api.Test;
//import cn.sinohealth.flowlimit.springboot.starter.service.aspect.impl.RedisLimitFlowAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlowLimitSpringBootStarterApplicationTests {

    @Autowired
    private RedisLimitFlowAspect redisLimitFlowAspect;

    @Test
    void contextLoads() {
        System.out.println(redisLimitFlowAspect);
    }

}
