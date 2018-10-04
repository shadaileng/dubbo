# <center>Dubbo</center>

[toc]

# 一. 分布式系统

分布式系统是若干个独立计算机的集合，这些计算机对于用户来说是单个的相关系统。分布式系统是建立在网络之上的软件系统。

分布式系统中的各个服务远程调用称为远程过程调用(`RPC`),解决远程过程调用的框架就是分布式服务框架.`Dubbo`就是一个分布式服务框架.

在分布式服务框架的基础上引入服务中心管理维护服务之间的关系和实时管理服务集群容量,提高集群利用率.这就是流动计算框架.

## 1 RPC原理

1. `Client`调用服务,
2. `Client Stub`将调用服务信息以及参数序列化,并发送到服务器(`Server Stub`)
3. `Server Stub`反序列化服务信息以及参数对象,并调用服务器本地服务
4. `Server`执行服务,并将结果返回`Server Stub`
5. `Server Stub`将结果序列化之后返回客户端(`Client STub`)
6. `Client Stub`将结果反序列化,并返回到`Client`

> `RPC`框架性能:

- 服务之间建立连接速度
- 服务之间传递数据序列化和反序列化机制的速度

# 二. Dubbo简介

`Dubbo`是一款高性能`Java RPC` 框架:

- 面向接口的高性能`RPC`调用: 提供基于代理的远程调用能力,一接口为粒度,屏蔽远程调用底层细节
- 只能负载均衡: 内置多种负载均衡策略,感知下游节点健康状况,以减少调用延迟,提供系统吞吐量
- 服务自动注册与发现: 支持多种注册服务中心,服务实例上下线感知
- 高度扩展能力: 遵循微内核+插件的设计原则,所有核心能力(如: `Protocol`, `Transport`,`Serialization`)被设计为扩展点
- 运行期流量调度: 通过配置路由规则,实现灰度发布、同机房优先等功能
- 可视化服务治理与运维: 

`Dubbo`的设计架构

1. `Container`启动,并将`Provider`的服务注册到注册中心(`Registry`)
2. `Consumer`在注册中心(`Registry`)中订阅服务,如果订阅的服务发生变化,注册中心(`Registry`)推送变更到`Consumer`
3. `Consumer`根据同步调用服务
4. 监控中心实时监控服务状态

# 三. 使用Dubbo

## 1 注册中心

> `Dubbo`官方推荐使用[`Zookeeper`注册中心](http://dubbo.apache.org/zh-cn/docs/user/references/registry/zookeeper.html)

1. [下载`Zookeeper`压缩包](https://archive.apache.org/dist/zookeeper/),并解压
2. 配置`Zookeeper`
```
# 复制Zookeeper配置文件模板,并重命名为zoo.cfg
$ cp ./conf/zoo_sample.cfg ./conf/zoo.cfg
# 创建存放zookeeper数据的文件夹data
$ mkdir ./data
# 修改配置文件
$ vim ./conf/zoo.cfg 
---------------------------
dataDir=../data
clientPort=2181
----------------------
```
3. 启动`Zookeeper`

```
$ ./bin/zkServer.sh
```

4. 测试客户端连接

```
$ ./bin/zkCli.sh
# 获取根节点值
[zk: host:2181] get /
# 查看根节点下的节点
[zk: host:2181] ls /
# 创建节点,并赋值
[zk: host:2181] create -e /qpf 123456
```

## 2 管理中心

> 下载[`Dubbo-Ops`](https://github.com/apache/incubator-dubbo-ops),并解压,其中包含`dubbo-admin`、`dubbo-monitor-simple`

### 2.1 管理控制台(dubbo-admin)

> `Dubbo-2.5.x`及以下的版本,会打包成`war`包的形式,需要放在`Web`容器中运行.`2.6.x`之后的版本使用`spring-boot`构建,可打包成`jar`包,`java -jar`直接运行

1. 修改`dubbo-admin`配置文件`src/main/resource/application.properties`

```
# 配置注册中心地址
dubbo.registry.address=zookeeper://127.0.0.1:2181
```

2. 使用`Maven`打包`dubbo-admin`

```
$ cd ./dubbo-admin
$ mvn clear package
$ java -jar ./target/dubbo-admin-0.0.1-SNAPSHOT.jar
```
3. 访问管理控制台

```
http://127.0.0.1:7001
u: root
p: root
```

### 2.2 监控中心(dubbo-monitor-simple)

1. 使用`Manven`打包`dubbo-monitor-simple`

```
$ cd ./dubbo-monitor-simple
$ mvn package
$ tar -xvf ./target/dubbo-monitor-simple-x.x.x
```
2. 配置`./target/dubbo-monitor-simple-x.x.x/confdubbo.properties`

```
# 注册中心地址
dubbo.registy.address=zookeeper://127.0.0.1:2181
# 服务与监控中心的通信端口
dubbo.protocol.port=7070
# 监控中心web页面端口
dubbo.jetty.port=8080
```
3. 启动监控中心

```
$ ./target/dubbo-monitor-simple-x.x.x/assembly.bin/start.sh
```

4. 访问监控中心

```
127.0.0.1:8080
```

## 3 案例

### 3.1 需求场景

模块 | 功能
---|---
订单服务`Web`模块 | 创建订单
用户服务`service`模块 | 查询用户信息

订单服务(`Consumer`)创建订单时,远程调用用户服务(`Provider`)查询用户信息

### 3.2 公共接口

1. 创建`Maven`工程 ==> `Group Id:com.qpf.mall` ==> `Artifact Id: mall-interfaces` ==> `Packaging: jar` ==> `Finish`

2. 创建用户地址对象(`com.qpf.mall.bean.UserAddress`)

```
public UserAddress implements Serializable {
    private Integer id;
    /** 用户地址 */
    private String userAddress; 
    /** 用户Id */
    private String userId;
    /** 收货人 */
    private String consignee;
    /** 电话 */
    private String phoneNum;
    /** 是否默认 */
    private boolean isDefault;
    // constractor getter setter toString
}
```
3. 创建用户服务接口(`com.qpf.mall.service.UserService``)
```
public interface UserService {
    public List<UserAddress> getUserAdderssList(String userId);
}
```

4. 创建订单服务接口(`com.qpf.mall.service.OrderService`)
```
public interface OrderService {
    public String initOrder(String userId);
}
```

### 3.3 Maven方式创建

#### 3.3.1 Provider

1. 创建`Maven`工程 ==> `Group Id:com.qpf.mall` ==> `Artifact Id: user-service-provider` ==> `Packaging: jar` ==> `Finish`

2. `pom.xml`导入公共接口依赖

```
<dependencys>
    <dependency>
        <groupId>com.qpf.mall</groupId>
        <artifactId>mall-interfaces</artifactId>
    </dependency>
</dependencys>
```

3. 创建用户服务(`com.qpf.mall.service.impl.UserServiceImpl`)实现用户服务接口

```
public class UserServiceImpl implements UserService {
    public List<UserAddress> getUserAdderssList(String userId) {
        UserAddress address1 = new UserAddress(1, "地球1610", "u0001", "qpf", "13800000000", false);
        UserAddress address2 = new UserAddress(2, "地球616", "u0001", "qpf", "13800000000", true);
        
        return Arrays.asList(address1, address2);
    }
}
```

4. 注册`Provider`到`Registry`(暴露服务)
    - 导入`Dubbo`依赖(`dubbo-2.6.2`, `curator-2.12.0`)
        ```
        <!-- https://mvnrepository.com/artifact/com.alibaba/dubbo -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dubbo</artifactId>
            <version>2.6.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>2.12.0</version>
        </dependency>
        ```
        
        > `dubbo-2.6.x`之后的版本使用`curator`作为`Zookeeper`的客户端,`dubbo-2.5.x`及以下版本使用`zkclient`作为客户端
    
    - 配置暴露服务(`spring`配置文件)
        ```
        <?xml version="1.0" encoding="UTF-8"?>
            <beans xmlns="http://www.springframework.org/schema/beans"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
                xsi:schemaLocation="http://www.springframework.org/schema/beans        http://www.springframework.org/schema/beans/spring-beans-4.3.xsd        http://dubbo.apache.org/schema/dubbo        http://dubbo.apache.org/schema/dubbo/dubbo.xsd">
             
                <!-- 提供方应用信息，用于计算依赖关系 -->
                <dubbo:application name="user-service-provider"  />
             
                <!-- 使用zookeeper注册中心暴露服务地址 -->
                <!--<dubbo:registry protocol="zookeeper"  address="127.0.0.1:2181" />-->
                <dubbo:registry address="zookeeper://127.0.0.1:2181" />
             
                <!-- 用dubbo协议在20880端口暴露服务,与Comsumer的通信规则 -->
                <dubbo:protocol name="dubbo" port="20880" />
             
                <!-- 声明需要暴露的服务接口 -->
                <dubbo:service interface="com.qpf.mall.service.UserService" ref="userService" />
             
                <!-- 和本地bean一样实现服务 -->
                <bean id="userService" class="com.qpf.mall.service.impl.UserServiceImpl" />
                
                <!-- 连接监控中心 -->
                <!-- 自动发现连接监控中心 -->
                <dubbo:monitor protocol="registry"></dubbo:monitor>
                <!-- 直连监控中心 -->
                <!--<dubbo:monitor address="127.0.0.1:7070"></dubbo:monitor>-->
            </beans>
        ```
5. 创建`MainApplication`程序启动`Provider`

```
public MainApplication {
    public staticvoid main(String[] args) throws Exceptioin {
        ClassPathXmlApplcationContext ioc = new ClassPathXmlApplcationContext("provider.xml");
        ioc.start()
        System.in.read();
    }
}
```


#### 3.3.2 Consumer

1. 创建`Maven`工程 ==> `Group Id:com.qpf.mall` ==> `Artifact Id: order-service-consumer` ==> `Packaging: jar` ==> `Finish`

2. `pom.xml`导入公共接口依赖

```
<dependencys>
    <dependency>
        <groupId>com.qpf.mall</groupId>
        <artifactId>mall-interfaces</artifactId>
    </dependency>
</dependencys>
```

3. 创建订单服务(`com.qpf.mall.service.impl.OrderServiceImpl`)实现订单服务接口

```
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private UserService userService;
    public String initOrder(String userId) {
        System.out.println("创建订单: =====================");
        System.out.println("用户地址: =======");
        List<UserAddress> userAddresses =  userService.getUserAdderssList(userId);
        for (UserAddress userAddress: userAddresses) {
            System.out.println("====: " + userAddress);
        }
        System.out.println("用户地址: =======");
        return "用户地址: " + userAddresses;
    }
}
```

4. `Consumer`从`Registry`订阅服务
    - 导入`Dubbo`依赖(`dubbo-2.6.2`, `curator-2.12.0`)
        ```
        <!-- https://mvnrepository.com/artifact/com.alibaba/dubbo -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dubbo</artifactId>
            <version>2.6.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>2.12.0</version>
        </dependency>
        ```
        
        > `dubbo-2.6.x`之后的版本使用`curator`作为`Zookeeper`的客户端,`dubbo-2.5.x`及以下版本使用`zkclient`作为客户端
        
    - 配置`Consumer`订阅服务(`consumer.xml`)
        ```
        <?xml version="1.0" encoding="UTF-8"?>
        <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
            xsi:schemaLocation="http://www.springframework.org/schema/beans
            xmlns:context="http://www.springframework.org/schema/context        http://www.springframework.org/schema/beans/spring-beans-4.3.xsd        http://dubbo.apache.org/schema/dubbo        http://dubbo.apache.org/schema/dubbo/dubbo.xsd">
            <context:component-scan base-package="com.qpf.mall.service.impl"    ></context:component-scan>
            <!-- 消费方应用名，用于计算依赖关系，不是匹配条件，不要与提供方一样 -->
            <dubbo:application name="order-service-consumer"  />
         
            <!-- 使用zookeeper广播注册中心暴露发现服务地址 -->
            <dubbo:registry address="zookeeper://127.0.0.1:2181" />
         
            <!-- 生成远程服务代理，可以和本地bean一样使用userService -->
            <dubbo:reference id="userService" interface="com.qpf.mall.service.UserService" />
            
            <!-- 连接监控中心 -->
            <!-- 自动发现连接监控中心 -->
            <dubbo:monitor protocol="registry"></dubbo:monitor>
            <!-- 直连监控中心 -->
            <!--<dubbo:monitor address="127.0.0.1:7070"></dubbo:monitor>-->
            
            
        </beans>
        ```
5. 调用`OrderServoce`


```
public MainApplication {
    public staticvoid main(String[] args) throws Exceptioin {
        ClassPathXmlApplcationContext ioc = new ClassPathXmlApplcationContext("consumer.xml");
        OrderService service = ioc.getBean(OrderService.class);
        System.out.println(service.initOrder("u0001"));
        System.in.read();
    }
}
```

### 3.4 Spring Boot方式创建

#### 3.4.1 Provider

1. 创建`Spring Boot`工程 ==> `Group Id: com.qpf.mall` ==> `Artifact Id: boot-user-service-provider` ==> `Packaging: jar` ==> `Package: com.qpf.mall` ==> `Version: 2.0.4`


2. `pom.xml`导入公共接口依赖,以及[`dubbo-starter`](https://github.com/apache/incubator-dubbo-spring-boot-project)

```
<dependencys>
    <dependency>
        <groupId>com.qpf.mall</groupId>
        <artifactId>mall-interfaces</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.boot</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
        <version>0.2.0</version>
    </dependency>
</dependencys>
```

> `dubbo-starter`的依赖环境

versions|Java|Spring Boot|Dubbo
---|---|---|---
0.2.0|1.8+|2.0.x|2.6.2 +
0.1.1|1.7+|1.5.x|2.6.2 +

3. 创建用户服务(`com.qpf.mall.service.impl.UserServiceImpl`)实现用户服务接口
    - `@Component`注解将用户服务实现添加到`Spring`容器
    - `Dubbo`的`@Service`注解注册服务到`registry`
        ```
        @Component
        @Service
        public class UserServiceImpl implements UserService {
            public List<UserAddress> getUserAdderssList(String userId) {
                UserAddress address1 = new UserAddress(1, "地球1610", "u0001", "qpf", "13800000000", false);
                UserAddress address2 = new UserAddress(2, "地球616", "u0001", "qpf", "13800000000", true);
                
                return Arrays.asList(address1, address2);
            }
        }
        ```
    - 主程序类添加`@EnableDubbo`注解开启基于注解的`Dubbo`功能
        ```
        @EnableDubbo
        @SpringBootApplication
        public class ServiceProviderTicketApplication {
        
            public static void main(String[] args) {
                SpringApplication.run(ServiceProviderTicketApplication.class, args);
            }
        }    
        ```

4. 注册`Provider`服务到`Registry`
    - 配置`Provider`注册服务
        ```
        # 应用名
        dubbo.application.name=boot-user-service-provider
        # 注册中心
        dubbo.registry.address=zookeeper://127.0.0.1:2181
        # 使用dubbo协议通信服务
        dubbo.protocol.name=dubbo
        # dubbo服务通信端口
        dubbo.protocol.port=20880
        # 监控中心自动发现应用
        dubbo.monitor.protocol=registry
        ```

#### 3.4.2 Consumer

1. 创建`Spring Boot`工程 ==> `Group Id: com.qpf.mall` ==> `Artifact Id: boot-order-service-consumer` ==> `Packaging: jar` ==> `Package: com.qpf.mall` ==> `Version: 2.0.4` ==> `Web`


2. `pom.xml`导入公共接口依赖,以及[`dubbo-starter`](https://github.com/apache/incubator-dubbo-spring-boot-project)
```
<dependencys>
    <dependency>
        <groupId>com.qpf.mall</groupId>
        <artifactId>mall-interfaces</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.boot</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
        <version>0.2.0</version>
    </dependency>
</dependencys>
```

3. 创建用户服务(`com.qpf.mall.service.impl.OrderServiceImpl`)实现用户服务接口
    - `@Service`注解将接口实现添加到`Spring`容器中
    - `@Reference`注解从`Resistry`自动发现服务
        ```
        @Service
        public class ServiceImpl implements OrderService {
            @Reference
            private UserService userService;
            public String initOrder(String userId) {
                System.out.println("创建订单: =====================");
                System.out.println("用户地址: =======");
                List<UserAddress> userAddresses =  userService.getUserAdderssList(userId);
                for (UserAddress userAddress: userAddresses) {
                    System.out.println("====: " + userAddress);
                }
                System.out.println("用户地址: =======");
                return "用户地址: " + userAddresses;
            }
        }
        ```
    - 主程序类添加`@EnableDubbo`注解开启基于注解的`Dubbo`功能
        ```
        @EnableDubbo
        @SpringBootApplication
        public class ServiceConsumerOrderApplication {
            public static void main(String[] args) {
                SpringApplication.run(ServiceProviderTicketApplication.class, args);
            }
        }
        ```

4. 创建`Controller`

```
@RestController
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapper("/orderinit")
    public String initOrder(@RequestParam("uid") String userId) {
        String order = orderService.initOrder(userId);
        return order;
    }
}
```

5. `Consumer`从`Registry`订阅服务
    - 配置`Consumer`订阅服务
        ```
        # 修改服务器端口
        server.port=8081
        # 应用名
        dubbo.application.name=boot-order-service-consumer
        # 服务中心地址
        dubbo.registry.address=zookeeper://127.0.0.1:2181
        # 监控中心自动发现应用
        dubbo.monitor.protocol=registry
        ```

## 4 配置策略

### 4.1 配置优先级

`Dubbo`可用的配置方式以及优先级如下:

1. `JVM`启动参数: 

```
-Ddubbo.protocol.port=20880
```

2. `Spring`配置文件或`appication.properties`: 

```
<dubbo:protocol port="20880"></dubbo:protocol>
```

3. 公共配置文件(`dubbo.properties`): 

```
dubbo.protocol.port=20880
```

### 4.2 示例

> 优先级: 

- 精确优先(方法级优先,接口级次之,全局配置再次之)
- 消费者优先(级别一致,消费者优先于提供者)

1. 启动时检查

`Dubbo`默认在`Consumer`启动时检查订阅的服务是否可用,不可用则会抛出异常.默认值`check=true`,可以设置`check=false`关闭检查.

- `Spring`配置文件
    - 关闭某个服务检查
        ```
        <dubbo:reference interface="com.foo.BarService" check="false" />
        ```
    - 关闭所有服务检查
        ```
        <dubbo:consumer check="false" />
        ```
    - 关闭启动时检查注册中心是否存在
        ```
        <dubbo:registry check="false" />
        ```
- `dubbo.properties`
    ```
    dubbo.reference.com.foo.BarService.check=false
    dubbo.reference.check=false
    dubbo.consumer.check=false
    dubbo.registry.check=false
    ```

- `-D`参数
    ```
    java -Ddubbo.reference.com.foo.BarService.check=false
    java -Ddubbo.reference.check=false
    java -Ddubbo.consumer.check=false 
    java -Ddubbo.registry.check=false
    ```

2. 超时
`Dubbo`默认设置调用服务超时时间是`1000ms`

- `Spring`配置文件
    - 设置某个服务的某个方法超时时间
        ```
        <dubbo:reference interface="com.foo.BarService">
            <dubbo:method timeout="2000"></dubbo:method>
        </dubbo:reference>
        ```
    - 设置某个服务超时时间
        ```
        <dubbo:reference interface="com.foo.BarService" timeout="2000" />
        ```
    - 设置所有服务超时时间
        ```
        <dubbo:consumer timeout="2000" />
        ```
- `dubbo.properties`
    ```
    dubbo.reference.com.foo.BarService.timeout=2000
    dubbo.reference.timeout=2000
    dubbo.consumer.timeout=2000
    ```

- `-D`参数
    ```
    java -Ddubbo.reference.com.foo.BarService.timeout=2000
    java -Ddubbo.reference.timeout=2000
    java -Ddubbo.consumer.timeout=2000 
    ```
3. 重复次数(`retries`)
    - 调用失败后再重复指定次数
    - 重复次数为`0`,则为不重复
    - 如果存在多个`Provider`,依次调用不同的`Provider`,直到成功或者重复指定次数
    - 幂等(多次执行结果一致)方法设置重复次数,非幂等方法不设置重复次数

4. 多版本(`version`)
    - `Provider`的同一个接口有多个实现,以不同的`version`来区分,`Consumer`调用对应`version`的服务

5. 本地存根
    - 在公共接口中实现是接口,本地检查接口参数再判断是否继续调用远程接口实现
    - 本地存根实现必须要有一个有参构造器,参数为接口远程实例,给实例自动注入远程服务实现实例
        ```
        public class UserServiceStub implements UserService {
            private final UserService userService;
            public UserServiceStub (UserService userService) {
                this.userService = userService;
            }
            public List<UserAddress> getUserAdderssList(String userId) {
                if (!StringUtil.isEmpty(useId)) {
                    return userService.getUserAddressList(userId);
                }
                return null;
            }
        }
        ```
        - 可以在`Provider`或者`Consumer`配置本地存根
            - `Provider`:
                ```
                <dubbo:service interface="com.qpf.mall.UserService" stub="true"></dubbo:service>
                # 或
                <dubbo:service interface="com.qpf.mall.service.UserService" stub="com.qpf.mall.service.stub.UserServiceStub"></dubbo:service>
                ```
            - `Consumer`
                ```
                <dubbo:reference interface="com.qpf.mall.UserService" stub="true"  stub="com.qpf.mall.service.stub.UserServiceStub"></dubbo:reference>
                ```
6. 
