package org.cs4471.service.controller;

import org.cs4471.service.Response;
import org.cs4471.service.registry.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    private int sleepTime = 5000;

    @Value("${service.name}")
    private String serviceName;

    @Value("${service.url}")
    private String serviceURL;

    @Value("${service.desc}")
    private String serviceDesc;

    @Autowired
    private RegistryService registryService;

    @GetMapping("/register")
    public Response register() {
        // Connect to service controller
        System.out.println(String.format("%s : Connecting to primary and backup registries...", serviceName));

        // Broadcast to service registry
        Response status = null;
        int retryCount = 0;
        try {
            while (retryCount < 5) {
                status = registryService.Register();

                if (status.getCode() == 200) {
                    System.out.println(String.format("%s : %s", serviceName, status.getResponse()));
                    break;
                }
                else {
                    System.out.println(String.format("%s : Failed to register, retrying... (%d of 5)", serviceName, retryCount + 1));
                    Thread.sleep(sleepTime);
                    retryCount++;
                }
            }
        }
        catch (Exception e) {}

        return status;
    }

    // Sends back a heartbeat state when requested by registry
    @GetMapping("/heartbeat")
    public Response heartbeat() {
        System.out.println(String.format("%s : Received heartbeat ping from registry", serviceName));
        return new Response(200, serviceName, "");
    }

    @GetMapping("/deregister")
    public Response deregister() {
        System.out.println(String.format("%s : Deregistering from registry...", serviceName));
        // Deregister and exit
        registryService.Deregister();
        System.out.println(String.format("%s : Done!", serviceName));
        return new Response(200, serviceName, "Deregistered from all registries");
    }

    // Shut down the service and deregister from registry
    @GetMapping("/exit")
    public void exit() {
        System.out.println(String.format("%s : Sending deregister requests to exit...", serviceName));
        // Deregister and exit
        registryService.Deregister();
        System.out.println(String.format("%s : Exiting...", serviceName));
        System.exit(1);
    }
}
