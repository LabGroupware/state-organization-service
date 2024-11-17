package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.api.state.organizationservice.audit.AuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class SpringSecurityAuditorAwareConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditAware();
    }
}
