package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.common.jdbc.CoreJdbcStatementExecutor;
import org.cresplanex.core.common.jdbc.CoreSchema;
import org.cresplanex.core.common.jdbc.CoreTransactionTemplate;
import org.cresplanex.core.common.jdbc.sqldialect.SqlDialectSelector;
import org.cresplanex.core.common.kafka.consumer.CoreKafkaMessageConsumer;
import org.cresplanex.core.messaging.common.ChannelMapping;
import org.cresplanex.core.messaging.common.ChannelMappingDefaultConfiguration;
import org.cresplanex.core.messaging.consumer.MessageConsumer;
import org.cresplanex.core.messaging.consumer.MessageConsumerImpl;
import org.cresplanex.core.messaging.consumer.MessageConsumerImplementation;
import org.cresplanex.core.messaging.consumer.decorator.BuiltInMessageHandlerDecoratorConfiguration;
import org.cresplanex.core.messaging.consumer.decorator.DecoratedMessageHandlerFactory;
import org.cresplanex.core.messaging.consumer.decorator.OptimisticLockingDecoratorConfiguration;
import org.cresplanex.core.messaging.consumer.duplicate.DuplicateMessageDetector;
import org.cresplanex.core.messaging.consumer.duplicate.SqlTableBasedDuplicateMessageDetector;
import org.cresplanex.core.messaging.consumer.kafka.MessageConsumerKafkaImplementation;
import org.cresplanex.core.messaging.consumer.subscribermap.SubscriberMapping;
import org.cresplanex.core.messaging.consumer.subscribermap.SubscriberMappingDefaultConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    // 実装
    CoreKafkaMessageConsumerConfiguration.class, // Kafkaの実装
    // Duplicate実装
    CoreCommonJdbcOperationsConfiguration.class,
    // マッピング
    ChannelMappingDefaultConfiguration.class, // デフォルトのチャネルマッピング利用
    SubscriberMappingDefaultConfiguration.class, // デフォルトのサブスクライバーIDマッピング利用
    // デコレータ
    BuiltInMessageHandlerDecoratorConfiguration.class, // decorator Factory + pre/post handle, pre/post receive interceptor + duplicate handling
    OptimisticLockingDecoratorConfiguration.class // 楽観的ロックの失敗時のリトライ
})
public class MessageConsumerConfiguration {

    // Kafkaを利用
    @Bean
    public MessageConsumerImplementation messageConsumerImplementation(CoreKafkaMessageConsumer coreKafkaMessageConsumer) {
        return new MessageConsumerKafkaImplementation(coreKafkaMessageConsumer);
    }

    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    // Duplicate実装
    @Bean
    public DuplicateMessageDetector duplicateMessageDetector(CoreSchema coreSchema,
            SqlDialectSelector sqlDialectSelector,
            CoreJdbcStatementExecutor coreJdbcStatementExecutor,
            CoreTransactionTemplate coreTransactionTemplate) {
        return new SqlTableBasedDuplicateMessageDetector(coreSchema,
                sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression(),
                coreJdbcStatementExecutor,
                coreTransactionTemplate);
    }

    // Consumerの実装
    @Bean
    public MessageConsumer messageConsumer(MessageConsumerImplementation messageConsumerImplementation,
            ChannelMapping channelMapping,
            DecoratedMessageHandlerFactory decoratedMessageHandlerFactory, SubscriberMapping subscriberMapping) {
        return new MessageConsumerImpl(channelMapping, messageConsumerImplementation, decoratedMessageHandlerFactory, subscriberMapping);
    }
}
