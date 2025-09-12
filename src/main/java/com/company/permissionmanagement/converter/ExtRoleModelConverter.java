package com.company.permissionmanagement.converter;


import com.company.permissionmanagement.entity.ExtResourceRoleModel;
import io.jmix.core.EntityStates;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RoleModelConverter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Primary
public class ExtRoleModelConverter extends RoleModelConverter {

    private final EntityStates entityStates;

    public ExtRoleModelConverter(EntityStates entityStates) {
        super(entityStates);
        this.entityStates = entityStates;
    }

    @Override
    public ExtResourceRoleModel createResourceRoleModel(ResourceRole role) {
        ExtResourceRoleModel model = this.metadata.create(ExtResourceRoleModel.class);
        this.initBaseParameters(model, role);
        model.setScopes(role.getScopes());
        model.setResourcePolicies(this.createResourcePolicyModels(role.getResourcePolicies()));
        this.entityStates.setNew(model, false);

        // Lấy từ customProperties
        Map<String, String> props = role.getCustomProperties();
        if (props != null) {
            String raw = props.get("forUser");
            if (raw != null) {
                model.setForUser(Boolean.parseBoolean(raw));
            }
        }

        // Đồng bộ lại vào customProperties
        if (model.getForUser() != null) {
            model.getCustomProperties().put("forUser", String.valueOf(model.getForUser()));
        }

        return model;
    }

}