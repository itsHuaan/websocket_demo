package com.example.websocket_demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableJpaAuditing
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableJpaRepositories(
        basePackages = "com.example.websocket_demo.repository",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.example\\.websocket_demo\\.repository\\.(?!WolDeviceRepository|UserRepository|RoleRepository|RefreshTokenRepository|TokenBlackListRepository|TestRepository).*"
        )
)
public class JpaConfig {
    @Bean
    AuditorAware<String> auditorProvider(){
        return () -> Optional.of("SYSTEM");
    }
}
