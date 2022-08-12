# 摘要

- `master`与`deploy`分支等价，后者用来部署到`maven`仓库时候使用
- `v1.x`的版本当中，要保留`main/java/**/test`、启动类和`application.yaml`文件，用于对本启动器的测试。合并到master/deploy分支时，需取消合并这些文件，避免多余
- `test`中有`controller`，也是用来测试的接口

# 1.工程简介

> Flow-Limit-Spring-boot-starter，是一个springboot的启动器，提供限流与反爬的解决方案。

- 更新日志：
  - v1.0:实现Redis AOP计数器限流。
  - v1.1:重构启动器结构，使用**模板方法模式**。
  - v1.2:新增Redis拦截器方式，本质是RedisAOP适配，即**适配器模式**。
  - v1.3:AOP与Interceptor可以一起使用，因其执行顺序Interceptor>AOP，因此需要准确的配置切点与拦截路径。
  - v1.4:配置文件，prefix、counterKey允许为null。修复重大Bug。

简单使用，只需引入依赖，简单配置一下就能使用，无侵入，易插拔，易使用。

# 2.快速开始

1. 引入依赖，依赖需在本地仓库或是局域网内服务器仓库

```xml
<dependency>
  <groupId>cn.sinohealth</groupId>
  <artifactId>flow-limit-spring-boot-starter</artifactId>
  <version>1.3.0-SNAPSHOT</version>
</dependency>
```

2. 编写application.yaml配置文件

```yaml
#配置Redis
spring:
  redis:
    host: 192.168.16.87
    port: 6379
# 配置本启动器
flowlimit:
  #是否启用流量限制
  enabled: true
  redis-flow-limit-properties:
    #是否启用全局限制，即所有用户所有操作均被一起计数限制.
    enabled-global-limit: true
    #即计数器的key前缀，可以为空，但不建议
    prefix-key: "icecreamtest::innovative-medicine:desktop-web:redis:flow:limit"
    #每个计数器的Key，注意计数器的key数量与相应配置值要一致，可以为空，但不建议。
    counter-keys:
      - "counter:second:3:"
      - "counter:minutes:2:"
      - "counter:minutes:5:"
      - "counter:hour:1:"
      - ...
    #每个计数器的保持时长，单位是秒
    counter-holding-time:
      - 6
      - 180
      - 300
      - 3600
      - ...
    #每个计数器对应的限流次数，即接口调用次数限制
    counter-limit-number:
      - 5
      - 80
      - 320
      - 240000
      - ... 
```

3.1提供两种实现方式，首先是AOP方式.

新建一个类MyRedisFlowLimitConfig继承AbstractRedisFlowLimitAspect抽象类，实现抽象类的方法

```java
//交由Spring托管
@Configuration
//开启切面
@Aspect
public class MyRedisFlowLimitConfig extends AbstractRedisFlowLimitAspect {
  //选择需要被限制的Controller方法
  @Pointcut("within(cn.sinohealth.flowlimit.springboot.starter.test.TestController)" +
          "&&@annotation(org.springframework.web.bind.annotation.RequestMapping)")
  public void pointcut() {
  }

    //过滤哪些请求，返回TRUE表示对该请求不进行计数限制
    @Override
    protected boolean filterRequest(JoinPoint joinPoint) {
        if (threadLocal.get().getUseID() == 1) {
            //放行超级管理员
            return true;
        }
        return false;
    }

    //当计数器达到上限时执行，返回TRUE则清空计数器放行，否则拒绝策略
    @Override
    protected boolean beforeLimitingHappenWhetherContinueLimit(JoinPoint joinPoint) {
        return false;
    }

    //拒绝策略，可以选择抛出异常，或者返回与Controller类型一样的数据封装
    @Override
    protected Object rejectHandle(JoinPoint joinPoint) throws Throwable {
        throw new Exception("AOP拦截接口");
    }

    //追加用户的ID，enabled-global-limit: true时，会被调用，返回当前登录用户的ID以便限流只是针对当前用户生效。
    @Override
    protected String appendCounterKeyWithUserId(JoinPoint joinPoint) {
        return threadlocal.get().getUserId();
    }
}
```

3.2 第二种方式式拦截器的方式。

新建MyRedisFlowLimitInterceptorConfig.class继承AbstractRedisFlowLimitInterceptor并实现其所有方法。

**父类已经将拦截器注册了，因此不需要手动在WebMvcConfiguration中注册拦截器，仅仅需要配置拦截路径即可**

```java
//交由Spring托管
@Component
public class MyRedisFlowLimitInterceptorConfig extends AbstractRedisFlowLimitInterceptor {
    //设置拦截器的拦截路径
    @Override
    public void setInterceptorPathPatterns(InterceptorRegistration registry) {
        registry.addPathPatterns("/api/**");
    }

    //过滤哪些请求，返回TRUE表示对该请求不进行计数限制
    @Override
    public boolean filterRequest(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return false;
    }

    //追加用户的ID，enabled-global-limit: true时，会被调用，返回当前登录用户的ID以便限流只是针对当前用户生效。
    @Override
    public String appendCounterKeyWithUserId(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return null;
    }

    //当计数器达到上限时执行，返回TRUE则清空计数器放行，否则拒绝策略
    @Override
    public boolean beforeLimitingHappenWhetherContinueLimit(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return false;
    }

    //拒绝策略，可以选择抛出异常，或者返回与Controller类型一样的数据封装
    @Override
    public void rejectHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setCharacterEncoding("utf-8");
        response.getWriter().write("接口调用频繁");
        response.setStatus(404);
    }

}
```

# 3. 实现原理

> 待补充
