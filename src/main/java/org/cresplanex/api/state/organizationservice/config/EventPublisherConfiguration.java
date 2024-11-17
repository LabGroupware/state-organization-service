package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.events.publisher.CoreEventPublisherConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    // 実装
    CoreEventPublisherConfiguration.class
})
public class EventPublisherConfiguration {
}
