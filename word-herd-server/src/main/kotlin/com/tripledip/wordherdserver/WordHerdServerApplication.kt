package com.tripledip.wordherdserver

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.server.ServerHttpRequest
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal


@SpringBootApplication
class WordHerdServerApplication

fun main(args: Array<String>) {
    runApplication<WordHerdServerApplication>(*args)
}

@Configuration
class SecurityConfig {
    @Bean
    fun webSecurityConfigurerAdapter(environment: Environment) =
        if (environment.acceptsProfiles("insecure")) {
            object : WebSecurityConfigurerAdapter() {
                override fun configure(http: HttpSecurity) {
                    http.authorizeRequests().anyRequest().permitAll()
                }
            }
        } else {
            object : WebSecurityConfigurerAdapter() {
                override fun configure(http: HttpSecurity) {
                    http.authorizeRequests()
                        .antMatchers("/", "/static/**", "/favicon.ico", "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                        .and().csrf().ignoringAntMatchers("/logout")
                        .and().oauth2Login().loginPage("/").defaultSuccessUrl("/", true)
                        .and().logout().logoutSuccessUrl("/")
                }
            }
        }
}

@Configuration
@EnableWebSocketMessageBroker
class WebsocketConfig(@Value("client.allowed.origin") val allowedOrigin: String) : WebSocketMessageBrokerConfigurer {

    val handshakeHandler = object : DefaultHandshakeHandler() {
        override fun determineUser(
            request: ServerHttpRequest,
            wsHandler: WebSocketHandler,
            attributes: MutableMap<String, Any>
        ): Principal? = super.determineUser(request, wsHandler, attributes) ?: Principal { "anonymous" }
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/words")
            .setHandshakeHandler(handshakeHandler)
            .setAllowedOrigins(allowedOrigin)
            .withSockJS()
    }
}
