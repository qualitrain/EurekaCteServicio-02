package org.qtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@EnableEurekaClient 
@EnableCircuitBreaker
@EnableHystrixDashboard
@SpringBootApplication
@ServletComponentScan("org.qtx.web")
public class EurekaCteServicio02Application {

	public static void main(String[] args) {
		SpringApplication.run(EurekaCteServicio02Application.class, args);
	}

}
