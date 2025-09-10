package com.company.permissionmanagement.security;

import com.company.permissionmanagement.annotation.ForUser;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilderImpl;
import io.jmix.security.impl.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.impl.role.builder.extractor.RowLevelPolicyExtractor;
import io.jmix.security.model.ResourceRole;
import io.jmix.core.ClassManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("sec_CustomAnnotatedRoleBuilder")
@Primary // Quan trọng: để Jmix ưu tiên bean này!
public class CustomAnnotatedRoleBuilder extends AnnotatedRoleBuilderImpl {

    public CustomAnnotatedRoleBuilder(
            Collection<ResourcePolicyExtractor> resourcePolicyExtractors,
            Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors,
            ClassManager classManager
    ) {
        super(resourcePolicyExtractors, rowLevelPolicyExtractors, classManager);
    }

    @Override
    public ResourceRole createResourceRole(String className) {
        ResourceRole role = super.createResourceRole(className);

        // Inject @ForUser (và các annotation khác nếu cần)
        Class<?> roleClass = null;
        try {
            roleClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Nếu không tìm được class, có thể log hoặc bỏ qua
        }

        if (roleClass != null && roleClass.isAnnotationPresent(ForUser.class)) {
            ForUser ann = roleClass.getAnnotation(ForUser.class);
            role.getCustomProperties().put("forUser", String.valueOf(ann.value()));
        }
        // Nếu có nhiều annotation custom, xử lý tương tự

        return role;
    }

    // Nếu bạn không custom row-level role thì không cần override createRowLevelRole
}
