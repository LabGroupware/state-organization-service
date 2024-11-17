package org.cresplanex.api.state.organizationservice.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@RequiredArgsConstructor
@Configuration
public class ApplicationConfiguration {

    @Value("${app.name}")
    private String name;

    @Value("${app.version}")
    private String version;
}
