package cn.sinohealth.flowlimit.springboot.starter;

import cn.sinohealth.flowlimit.springboot.starter.properties.FlowLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlowLimitSpringBootStarterApplicationTests {

    @Autowired
    private FlowLimitProperties flowLimitProperties;

    @Test
    void contextLoads() {
        System.out.println(flowLimitProperties.getRedisLimitFlowAspect().isSyncCount());
    }

}
