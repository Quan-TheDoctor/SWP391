package com.se1873.js.springboot.project.configuration;

import com.se1873.js.springboot.project.controller.GlobalController;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository, EmployeeRepository employeeRepository, UserService userService, GlobalController globalController) throws Exception {
    http
      .authorizeHttpRequests((requests) ->
        requests
          .requestMatchers("/api/face-recognition/recognition-success").permitAll()
          .requestMatchers("/", "/home", "/login", "/request/**", "/css/**", "/js/**", "/api/attendance/recognize").permitAll() // Cho phép truy cập các trang này
          .requestMatchers("/payroll/**", "/attendance/**", "/request/**", "/employee/employee-insert", "/employee/**").hasAnyRole("ADMIN")
          .requestMatchers("/admin/**").hasRole("ADMIN")
          .anyRequest().authenticated() // Các trang khác yêu cầu đăng nhập
      )
      .formLogin((form) ->
        form
          .loginPage("/login") // Chỉ định trang login tùy chỉnh
          .successHandler((request, response, authentication) -> {
            boolean isAdmin = false;
            for (GrantedAuthority ga : authentication.getAuthorities()) {
              if (ga.getAuthority().equals("ROLE_ADMIN")) {
                isAdmin = true;
                break;
              }
            }

            User user = (User) authentication.getPrincipal();
            userService.handleSuccessfulLogin(user.getUsername(), response);

          })
          .failureUrl("/login?error=true") // Xử lý lỗi đăng nhập
          .permitAll()
      )
      .logout((logout) -> logout
        .logoutUrl("/logout")
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
        .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi đăng xuất
        .permitAll()
      )
      .csrf(csrf -> csrf
        .ignoringRequestMatchers("/api/attendance/**", "/api/face-recognition/**")
      );
    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return username -> {
      com.se1873.js.springboot.project.entity.User u = userRepository.findUserByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("not found user " + username));
      return User.withUsername(u.getUsername())
        .password(u.getPasswordHash())
        .roles(u.getRole())
        .build();
    };
  }
}




