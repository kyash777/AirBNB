package com.yash.projects.airBnb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.stripe.net.RequestOptions;

@Configuration
public class StripeConfig {

    @Bean
    public RequestOptions stripeRequestOptions(@Value("${stripe.secret.key}") String stripeSecretKey) {
        return RequestOptions.builder()
                .setApiKey(stripeSecretKey)
                .build();
    }
}

