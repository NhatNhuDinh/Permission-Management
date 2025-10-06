package com.company.permissionmanagement.extension;

import com.company.permissionmanagement.entity.ExtResourceRoleEntity;
import com.company.permissionmanagement.entity.ExtResourceRoleModel;
import io.jmix.core.*;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securitydata.impl.role.DatabaseRolePersistence;
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

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
        super.save(roleModel);

        if (!(roleModel instanceof ExtResourceRoleModel ext)) {
            return;
        }

        ExtResourceRoleEntity entity = null;
        // Lấy entity thật từ DB theo databaseId hoặc code
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

        // Nếu không có thì tạo mới
        if (entity == null) {
            entity = dataManager.create(ExtResourceRoleEntity.class);
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

}