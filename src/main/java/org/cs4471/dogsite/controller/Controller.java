package org.cs4471.dogsite.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.cs4471.dogsite.Response;
import org.cs4471.dogsite.registry.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @Autowired
    private RegistryService registryService;

    // Modify this, this is the root endpoint
    @GetMapping("/root")
    public Response root() {
        return new Response(200, "HelloWorld", "Hello World!");
    }

    // Example with a request value
    @GetMapping("/test")
    public Response test(HttpServletRequest request) {
        String value = request.getParameter("value");
        if (value == null) value = "null";
        return new Response(200, "HelloWorld", "Hello World with value = " + value + "!");
    }

    // Sends back a heartbeat state when requested by registry
    @GetMapping("/heartbeat")
    public Response heartbeat() {
        System.out.println("HelloWorld : Received heartbeat ping from registry");
        return new Response(200, "HelloWorld", "");
    }

    // Shut down the service and deregister from registry
    @GetMapping("/exit")
    public void exit() {
        System.out.println("HelloWorld : Exiting in 30 seconds...");
        // Deregister and exit
        registryService.Deregister();
        System.out.println("HelloWorld : Exiting...");
        System.exit(1);
    }
}
