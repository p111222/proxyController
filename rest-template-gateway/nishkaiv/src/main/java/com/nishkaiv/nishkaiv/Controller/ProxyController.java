package com.nishkaiv.nishkaiv.Controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

// @RestController
// public class ProxyController {

//     private final RestTemplate restTemplate = new RestTemplate();

    // @PostMapping("/firstRestapi/**")
    // public ResponseEntity<String> proxyService(@RequestBody String message) {
    // String targetUrl = determineTargetUrl(message);
    // System.out.println("targetURL"+targetUrl);
    // HttpEntity<String> requestEntity;

    // if (shouldIncludeHeaders(message)) {
    // HttpHeaders headers = new HttpHeaders();
    // headers.add("client_id", "restapi");
    // headers.add("client_secret", "ALBuL7PIiq9Rz08eC62VrJypRYeuvenu");
    // requestEntity = new HttpEntity<>(message, headers);
    // } else {
    // requestEntity = new HttpEntity<>(message);
    // }

    // ResponseEntity<String> response = restTemplate.postForEntity(targetUrl,
    // requestEntity, String.class);
    // return
    // ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    // }

//     @PostMapping("/firstRestapi/**")
//     public ResponseEntity<String> proxyService(@RequestHeader("Message") String message) {
//         String targetUrl = determineTargetUrl(message);
//         System.out.println("targetURL: " + targetUrl);

//         HttpHeaders headers = new HttpHeaders();
//         headers.add("client_id", "restapi");
//         headers.add("client_secret", "ALBuL7PIiq9Rz08eC62VrJypRYeuvenu");

//         headers.add("Message", message);

//         HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

//         ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);

//         return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
//     }

//     private String determineTargetUrl(String message) {
//         return message.contains("service1") ? "http://43.204.108.73:8344/firstRestapi"
//                 : "http://43.204.108.73:8344/secondRestapi";
//     }

//     private boolean shouldIncludeHeaders(String message) {
//         return message.contains("service1");
//     }
// }


// @RestController
// public class ProxyController {

//     private final RestTemplate restTemplate = new RestTemplate();

//     @PostMapping("/firstRestapi/**")
//     public ResponseEntity<String> proxyService(@RequestHeader("Message") String message) {
//         String targetUrl = determineTargetUrl(message);
//         System.out.println("Target URL: " + targetUrl);

//         HttpEntity<String> requestEntity;

//         if (shouldIncludeHeaders(message)) {
//             HttpHeaders headers = new HttpHeaders();
//             headers.add("client_id", "restapi");
//             headers.add("client_secret", "ALBuL7PIiq9Rz08eC62VrJypRYeuvenu");
//             headers.add("Message", message); 

//             requestEntity = new HttpEntity<>("", headers);
//         } else {
//             requestEntity = new HttpEntity<>("");
//         }

//         ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);

//         return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
//     }

//     @PostMapping("/secondRestapi/**")
//     public ResponseEntity<String> proxyService1(@RequestHeader("Message") String message) {
//         String targetUrl = determineTargetUrl(message);
//         System.out.println("Target URL: " + targetUrl);

//         HttpEntity<String> requestEntity;

//         // if (shouldIncludeHeaders(message)) {
//         //     HttpHeaders headers = new HttpHeaders();
//         //     headers.add("client_id", "restapi");
//         //     headers.add("client_secret", "ALBuL7PIiq9Rz08eC62VrJypRYeuvenu");
//         //     headers.add("Message", message); 

//         //     requestEntity = new HttpEntity<>("", headers);
//         // } else {
//             requestEntity = new HttpEntity<>("");
//         // }

//         ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);

//         return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
//     }


//     private String determineTargetUrl(String message) {
//         return message.contains("service1") ? "http://43.204.108.73:8344/firstRestapi"
//                 : "http://43.204.108.73:8344/secondRestapi";
//     }

//     private boolean shouldIncludeHeaders(String message) {
//         return message.contains("service1");
//     }
// }


import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    // Initialize OpenTelemetry tracer
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("proxy-controller");

    @PostMapping("/firstRestapi/**")
    public ResponseEntity<String> proxyService(@RequestHeader("Message") String message) {
        // Start a new OpenTelemetry span for the proxyService method
        Span span = tracer.spanBuilder("proxyService - firstRestapi")
                .setSpanKind(SpanKind.SERVER) // Set span kind to server
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            String targetUrl = determineTargetUrl(message);
            System.out.println("Target URL: " + targetUrl);

            HttpEntity<String> requestEntity;

            if (shouldIncludeHeaders(message)) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("client_id", "restapi");
                headers.add("client_secret", "ALBuL7PIiq9Rz08eC62VrJypRYeuvenu");
                headers.add("Message", message);

                requestEntity = new HttpEntity<>("", headers);
            } else {
                requestEntity = new HttpEntity<>("");
            }

            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            // Record the exception in the span
            span.recordException(e);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR); // Mark span as error
            throw e; // Re-throw the exception
        } finally {
            // End the span
            span.end();
        }
    }

    @PostMapping("/secondRestapi/**")
    public ResponseEntity<String> proxyService1(@RequestHeader("Message") String message) {
        // Start a new OpenTelemetry span for the proxyService1 method
        Span span = tracer.spanBuilder("proxyService1 - secondRestapi")
                .setSpanKind(SpanKind.SERVER) // Set span kind to server
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            String targetUrl = determineTargetUrl(message);
            System.out.println("Target URL: " + targetUrl);

            HttpEntity<String> requestEntity;

            requestEntity = new HttpEntity<>("");

            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            // Record the exception in the span
            span.recordException(e);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR); // Mark span as error
            throw e; // Re-throw the exception
        } finally {
            // End the span
            span.end();
        }
    }

    private String determineTargetUrl(String message) {
        return message.contains("service1") ? "http://43.204.108.73:8344/firstRestapi"
                : "http://43.204.108.73:8344/secondRestapi";
    }

    private boolean shouldIncludeHeaders(String message) {
        return message.contains("service1");
    }
}
