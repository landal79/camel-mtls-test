package org.example.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MTLSTestRootBuilder extends RouteBuilder {

    static final String DIRECT_ENDPOINT_FORMAT_PATTERN = "direct://%s";
    static final String ROUTE_ID_TOKEN = "api-token";

    @Value("${api.path}")
    private String httpPath;

    @Override
    public void configure() throws Exception {

        fromF(DIRECT_ENDPOINT_FORMAT_PATTERN, ROUTE_ID_TOKEN)
                .routeId(ROUTE_ID_TOKEN)
                .removeHeaders("Camel*")
                .removeHeaders("Authorization")
                .setHeader(Exchange.HTTP_CHARACTER_ENCODING, constant(StandardCharsets.UTF_8.toString()))
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .setHeader(HttpHeaders.ACCEPT, constant(MediaType.APPLICATION_JSON_VALUE))
                .setHeader(HttpHeaders.ACCEPT_ENCODING, constant(StandardCharsets.UTF_8.toString()))
                .setHeader(Exchange.HTTP_PATH, constant(httpPath))
                .toD().allowOptimisedComponents(false).uri("{{api.url}}");

    }
}
