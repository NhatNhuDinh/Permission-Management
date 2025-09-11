package com.company.permissionmanagement.extcomponent;

import com.company.permissionmanagement.anotations.ForUser;
import io.jmix.core.DevelopmentException;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.security.SecurityProperties;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder;
import io.jmix.security.impl.role.event.ResourceRoleModifiedEvent;
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider;
import io.jmix.security.impl.role.provider.ResourceRoleDetector;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.ResourceRole;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ExtAnnotatedResourceRoleProvider extends AnnotatedResourceRoleProvider {

    // tự giữ các tham chiếu để dùng trong build/refresh
    private final JmixModulesClasspathScanner classpathScanner;
    private final AnnotatedRoleBuilder annotatedRoleBuilder;
    private final ResourceRoleDetector detector;
    private final SecurityProperties securityProperties;
    private final ApplicationEventPublisher eventPublisher;

    public ExtAnnotatedResourceRoleProvider(JmixModulesClasspathScanner classpathScanner,
                                            AnnotatedRoleBuilder annotatedRoleBuilder,
                                            ResourceRoleDetector detector,
                                            SecurityProperties securityProperties,
                                            ApplicationEventPublisher eventPublisher) {
        // super() sẽ build cache gốc, nhưng ta sẽ build lại ngay sau
        super(classpathScanner, annotatedRoleBuilder, detector);
        this.classpathScanner = classpathScanner;
        this.annotatedRoleBuilder = annotatedRoleBuilder;
        this.detector = detector;
        this.securityProperties = securityProperties;
        this.eventPublisher = eventPublisher;

        // build lại cache, có “forUser”
        buildRolesCacheExt();
    }

    @Override
    public Collection<ResourceRole> getAllRoles() {
        // đảm bảo luôn trả về bản đã bơm custom properties
        return super.getAllRoles();
    }

    @Override
    public ResourceRole findRoleByCode(String code) {
        return super.findRoleByCode(code);
    }

    @Override
    public void refreshRoles() {
        if (securityProperties.isAnnotatedRolesHotDeployEnabled()) {
            classpathScanner.refreshClassNames(detector);
            buildRolesCacheExt();
            eventPublisher.publishEvent(new ResourceRoleModifiedEvent(this));
        } else {
            throw new DevelopmentException("Annotated roles hot deploy is forbidden");
        }
    }

    @SuppressWarnings("unchecked")
    private void buildRolesCacheExt() {
        // quét các class annotated role như base
        Set<String> classNames = classpathScanner.getClassNames(ResourceRoleDetector.class);

        Map<String, ResourceRole> map = new LinkedHashMap<>();
        for (String className : classNames) {
            // tạo ResourceRole từ class name (giống base)
            ResourceRole role = annotatedRoleBuilder.createResourceRole(className);

            // nạp class để đọc @ForUser
            try {
                Class<?> roleClass = Class.forName(className);
                ForUser mark = roleClass.getAnnotation(ForUser.class);
                if (mark != null) {
                    Map<String, String> props = role.getCustomProperties();
                    if (props == null) props = new HashMap<>();
                    props.put("forUser", String.valueOf(mark.value()));
                    // Nếu là annotated role (không đến từ DB) và thiếu/blank databaseId → gán UUID ổn định theo code
                    String dbId = props.get("databaseId");
                    if (dbId == null || dbId.isBlank()) {
                        String stable = UUID.nameUUIDFromBytes(role.getCode().getBytes(StandardCharsets.UTF_8)).toString();
                        props.put("databaseId", stable);
                    }
                    role.setCustomProperties(props);
                }
            } catch (ClassNotFoundException ignored) {
            }

            map.put(((BaseRole) role).getCode(), role);
        }

        this.roles = map;
    }
}