package com.se1873.js.springboot.project.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests((requests) ->
                    requests
                            .requestMatchers("/", "/home", "/login", "/css/**", "/js/**").permitAll() // Cho phép truy cập các trang này
                            .anyRequest().authenticated() // Các trang khác yêu cầu đăng nhập
            )
            .formLogin((form) ->
                    form
                            .loginPage("/login") // Chỉ định trang login tùy chỉnh
                            .defaultSuccessUrl("/dashboard", true) // Chuyển hướng sau khi đăng nhập thành công
                            .failureUrl("/login?error=true") // Xử lý lỗi đăng nhập
                            .permitAll()
            )
            .logout((logout) -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi đăng xuất
                    .permitAll()
            );

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user =
      User.withDefaultPasswordEncoder()
        .username("user")
        .password("password")
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(user);
  }
}
