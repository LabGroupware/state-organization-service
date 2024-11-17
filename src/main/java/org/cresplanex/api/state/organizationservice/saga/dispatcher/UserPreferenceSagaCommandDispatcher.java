package org.cresplanex.api.state.organizationservice.saga.dispatcher;

import lombok.AllArgsConstructor;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.dispatcher.BaseSagaCommandDispatcher;
import org.cresplanex.api.state.organizationservice.saga.handler.UserPreferenceSagaCommandHandlers;
import org.cresplanex.core.saga.participant.SagaCommandDispatcher;
import org.cresplanex.core.saga.participant.SagaCommandDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class UserPreferenceSagaCommandDispatcher extends BaseSagaCommandDispatcher {

    @Bean
    public SagaCommandDispatcher userProfileSagaCommandHandlersDispatcher(
            UserPreferenceSagaCommandHandlers userPreferenceSagaCommandHandlers,
            SagaCommandDispatcherFactory sagaCommandDispatcherFactory) {
        return sagaCommandDispatcherFactory.make(
                this.getDispatcherId(SagaCommandChannel.USER_PREFERENCE),
                userPreferenceSagaCommandHandlers.commandHandlers());
    }
}
