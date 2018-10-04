package com.qpf.mall.config;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DubboConfig {

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("boot-service-consumer-price");
        return applicationConfig;
    }

    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();

        registryConfig.setAddress("zookeeper://116.85.54.176:2181");

        return registryConfig;
    }

    @Bean
    public MonitorConfig monitorConfig () {
        MonitorConfig monitorConfig = new MonitorConfig();

        monitorConfig.setProtocol("registry");

        return monitorConfig;
    }

}
