package com.company.permissionmanagement.config;

import com.company.permissionmanagement.extension.ExtDatabaseResourceRoleProvider;
import com.company.permissionmanagement.extension.ExtendDatabaseRolePersistence;
import io.jmix.core.*;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.security.role.RolePersistence;
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider;
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityDataOverrides {


    @Bean(name = "sec_DatabaseResourceRoleProvider")
    public DatabaseResourceRoleProvider databaseResourceRoleProvider() {
        return new ExtDatabaseResourceRoleProvider();
    }

    @Bean(name = "sec_DatabaseRolePersistence")
    public RolePersistence rolePersistence(ApplicationContext applicationContext,
                                           Metadata metadata,
                                           EntityStates entityStates,
                                           DataManager dataManager,
                                           FetchPlans fetchPlans,
                                           EntityImportExport entityImportExport,
                                           EntityImportPlans entityImportPlans,
                                           QueryTransformerFactory queryTransformerFactory,
                                           DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider) {
        return new ExtendDatabaseRolePersistence(
                applicationContext, metadata, entityStates, dataManager, fetchPlans,
                entityImportExport, entityImportPlans, queryTransformerFactory, databaseRowLevelRoleProvider
        );
    }

}
