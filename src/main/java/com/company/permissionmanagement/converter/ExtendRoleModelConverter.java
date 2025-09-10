package com.company.permissionmanagement.converter;


import com.company.permissionmanagement.annotation.ForUser;
import com.company.permissionmanagement.entity.ExtendResourceRoleModel;
import io.jmix.core.EntityStates;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleModelConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
@Primary
public class ExtendRoleModelConverter extends RoleModelConverter {

    private final EntityStates entityStates;

    public ExtendRoleModelConverter(EntityStates entityStates) {
        super(entityStates);
        this.entityStates = entityStates;
    }

    @Override
    public ResourceRoleModel createResourceRoleModel(ResourceRole role) {
        ResourceRoleModel base = super.createResourceRoleModel(role);
        ExtendResourceRoleModel model = new ExtendResourceRoleModel();
        BeanUtils.copyProperties(base, model);

        Map<String, String> props = role.getCustomProperties();
        if (props != null) {
            String raw = props.get("forUser");
            if (raw != null) {
                model.setForUser(Boolean.parseBoolean(raw));
            }
        }

        return model;
    }

}
