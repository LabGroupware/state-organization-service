package org.cresplanex.api.state.organizationservice.config;

import java.util.Collection;

import org.cresplanex.core.commands.producer.CommandProducer;
import org.cresplanex.core.common.id.ApplicationIdGenerator;
import org.cresplanex.core.common.jdbc.CoreJdbcStatementExecutor;
import org.cresplanex.core.common.jdbc.CoreSchema;
import org.cresplanex.core.messaging.consumer.MessageConsumer;
import org.cresplanex.core.saga.lock.SagaLockManager;
import org.cresplanex.core.saga.orchestration.Saga;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.cresplanex.core.saga.orchestration.SagaManagerFactory;
import org.cresplanex.core.saga.orchestration.command.SagaCommandProducer;
import org.cresplanex.core.saga.orchestration.command.SagaCommandProducerConfiguration;
import org.cresplanex.core.saga.orchestration.repository.SagaInstanceRepository;
import org.cresplanex.core.saga.orchestration.repository.SagaInstanceRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    SagaCommandProducerConfiguration.class
})
public class SagaOrchestratorConfiguration {

    @Bean
    public SagaInstanceRepository sagaInstanceRepository(CoreJdbcStatementExecutor coreJdbcStatementExecutor,
            CoreSchema coreSchema) {
        return new SagaInstanceRepositoryJdbc(coreJdbcStatementExecutor, new ApplicationIdGenerator(), coreSchema);
    }

    @Bean
    public SagaInstanceFactory sagaInstanceFactory(SagaInstanceRepository sagaInstanceRepository, CommandProducer commandProducer, MessageConsumer messageConsumer,
            SagaLockManager sagaLockManager, SagaCommandProducer sagaCommandProducer, Collection<Saga<?>> sagas) {
        SagaManagerFactory smf = new SagaManagerFactory(sagaInstanceRepository, commandProducer, messageConsumer,
                sagaLockManager, sagaCommandProducer);
        return new SagaInstanceFactory(smf, sagas);
    }
}
