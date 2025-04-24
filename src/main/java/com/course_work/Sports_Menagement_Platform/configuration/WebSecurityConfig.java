package com.course_work.Sports_Menagement_Platform.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final CustomAuthenticationProvider customAuthenticationProvider;
    public WebSecurityConfig(CustomAuthenticationProvider customAuthenticationProvider) {
        this.customAuthenticationProvider = customAuthenticationProvider;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorizeRequest -> authorizeRequest
                                .requestMatchers("/admin").hasAuthority("ADMIN")
                                .requestMatchers("/user/**").authenticated()
                                .requestMatchers("/org_com/**").authenticated()
                                .requestMatchers("/team/**").authenticated()
                                .requestMatchers("/home").permitAll()
                                .requestMatchers("/registration").permitAll()
                                .requestMatchers("/uploads/**").permitAll()
                                .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll())
                .exceptionHandling(exception -> exception.accessDeniedPage("/access-denied"))
                .build();
    }



}
