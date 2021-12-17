package com.leyou.order.config;

import com.leyou.common.utils.IdWorker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IdWorkerProperties.class)
public class IdWorkConfig {


    //构造IdWork对象，存入Spring
    @Bean
    public IdWorker idWorker(IdWorkerProperties prop){
        return new IdWorker(prop.getWorkerId(),prop.getDataCenterId());
    }
}
