package org.cs4471.service.registry;

import com.google.gson.Gson;
import org.cs4471.service.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component("registryService")
public class RegistryService {
    @Value("${service.registrar}")
    private String serviceRegistrar;

    @Value("${service.registrarbackup}")
    private String serviceRegistrarBackup;

    @Value("${service.name}")
    private String serviceName;

    @Value("${service.url}")
    private String serviceURL;

    @Value("${service.desc}")
    private String serviceDesc;

    public Response Register() {
        boolean r1success = false;
        boolean r2success = false;

        String send = String.format("/register?name=%s&url=%s&desc=%s", serviceName, serviceURL, serviceDesc);
        String result = WebClient.builder().baseUrl(serviceRegistrar).build().get().uri(send).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(Exception.class, ex -> Mono.just(""))
                .block();

        Response registryResponse = new Gson().fromJson(result, Response.class);
        if (registryResponse != null && registryResponse.getCode() == 200) r1success = true;

        String backupResult = WebClient.builder().baseUrl(serviceRegistrarBackup).build().get().uri(send).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(Exception.class, ex -> Mono.just(""))
                .block();

        Response backupResponse = new Gson().fromJson(backupResult, Response.class);
        if (backupResponse != null && backupResponse.getCode() == 200) r2success = true;

        if (r1success && r2success) {
            return new Response(200, serviceName, String.format("Registered %s to primary and backup registry", serviceName));
        }
        else if (r1success && !r2success) {
            return new Response(200, serviceName, String.format("Registered %s to only primary registry", serviceName));
        }
        else if (r2success && !r1success) {
            return new Response(200, serviceName, String.format("Registered %s to only backup registry", serviceName));
        }

        return new Response(400, serviceName, String.format("Failed to register %s to any registries", serviceName));
    }

    public void Deregister() {
        WebClient.builder().baseUrl(serviceRegistrar).build().get().uri(String.format("/deregister?name=%s", serviceName)).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(Exception.class, ex -> Mono.just(""))
                .block();

        WebClient.builder().baseUrl(serviceRegistrarBackup).build().get().uri(String.format("/deregister?name=%s", serviceName)).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(Exception.class, ex -> Mono.just(""))
                .block();
    }
}
