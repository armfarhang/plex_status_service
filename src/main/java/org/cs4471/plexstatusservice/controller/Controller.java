package org.cs4471.plexstatusservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.cs4471.plexstatusservice.Response;
import org.cs4471.plexstatusservice.registry.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @Value("${service.name}")
    private String serviceName;

    @Autowired
    private RegistryService registryService;
    // Sends back a heartbeat state when requested by registry
    @GetMapping("/heartbeat")
    public Response heartbeat() {
        System.out.println(String.format("%s : Received heartbeat ping from registry", serviceName));
        return new Response(200, serviceName, "");
    }

    @GetMapping("/deregister")
    public void deregister() {
        System.out.println(String.format("%s : Deregistering from registry...", serviceName));
        // Deregister and exit
        registryService.Deregister();
        System.out.println(String.format("%s : Done!", serviceName));
    }

    // Shut down the service and deregister from registry
    @GetMapping("/exit")
    public void exit() {
        System.out.println(String.format("%s : Exiting in 30 seconds...", serviceName));
        // Deregister and exit
        registryService.Deregister();
        System.out.println(String.format("%s : Exiting...", serviceName));
        System.exit(1);
    }
}
