package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.common.kafka.consumer.CoreKafkaMessageConsumer;
import org.cresplanex.core.common.kafka.consumer.swimlanemap.OriginalTopicPartitionToSwimlaneMapping;
import org.cresplanex.core.common.kafka.consumer.swimlanemap.TopicPartitionToSwimlaneMapping;
import org.cresplanex.core.common.kafka.lower.KafkaConsumerFactory;
import org.cresplanex.core.common.kafka.lower.KafkaConsumerFactoryConfiguration;
import org.cresplanex.core.common.kafka.property.CoreKafkaConnectProperties;
import org.cresplanex.core.common.kafka.property.CoreKafkaConnectPropertiesConfiguration;
import org.cresplanex.core.common.kafka.property.CoreKafkaConsumerProperties;
import org.cresplanex.core.common.kafka.property.CoreKafkaConsumerSpringPropertiesConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    KafkaConsumerFactoryConfiguration.class, // Kafkaの低レイヤAPI呼び出しにSDKのラッパーを利用.
    CoreKafkaConsumerSpringPropertiesConfiguration.class, // Kafkaコンシューマのプロパティ設定にSpringプロパティの注入を行う.
    CoreKafkaConnectPropertiesConfiguration.class // Kafka接続のプロパティ設定にSpringプロパティの注入を行う.
})
public class CoreKafkaMessageConsumerConfiguration {

    // パーティションからスイムレーンへのデフォルトのマッピング
    private final TopicPartitionToSwimlaneMapping partitionToSwimLaneMapping = new OriginalTopicPartitionToSwimlaneMapping();

    /**
     * Kafkaメッセージを消費するCoreKafkaMessageConsumerビーンを作成
     *
     * @param props Kafkaの基本プロパティ
     * @param coreKafkaConsumerProperties Kafkaコンシューマの設定
     * @param kafkaConsumerFactory Kafkaコンシューマを生成するファクトリ
     * @return 設定済みのCoreKafkaMessageConsumerインスタンス
     */
    @Bean
    public CoreKafkaMessageConsumer messageConsumerKafka(CoreKafkaConnectProperties props,
            CoreKafkaConsumerProperties coreKafkaConsumerProperties,
            KafkaConsumerFactory kafkaConsumerFactory) {
        return new CoreKafkaMessageConsumer(props.getBootstrapServers(), coreKafkaConsumerProperties, kafkaConsumerFactory, partitionToSwimLaneMapping);
    }
}
