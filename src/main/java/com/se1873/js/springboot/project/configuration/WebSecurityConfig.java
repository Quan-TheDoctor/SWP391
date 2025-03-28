package com.se1873.js.springboot.project.configuration;

import com.se1873.js.springboot.project.controller.GlobalController;
import com.se1873.js.springboot.project.entity.Role;
import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import com.se1873.js.springboot.project.security.CustomPermissionEvaluator;
import com.se1873.js.springboot.project.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final CustomPermissionEvaluator permissionEvaluator;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, EmployeeRepository employeeRepository, 
            GlobalController globalController) throws Exception {
        http
            .authorizeHttpRequests((requests) ->
                requests
                    .requestMatchers("/api/face-recognition/recognition-success").permitAll()
                    .requestMatchers("/", "/home", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/api/attendance/recognize", "/api/resume/**", "/resume").permitAll()
                    .requestMatchers("/leave/submit-leave").authenticated()
                    .anyRequest().authenticated()
            )
            .formLogin((form) ->
                form
                    .loginPage("/login")
                    .successHandler(successHandler())
                    .failureUrl("/login?error=true")
                    .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/payroll/**","/api/roles/**", "/api/attendance/**", "/api/face-recognition/**",
                    "/api/chat/**", "/api/chat", "/resume/**", "/request/bulk-delete", 
                    "/recruitment/**", "/request/bulk-approve", "/request/bulk-deny", "/api/roles/permissions")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .exceptionHandling((exceptions) -> exceptions
                .accessDeniedHandler(accessDeniedHandler())
            );
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.se1873.js.springboot.project.entity.User u = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("not found user " + username));
            
            if (u.getStatus() != null && "locked".equals(u.getStatus())) {
                throw new org.springframework.security.authentication.LockedException("Tài khoản đã bị khóa");
            }

            return User.withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles(u.getRole())
                .build();
        };
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        handler.setErrorPage("/access-denied");
        return handler;
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/");
        return handler;
    }
}




