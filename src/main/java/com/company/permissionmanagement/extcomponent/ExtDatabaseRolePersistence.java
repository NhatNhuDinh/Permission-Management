package com.company.permissionmanagement.extcomponent;

import com.company.permissionmanagement.entity.ExtResourceRoleEntity;
import com.company.permissionmanagement.entity.ExtResourceRoleModel;
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

public class ExtDatabaseRolePersistence extends DatabaseRolePersistence {

    private final DataManager dataManager;

    public ExtDatabaseRolePersistence(ApplicationContext applicationContext,
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
        // Lưu phần chuẩn (có thể dùng lại logic cha)
        super.save(roleModel);

        if (!(roleModel instanceof ExtResourceRoleModel ext)) {
            return;
        }

        ExtResourceRoleEntity entity = null;

        // Lấy đúng entity thật từ DB theo databaseId hoặc code
        UUID dbId = parseUUID(roleModel.getCustomProperties().get("databaseId"));
        if (dbId != null) {
            entity = dataManager.load(ExtResourceRoleEntity.class)
                    .id(dbId)
                    .optional().orElse(null);
        }
        if (entity == null && roleModel.getId() != null) {
            entity = dataManager.load(ExtResourceRoleEntity.class)
                    .id(roleModel.getId())
                    .optional().orElse(null);
        }
        if (entity == null) {
            entity = dataManager.load(ExtResourceRoleEntity.class)
                    .query("select e from ExtResourceRoleEntity e where e.code = :code")
                    .parameter("code", roleModel.getCode())
                    .optional().orElse(null);
        }

        // Nếu không có thì tạo mới (trường hợp tạo mới hoàn toàn)
        if (entity == null) {
            entity = dataManager.create(ExtResourceRoleEntity.class);
            entity.setId(roleModel.getId()); // Đặt id nếu bạn muốn đồng bộ với model
        }

        // Gán thuộc tính
        entity.setForUser(Boolean.TRUE.equals(ext.getForUser()));

        // Lưu
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
                    if (model instanceof ExtResourceRoleModel) {
                        ExtResourceRoleEntity entity = null;
                        UUID dbId = parseUUID(model.getCustomProperties().get("databaseId"));
                        if (dbId != null) {
                            entity = dataManager.load(ExtResourceRoleEntity.class)
                                    .id(dbId)
                                    .optional().orElse(null);
                        }
                        if (entity == null) {
                            entity = dataManager.load(ExtResourceRoleEntity.class)
                                    .query("select e from ExtResourceRoleEntity e where e.code = :code")
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

        // Xóa vật lý
        dataManager.remove(entitiesToRemove);
    }

}
