package com.company.permissionmanagement.converter;


import com.company.permissionmanagement.entity.ExtendResourceRoleModel;
import io.jmix.core.EntityStates;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleModelConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Primary
public class ExtendRoleModelConverter extends RoleModelConverter {

    private final EntityStates entityStates;

    public ExtendRoleModelConverter(EntityStates entityStates) {
        super(entityStates);
        this.entityStates = entityStates;
    }

    public ExtendResourceRoleModel createExtResourceRoleModel(ResourceRole role) {
        ExtendResourceRoleModel roleModel = this.metadata.create(ExtendResourceRoleModel.class);
        this.initBaseParameters(roleModel, role);
        roleModel.setScopes(role.getScopes());
        roleModel.setResourcePolicies(this.createResourcePolicyModels(role.getResourcePolicies()));
        this.entityStates.setNew(roleModel, false);
        Map<String, String> props = role.getCustomProperties();
        if (props != null) {
            String raw = props.get("forUser");
            if (raw != null) {
                roleModel.setForUser(Boolean.parseBoolean(raw));
            }
        }
        return roleModel;
    }
}
