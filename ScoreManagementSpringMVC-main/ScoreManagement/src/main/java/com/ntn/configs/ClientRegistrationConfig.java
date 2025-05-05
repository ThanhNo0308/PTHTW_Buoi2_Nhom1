package com.ntn.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

// Quản lý cấu hình OAuth2
@Configuration
public class ClientRegistrationConfig {

    @Autowired
    private Environment env;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
                googleClientRegistration(),
                facebookClientRegistration()
        );
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration
                .withRegistrationId("google")
                .clientId(env.getProperty("spring.security.oauth2.client.registration.google.client-id"))
                .clientSecret(env.getProperty("spring.security.oauth2.client.registration.google.client-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }

    private ClientRegistration facebookClientRegistration() {
        return ClientRegistration
                .withRegistrationId("facebook")
                .clientId(env.getProperty("spring.security.oauth2.client.registration.facebook.client-id"))
                .clientSecret(env.getProperty("spring.security.oauth2.client.registration.facebook.client-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("public_profile")
                .authorizationUri("https://www.facebook.com/v12.0/dialog/oauth")
                .tokenUri("https://graph.facebook.com/v12.0/oauth/access_token")
                .userInfoUri("https://graph.facebook.com/v12.0/me?fields=id,name,email,picture")
                .userNameAttributeName("id")
                .clientName("Facebook")
                .build();
    }
}
