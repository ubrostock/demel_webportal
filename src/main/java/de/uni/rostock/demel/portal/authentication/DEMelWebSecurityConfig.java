/*
 * This file is part of the DEMel web application.
 * 
 * Copyright 2023
 * DEMel project team, Rostock University, 18051 Rostock, Germany
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni.rostock.demel.portal.authentication;

import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration for WebSecurity
 * @author Robert Stephan
 * 
 * migrated to Spring 6 WebSecurity
 * tutorials: https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter,
 *            https://www.baeldung.com/spring-security-multiple-auth-providers
 */
@Configuration
@EnableWebSecurity
public class DEMelWebSecurityConfig {

    @Autowired
    private DEMelAuthenticationProvider authProvider;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
            .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    // earlier there was a method access("hasRole('MANAGER') or hasIpAddress('127.0.0.1') or hasIpAddress('::1')") 
    // that allowed the combinatation of a role and IP address (useful for development)
    // this still works with Thymeleaf security context, e.g.:  
    // <li class="nav-item" th:if="${#authorization.expression(@environment.getProperty('demel.security.rule.manager'))}">administration</li>
    // but no longer in JavaCode?
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(new RemoteIpFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests()
            // hasRole("MANAGER) == hasAuthority("ROLE_MANAGER")
            // use @PreAuthorize() on methods instead: 
            // requestMatchers("/browse/admin/**", "/indexing/**").hasRole("MANAGER")
            .anyRequest().permitAll()
            .and().formLogin().loginPage("/login").permitAll()
            .and().logout().logoutUrl("/logout").logoutSuccessUrl("/").permitAll()
            .and().csrf();
        return http.build();
    }
}
