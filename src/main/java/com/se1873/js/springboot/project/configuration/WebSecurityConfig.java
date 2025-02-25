package com.se1873.js.springboot.project.configuration;

import com.se1873.js.springboot.project.repository.EmployeeRepository;
import com.se1873.js.springboot.project.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Optional;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository, EmployeeRepository employeeRepository) throws Exception {
    http
            .authorizeHttpRequests((requests) ->
                    requests
                            .requestMatchers("/", "/home", "/login", "/css/**", "/js/**").permitAll() // Cho phép truy cập các trang này
                            .requestMatchers("/payroll","/employee/employee-insert","/employee").hasRole("ADMIN")
                            .anyRequest().authenticated() // Các trang khác yêu cầu đăng nhập
            )
            .formLogin((form) ->
                    form
                            .loginPage("/login") // Chỉ định trang login tùy chỉnh
                            .successHandler((request, response, authentication) -> {
                                boolean isAdmin = false;
                                for(GrantedAuthority ga : authentication.getAuthorities()){
                                    if(ga.getAuthority().equals("ROLE_ADMIN")){
                                        isAdmin = true;
                                        break;
                                    }
                                }

                                if (isAdmin){
                                    response.sendRedirect("/employee");
                                }else{
//                                    String username = authentication.getName();
//                                    Optional<com.se1873.js.springboot.project.entity.User> user = userRepository.findUserByUsername(username);
//                                    int id = user.get().getEmployee().getEmployeeId();
                                    response.sendRedirect("/user/detail");
                                }
                            })
                            .failureUrl("/login?error=true") // Xử lý lỗi đăng nhập
                            .permitAll()
            )
            .logout((logout) -> logout
                    .logoutUrl("/logout")
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                    .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi đăng xuất
                    .permitAll()
            );

    return http.build();
  }
   @Bean
//    public PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance();
//    }
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
      return new  BCryptPasswordEncoder();
    }

    @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
      return username -> {
          com.se1873.js.springboot.project.entity.User u = userRepository.findUserByUsername(username)
                 .orElseThrow(() ->  new UsernameNotFoundException("not found user " +username));
              return User.withUsername(u.getUsername())
                      .password(u.getPasswordHash())
                      .roles(u.getRole())
                      .build();
          };
      }
  }




