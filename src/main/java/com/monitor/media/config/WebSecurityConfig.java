package com.monitor.media.config;

import com.monitor.media.service.CustomOdicUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private CustomOdicUserService customOdicUserService;

    @Autowired
    public WebSecurityConfig(CustomOdicUserService customOdicUserService) {
        this.customOdicUserService = customOdicUserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         http
                .securityMatcher("/**")
                .authorizeHttpRequests()
                .requestMatchers("/", "/login**", "/js/**", "/error**").permitAll()
                .anyRequest().authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll()
                .and()
                .csrf().disable()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(customOdicUserService);
        return http.build();
    }
}
