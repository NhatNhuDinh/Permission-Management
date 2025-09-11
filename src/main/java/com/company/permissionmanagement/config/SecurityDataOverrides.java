package com.company.permissionmanagement.config;

import com.company.permissionmanagement.extcomponent.ExtAnnotatedResourceRoleProvider;
import com.company.permissionmanagement.extcomponent.ExtDatabaseResourceRoleProvider;
import com.company.permissionmanagement.extcomponent.ExtDatabaseRolePersistence;
import io.jmix.core.*;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.security.SecurityProperties;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder;
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider;
import io.jmix.security.impl.role.provider.ResourceRoleDetector;
import io.jmix.security.role.RolePersistence;
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider;
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityDataOverrides {


    @Bean(name = "sec_DatabaseResourceRoleProvider")
    public DatabaseResourceRoleProvider databaseResourceRoleProvider() {
        return new ExtDatabaseResourceRoleProvider();
    }

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
    @Bean(name = "sec_DatabaseRolePersistence") // <-- tên bean Jmix dùng, trùng để override
    public RolePersistence rolePersistence(ApplicationContext applicationContext,
                                           Metadata metadata,
                                           EntityStates entityStates,
                                           DataManager dataManager,
                                           FetchPlans fetchPlans,
                                           EntityImportExport entityImportExport,
                                           EntityImportPlans entityImportPlans,
                                           QueryTransformerFactory queryTransformerFactory,
                                           DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider) {
        return new ExtDatabaseRolePersistence(
                applicationContext, metadata, entityStates, dataManager, fetchPlans,
                entityImportExport, entityImportPlans, queryTransformerFactory, databaseRowLevelRoleProvider
        );
    }

}