package org.cs4471.plexstatusservice;

import org.cs4471.plexstatusservice.registry.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements ApplicationRunner {
	private int sleepTime = 5000;

	@Value("${service.name}")
	private String serviceName;

	@Value("${service.url}")
	private String serviceURL;

	@Value("${service.desc}")
	private String serviceDesc;

	@Autowired
	private RegistryService registryService;

	// Comment this entire section out for testing to prevent hanging
	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println(String.format("%s : Starting service with URL %s. Description: %s", serviceName, serviceURL, serviceDesc));

		// Connect to service controller
		System.out.println(String.format("%s : Connecting to primary and backup registries...", serviceName));

		// Broadcast to service registry
		try {
			while (true) {
				Response status = registryService.Register();

				if (status.getCode() == 200) {
					System.out.println(String.format("%s : %s", serviceName, status.getResponse()));
					break;
				}
				else {
					System.out.println(String.format("%s : Failed to register, retrying...", serviceName));
					Thread.sleep(sleepTime);
				}
			}
		}
		catch (Exception e) {}
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
