package org.example.routes;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfigurationProperties {

    @Data
    static class SSL {
        private Server server = new Server();
        private Client client = new Client();
    }

    @Data
    static class Server {
        private Certificate certificate;
        private Boolean trustSelfSigned = Boolean.FALSE;
    }

    @Data
    static class Client {
        private Certificate certificate;
    }

    @Data
    static class Certificate {
        private String path;
        private String type;
        private String password;
    }

    @Data
    static class Keystore {
        private String alias;
        private String path;
        private String type;
        private String password;
    }

    private String url;
    private String path;
    private SSL ssl = new SSL();
}
