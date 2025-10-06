package com.company.permissionmanagement.extension;

import com.company.permissionmanagement.annotation.ForUser;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilderImpl;
import io.jmix.security.impl.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.impl.role.builder.extractor.RowLevelPolicyExtractor;
import io.jmix.security.model.ResourceRole;
import io.jmix.core.ClassManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;

@Component("sec_CustomAnnotatedRoleBuilder")
@Primary
public class ExtAnnotatedRoleBuilder extends AnnotatedRoleBuilderImpl {

    public ExtAnnotatedRoleBuilder(
            Collection<ResourcePolicyExtractor> resourcePolicyExtractors,
            Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors,
            ClassManager classManager
    ) {
        super(resourcePolicyExtractors, rowLevelPolicyExtractors, classManager);
    }

    @Override
    public ResourceRole createResourceRole(String className) {
        ResourceRole role = super.createResourceRole(className);
        Class<?> roleClass;
        try {
            roleClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Role class not found: " + className, e);
        }

        if (roleClass.isAnnotationPresent(ForUser.class)) {
            ForUser ann = roleClass.getAnnotation(ForUser.class);
            role.getCustomProperties().put("forUser", String.valueOf(ann.value()));

            String dbId = role.getCustomProperties().get("databaseId");
            if (dbId == null || dbId.isBlank()) {
                String stable = UUID.nameUUIDFromBytes(role.getCode().getBytes(StandardCharsets.UTF_8)).toString();
                role.getCustomProperties().put("databaseId", stable);
            }
        }

        return role;
    }

}
