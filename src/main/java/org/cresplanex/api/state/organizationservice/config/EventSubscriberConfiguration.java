package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.events.subscriber.CoreEventSubscriberConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    // 実装
    CoreEventSubscriberConfiguration.class
})
public class EventSubscriberConfiguration {
}
