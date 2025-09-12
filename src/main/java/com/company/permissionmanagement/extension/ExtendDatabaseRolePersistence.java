package com.company.permissionmanagement.extension;

import com.company.permissionmanagement.entity.ExtendResourceRoleEntity;
import com.company.permissionmanagement.entity.ExtendResourceRoleModel;
import io.jmix.core.*;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securitydata.impl.role.DatabaseRolePersistence;
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

public class ExtendDatabaseRolePersistence extends DatabaseRolePersistence {

    private final DataManager dataManager;

    public ExtendDatabaseRolePersistence(ApplicationContext applicationContext,
                                      Metadata metadata,
                                      EntityStates entityStates,
                                      DataManager dataManager,
                                      FetchPlans fetchPlans,
                                      EntityImportExport entityImportExport,
                                      EntityImportPlans entityImportPlans,
                                      QueryTransformerFactory queryTransformerFactory,
                                      DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider) {
        super(applicationContext, metadata, entityStates, dataManager, fetchPlans,
                entityImportExport, entityImportPlans, queryTransformerFactory, databaseRowLevelRoleProvider);
        this.dataManager = dataManager;
    }

    @Override
    public void save(ResourceRoleModel roleModel) {
        super.save(roleModel);

        if (!(roleModel instanceof ExtendResourceRoleModel ext)) {
            return;
        }

        ExtendResourceRoleEntity entity = null;
        // Lấy entity thật từ DB theo databaseId hoặc code
        UUID dbId = parseUUID(roleModel.getCustomProperties().get("databaseId"));
        if (dbId != null) {
            entity = dataManager.load(ExtendResourceRoleEntity.class)
                    .id(dbId)
                    .optional().orElse(null);
        }
        if (entity == null && roleModel.getId() != null) {
            entity = dataManager.load(ExtendResourceRoleEntity.class)
                    .id(roleModel.getId())
                    .optional().orElse(null);
        }
        if (entity == null) {
            entity = dataManager.load(ExtendResourceRoleEntity.class)
                    .query("select e from ExtendResourceRoleEntity e where e.code = :code")
                    .parameter("code", roleModel.getCode())
                    .optional().orElse(null);
        }

        // Nếu không có thì tạo mới
        if (entity == null) {
            entity = dataManager.create(ExtendResourceRoleEntity.class);
            entity.setId(roleModel.getId()); // Đặt id để đồng bộ với model
        }

        // Gán thuộc tính
        entity.setForUser(Boolean.TRUE.equals(ext.getForUser()));
        dataManager.save(entity);
    }


    private UUID parseUUID(Object o) {
        try {
            return o == null ? null : UUID.fromString(String.valueOf(o));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void removeRoles(Collection<? extends BaseRoleModel> roleModels) {
        List<Object> entitiesToRemove = roleModels.stream()
                .map(model -> {
                    if (model instanceof ExtendResourceRoleModel) {
                        ExtendResourceRoleEntity entity = null;
                        UUID dbId = parseUUID(model.getCustomProperties().get("databaseId"));
                        if (dbId != null) {
                            entity = dataManager.load(ExtendResourceRoleEntity.class)
                                    .id(dbId)
                                    .optional().orElse(null);
                        }
                        if (entity == null) {
                            entity = dataManager.load(ExtendResourceRoleEntity.class)
                                    .query("select e from ExtendResourceRoleEntity e where e.code = :code")
                                    .parameter("code", model.getCode())
                                    .optional().orElse(null);
                        }
                        return entity;
                    } else if (model instanceof ResourceRoleModel) {
                        UUID dbId = model.getId();
                        if (dbId != null) {
                            return dataManager.load(ResourceRoleEntity.class)
                                    .id(dbId)
                                    .optional().orElse(null);
                        }
                        return null;
                    } else if (model instanceof RowLevelRoleModel) {
                        UUID dbId = model.getId();
                        if (dbId != null) {
                            return dataManager.load(RowLevelRoleEntity.class)
                                    .id(dbId)
                                    .optional().orElse(null);
                        }
                        return null;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        // Xóa các assignment
        List<RoleAssignmentEntity> roleAssignments = dataManager.load(RoleAssignmentEntity.class)
                .query("e.roleCode IN :codes")
                .parameter("codes", roleModels.stream()
                        .map(BaseRoleModel::getCode)
                        .collect(Collectors.toList()))
                .list();
        entitiesToRemove.addAll(roleAssignments);

        dataManager.remove(entitiesToRemove);
    }

}