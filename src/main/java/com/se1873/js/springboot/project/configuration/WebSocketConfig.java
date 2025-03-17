package com.se1873.js.springboot.project.configuration;

import com.se1873.js.springboot.project.entity.User;
import com.se1873.js.springboot.project.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final UserService userService;

  public WebSocketConfig(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
      .setAllowedOriginPatterns("*")
      .addInterceptors(new HandshakeInterceptor() {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
          if(request instanceof ServletServerHttpRequest) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth != null && auth.isAuthenticated()) {
              String username = auth.getName();
              userService.findUserByUsername(username).ifPresent(u -> {
                attributes.put("userId", u.getUserId());
                attributes.put("username", u.getUsername());
                System.out.println("WebSocket handshake: Added userId=" + u.getUserId() + " for user " + username);
              });
            }
          }
          return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

        }
      })
      .withSockJS();
  }
}
