package com.company.permissionmanagement.extcomponent;


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
        // Lưu phần chuẩn (code, name, policies, childRoles, …)
        super.save(roleModel);

        // Gài thêm forUser
        if (!(roleModel instanceof ExtResourceRoleModel ext)) {
            return; // màn hình của bạn đang dùng model mở rộng; nếu không phải thì bỏ qua
        }

        // 1) Ưu tiên lấy entity theo databaseId do Jmix gắn vào customProperties sau khi save
        ExtResourceRoleEntity entity = null;
        UUID dbId = parseUUID(roleModel.getCustomProperties().get("databaseId"));
        if (dbId != null) {
            entity = dataManager.load(ExtResourceRoleEntity.class)
                    .id(dbId)
                    .optional().orElse(null);
        }

        // 2) Fallback theo code (khi vừa tạo mới mà databaseId chưa sẵn sàng)
        if (entity == null) {
            entity = dataManager.load(ExtResourceRoleEntity.class)
                    .query("select e from ExtResourceRoleEntity e where e.code = :code")
                    .parameter("code", roleModel.getCode())
                    .optional().orElse(null);
        }

        // 3) Cập nhật cờ forUser và lưu
        if (entity != null) {
            entity.setForUser(Boolean.TRUE.equals(ext.getForUser()));
            dataManager.save(entity);
        }
    }

    private UUID parseUUID(Object o) {
        try {
            return o == null ? null : UUID.fromString(String.valueOf(o));
        } catch (Exception ignored) {
            return null;
        }
    }
}
