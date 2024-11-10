package org.cs4471.dogsite.registry;

import com.google.gson.Gson;
import org.cs4471.dogsite.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component("registryService")
public class RegistryService {
    private String name, url, desc;
    private WebClient client;

    @Value("${service.name}")
    private String serviceName;

    public void Set(String server, String name, String url, String desc) {
        this.name = name;
        this.url = url;
        this.desc = desc;
        client = WebClient.builder().baseUrl(server).build();
    }

    public Response Register() {
        String send = String.format("/register?name=%s&url=%s&desc=%s", name, url, desc);
        String result = client.get().uri(send).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(Exception.class, ex -> Mono.just(""))
                .block();

        if (result == null || result.isEmpty()) return new Response(503, "Registry", String.format("Failed to register %s service with registry", serviceName));
        else return new Gson().fromJson(result, Response.class);
    }

    public void Deregister() {
        client.get().uri(String.format("/deregister?name=%s", name)).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(Exception.class, ex -> Mono.just(""))
                .block();
    }
}
