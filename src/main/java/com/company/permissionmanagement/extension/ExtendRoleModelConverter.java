package com.company.permissionmanagement.extension;

import com.company.permissionmanagement.entity.ExtendResourceRoleModel;
import io.jmix.core.EntityStates;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RoleModelConverter;
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

    @Override
    public ExtendResourceRoleModel createResourceRoleModel(ResourceRole role) {
        ExtendResourceRoleModel model = this.metadata.create(ExtendResourceRoleModel.class);
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
