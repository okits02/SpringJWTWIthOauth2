package com.okits02.SpringJWTWithOauth2.configurations;


import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",
    };

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll());
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                        jwtConfigurer.decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConvert()))
        );
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConvert() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS512).build();
    }
}
