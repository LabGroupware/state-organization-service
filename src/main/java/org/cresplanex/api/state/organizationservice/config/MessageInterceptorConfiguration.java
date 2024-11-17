package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.interceptor.LoggingMessageInterceptor;
import org.cresplanex.core.messaging.common.MessageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageInterceptorConfiguration {

    @Bean
    public MessageInterceptor messageLoggingInterceptor() {
        return new LoggingMessageInterceptor();
    }
}
