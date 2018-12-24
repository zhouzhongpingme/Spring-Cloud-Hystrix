package cn.study.microservice.license.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@RefreshScope  该注解暂时不加，在讲解刷新配置的时候，再去掉注释
@Component
public class ServiceConfig {

    @Value("${example.property}")
    private String exampleProperty;

    public String getExampleProperty(){
        return exampleProperty;
    }
}
