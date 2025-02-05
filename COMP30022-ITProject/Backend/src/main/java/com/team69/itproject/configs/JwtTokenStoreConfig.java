package com.team69.itproject.configs;

import com.team69.itproject.enhancer.JwtTokenCustomEnhancer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
public class JwtTokenStoreConfig {
    @Value("${custom.sign-key}")
    private String key;

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        // JWT随机密钥
        jwtAccessTokenConverter.setSigningKey(key);
        jwtAccessTokenConverter.setVerifierKey(key);
        return jwtAccessTokenConverter;
    }

    @Bean
    public JwtTokenCustomEnhancer jwtTokenEnhancer() {
        return new JwtTokenCustomEnhancer();
    }

}
