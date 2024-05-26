package com.tcellpair3.customerservice.core.security;

import com.turkcell.tcell.core.security.BaseSecurityService;
import jakarta.ws.rs.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class CustomerSecurityConfiguration {

    private final BaseSecurityService baseSecurityService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        baseSecurityService.configureCommonSecurityRules(http);
        http.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers("/api/v1/customers/**").permitAll()
                        .requestMatchers("/api/v1/address/**").authenticated()
                        .requestMatchers("/api/v1/contactMedium/").authenticated()
                        .requestMatchers("/api/v1/customerInvoices/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/address/**").hasAnyAuthority("Test.Delete")
                        .anyRequest().permitAll()
        );
        return http.build();
    }
}