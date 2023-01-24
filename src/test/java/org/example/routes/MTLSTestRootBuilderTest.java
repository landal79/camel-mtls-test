package org.example.routes;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.net.ssl.HostnameVerifier;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles({"test"})
@ContextConfiguration(
        initializers = {
                MTLSTestRootBuilderTest.PropertiesInitializer.class})
//@Import(MTLSTestRootBuilderTest.MockTestConfiguration.class)
public class MTLSTestRootBuilderTest {

    @TestConfiguration
    static class MockTestConfiguration {

        @Bean
        HostnameVerifier noopHostnameVerifier() {
            return new NoopHostnameVerifier();
        }

    }

    private static WireMockServer wireMockServer;

    @Value("${api.path}")
    private String urlToken;

    @Autowired
    private ProducerTemplate template;

    @BeforeAll
    public static void beforeClass() {
        wireMockServer = new WireMockServer(wireMockConfig()
                .dynamicHttpsPort()
                .dynamicPort()
                .keystorePath("src/test/resources/keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit")
                .keystoreType("PKCS12")
                .needClientAuth(true)
                .trustStorePath("src/test/resources/keystore-mtls-server.p12")
                .trustStorePassword("changeit")
                .trustStoreType("PKCS12")
                .notifier(new ConsoleNotifier(true)));

        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    public static void afterClass() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void setup() {
        WireMock.reset();
    }

    @Test
    void test_mtls() throws Exception {

        WireMock.stubFor(post(urlPathEqualTo(urlToken))
                .withHeader(Exchange.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(resourceToString("response.json"))
                )
        );

        final String response = template.requestBody(
                String.format(MTLSTestRootBuilder.DIRECT_ENDPOINT_FORMAT_PATTERN, MTLSTestRootBuilder.ROUTE_ID_TOKEN),
                "",
                String.class);

        assertNotNull(response);

    }

    ////////////////

    protected String resourceToString(String resourceName) throws Exception {
        String resource = IOUtils.toString(this.getClass().getResourceAsStream(Objects.requireNonNull(resourceName)), StandardCharsets.UTF_8);
        assertNotNull(resource);
        return resource;
    }

    static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    String.format("api.port=%s", wireMockServer.httpsPort())).applyTo(applicationContext.getEnvironment());
        }
    }

}
