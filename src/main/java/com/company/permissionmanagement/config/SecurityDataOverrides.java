package com.company.permissionmanagement.config;

import com.company.permissionmanagement.components.ExtDatabaseResourceRoleProvider;
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityDataOverrides {


    @Bean(name = "sec_DatabaseResourceRoleProvider")
    public DatabaseResourceRoleProvider databaseResourceRoleProvider() {
        return new ExtDatabaseResourceRoleProvider();
    }
}
