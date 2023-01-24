package org.example.routes;

import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.SSLContextServerParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


@Configuration
public class ApiSecurityConfiguration {

    public static class DisableValidationTrustManager implements X509TrustManager {
        public DisableValidationTrustManager() {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    @Autowired
    private ApiConfigurationProperties configurationProperties;

    @Bean(name = "apiSslContextParameters")
    public SSLContextParameters ncclSslContextParameters() {

        final ApiConfigurationProperties.SSL ssl = configurationProperties.getSsl();

        final SSLContextParameters sslContextParameters = new SSLContextParameters();


        final KeyManagersParameters keyManagersParameters = keyManagersParameters();

        final SSLContextServerParameters serverParameters = new SSLContextServerParameters();
        serverParameters.setClientAuthentication("WANT");

        sslContextParameters.setSecureSocketProtocol("TLSv1.2");
        sslContextParameters.setKeyManagers(keyManagersParameters);
        sslContextParameters.setServerParameters(serverParameters);


        final TrustManagersParameters trustManagersParameters = trustManagersParameters();
        sslContextParameters.setTrustManagers(trustManagersParameters);


        return sslContextParameters;
    }

    KeyManagersParameters keyManagersParameters() {

        final ApiConfigurationProperties.Client client = configurationProperties.getSsl().getClient();
        final ApiConfigurationProperties.Certificate certificate = client.getCertificate();

        final KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setResource(certificate.getPath());
        keyStoreParameters.setPassword(certificate.getPassword());
        keyStoreParameters.setType(certificate.getType());

        final KeyManagersParameters keyManagersParameters = new KeyManagersParameters();
        keyManagersParameters.setKeyStore(keyStoreParameters);
        keyManagersParameters.setKeyPassword(certificate.getPassword());

        return keyManagersParameters;
    }

    TrustManagersParameters trustManagersParameters() {

        final ApiConfigurationProperties.Server server = configurationProperties.getSsl().getServer();
        final TrustManagersParameters trustManagersParameters = new TrustManagersParameters();

        if (BooleanUtils.isTrue(server.getTrustSelfSigned())) {
            trustManagersParameters.setTrustManager(new DisableValidationTrustManager());
            return trustManagersParameters;
        } else {
            final ApiConfigurationProperties.Certificate certificate = server.getCertificate();

            final KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
            keyStoreParameters.setResource(certificate.getPath());
            keyStoreParameters.setPassword(certificate.getPassword());
            keyStoreParameters.setType(certificate.getType());
            trustManagersParameters.setKeyStore(keyStoreParameters);
        }

        return trustManagersParameters;
    }


}
