package cn.sinohealth.flowlimit.springboot.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class FlowLimitSpringBootStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowLimitSpringBootStarterApplication.class, args);
    }

}