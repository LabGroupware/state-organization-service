package org.cresplanex.api.state.organizationservice.config;

import org.cresplanex.core.common.jdbc.CoreJdbcStatementExecutor;
import org.cresplanex.core.common.jdbc.CoreSchema;
import org.cresplanex.core.saga.lock.SagaLockManager;
import org.cresplanex.core.saga.lock.SagaLockManagerJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaLockManagerConfiguration {

    @Bean
    public SagaLockManager sagaLockManager(CoreJdbcStatementExecutor coreJdbcStatementExecutor,
            CoreSchema coreSchema) {
        return new SagaLockManagerJdbc(coreJdbcStatementExecutor, coreSchema);
    }
}
