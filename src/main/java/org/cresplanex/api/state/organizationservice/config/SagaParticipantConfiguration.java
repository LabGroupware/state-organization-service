package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.commands.common.CommandNameMapping;
import org.cresplanex.core.commands.common.CommandNameMappingDefaultConfiguration;
import org.cresplanex.core.commands.consumer.CommandReplyProducer;
import org.cresplanex.core.commands.consumer.CommandReplyProducerConfiguration;
import org.cresplanex.core.messaging.consumer.MessageConsumer;
import org.cresplanex.core.saga.lock.SagaLockManager;
import org.cresplanex.core.saga.participant.SagaCommandDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    CommandReplyProducerConfiguration.class,
    CommandNameMappingDefaultConfiguration.class
})
public class SagaParticipantConfiguration {

    @Bean
    public SagaCommandDispatcherFactory sagaCommandDispatcherFactory(MessageConsumer messageConsumer,
            SagaLockManager sagaLockManager,
            CommandNameMapping commandNameMapping,
            CommandReplyProducer commandReplyProducer) {
        return new SagaCommandDispatcherFactory(messageConsumer, sagaLockManager, commandNameMapping, commandReplyProducer);
    }
}
