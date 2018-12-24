package cn.study.microservice.license;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//该注解表明该类是项目(微服务)的启动类
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
public class Application {
	//运行该方法，会启动整个Spring Boot服务
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	//该注解告知Spring Cloud创建一个基于Ribbon的RestTemplate，才可以实现客户端负载均衡
	@LoadBalanced
	@Bean
	public RestTemplate getRestTemplate(){
		return new RestTemplate();
	}
}
