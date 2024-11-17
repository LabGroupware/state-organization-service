package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.commands.producer.CoreCommandProducerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    // 実装
    CoreCommandProducerConfiguration.class
})
public class CommandProducerConfiguration {
}
