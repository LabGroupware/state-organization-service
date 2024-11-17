package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.common.id.IdGenerator;
import org.cresplanex.core.common.jdbc.CoreCommonJdbcOperations;
import org.cresplanex.core.common.jdbc.CoreSchema;
import org.cresplanex.core.messaging.common.ChannelMapping;
import org.cresplanex.core.messaging.common.ChannelMappingDefaultConfiguration;
import org.cresplanex.core.messaging.common.MessageInterceptor;
import org.cresplanex.core.messaging.producer.MessageProducer;
import org.cresplanex.core.messaging.producer.MessageProducerImpl;
import org.cresplanex.core.messaging.producer.MessageProducerImplementation;
import org.cresplanex.core.messaging.producer.jdbc.MessageProducerJdbcImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    // 実装
    CoreCommonJdbcOperationsConfiguration.class, // JDBCの実装
    // マッピング
    ChannelMappingDefaultConfiguration.class, // デフォルトのチャネルマッピング利用
})
public class MessageProducerConfiguration {

    // JDBCを利用
    @Bean
    public MessageProducerImplementation messageProducerImplementation(CoreCommonJdbcOperations coreCommonJdbcOperations,
            IdGenerator idGenerator,
            CoreSchema coreSchema) {
        return new MessageProducerJdbcImplementation(coreCommonJdbcOperations,
                idGenerator,
                coreSchema);
    }

    @Autowired(required = false)
    private final MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

    @Bean
    public MessageProducer messageProducer(ChannelMapping channelMapping, MessageProducerImplementation implementation) {
        return new MessageProducerImpl(messageInterceptors, channelMapping, implementation);
    }
}
