package com.company.permissionmanagement.config;

import com.company.permissionmanagement.providers.ExtAnnotatedResourceRoleProvider;
import com.company.permissionmanagement.providers.ExtDatabaseResourceRoleProvider;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.security.SecurityProperties;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder;
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider;
import io.jmix.security.impl.role.provider.ResourceRoleDetector;
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityDataOverrides {


    @Bean(name = "sec_DatabaseResourceRoleProvider")
    public DatabaseResourceRoleProvider databaseResourceRoleProvider() {
        return new ExtDatabaseResourceRoleProvider();
    }

    // Annotated provider: ghi đè tên bean gốc
    @Bean(name = "sec_AnnotatedResourceRoleProvider")
    public AnnotatedResourceRoleProvider annotatedResourceRoleProvider(JmixModulesClasspathScanner classpathScanner,
                                                                       AnnotatedRoleBuilder annotatedRoleBuilder,
                                                                       ResourceRoleDetector detector,
                                                                       SecurityProperties securityProperties,
                                                                       ApplicationEventPublisher eventPublisher) {
        return new ExtAnnotatedResourceRoleProvider(
                classpathScanner, annotatedRoleBuilder, detector, securityProperties, eventPublisher
        );
    }
}
