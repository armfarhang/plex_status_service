package org.cs4471.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements ApplicationRunner {
	@Value("${service.name}")
	private String serviceName;

	@Value("${service.url}")
	private String serviceURL;

	@Value("${service.desc}")
	private String serviceDesc;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println(String.format("%s : Starting service. URL: %s, Description: %s", serviceName, serviceURL, serviceDesc));
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println("Service Started!");
	}
}
