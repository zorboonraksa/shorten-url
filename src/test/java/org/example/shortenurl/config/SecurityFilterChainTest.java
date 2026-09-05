package org.example.shortenurl.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityFilterChainTest {

    @Test
    void buildsStatelessJwtSecurityFilterChain() {
        SecurityConfig config = new SecurityConfig();
        ObjectPostProcessor<Object> objectPostProcessor = new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        };
        AuthenticationManagerBuilder authenticationManagerBuilder =
                new AuthenticationManagerBuilder(objectPostProcessor);
        GenericApplicationContext applicationContext = applicationContext();
        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(ApplicationContext.class, applicationContext);
        HttpSecurity http = new HttpSecurity(
                objectPostProcessor,
                authenticationManagerBuilder,
                sharedObjects
        );
        AuthenticationEntryPoint entryPoint = (_, _, _) -> {
        };
        AccessDeniedHandler deniedHandler = (_, _, _) -> {
        };

        SecurityFilterChain result = config.securityFilterChain(
                http,
                entryPoint,
                deniedHandler
        );

        assertNotNull(result);
        applicationContext.close();
    }

    private GenericApplicationContext applicationContext() {
        SecretKey secretKey = new SecretKeySpec(
                "test-secret-key-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("jwtDecoder", jwtDecoder);
        context.refresh();
        return context;
    }
}
