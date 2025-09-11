package com.company.permissionmanagement.components;

import com.company.permissionmanagement.entity.ExtendResourceRoleEntity;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.security.model.ResourceRole;
import io.jmix.securitydata.impl.role.provider.DatabaseResourceRoleProvider;

import java.util.HashMap;
import java.util.Map;


public class ExtDatabaseResourceRoleProvider extends DatabaseResourceRoleProvider {

    @Override
    protected void buildFetchPlan(FetchPlanBuilder fb) {
        super.buildFetchPlan(fb);
        fb.add("forUser");
    }

    @Override
    protected ResourceRole buildRole(Object entity) {
        ResourceRole role = super.buildRole(entity);

        if (entity instanceof ExtendResourceRoleEntity ex) {
            Boolean forUser = ex.getForUser();
            if (forUser != null) {
                Map<String, String> props = role.getCustomProperties();
                if (props == null) props = new HashMap<>();
                props.put("forUser", forUser.toString());
                role.setCustomProperties(props);
            }
        }
        return role;
    }
}