package com.tripledip.wordherdserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@SpringBootApplication
class WordHerdServerApplication

fun main(args: Array<String>) {
    runApplication<WordHerdServerApplication>(*args)
}

@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/", "/static/**", "/favicon.ico").permitAll()
            .anyRequest().authenticated().and()
            .csrf().ignoringAntMatchers("/logout").and()
            .oauth2Login().loginPage("/").defaultSuccessUrl("/", true).and()
            .logout().logoutSuccessUrl("/")
    }
}

@Configuration
@EnableWebSocketMessageBroker
class WebsocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/words")
            .setAllowedOrigins("http://localhost:3000", "http://lvh.me:8080")
            .withSockJS()
    }
}
