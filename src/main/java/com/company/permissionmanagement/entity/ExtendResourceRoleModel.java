package com.company.permissionmanagement.entity;

import io.jmix.core.entity.annotation.ReplaceEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.security.model.ResourceRoleModel;

@JmixEntity
@ReplaceEntity(ResourceRoleModel.class)
public class ExtendResourceRoleModel extends ResourceRoleModel {

    private Boolean forUser;

    public Boolean getForUser() {
        return forUser;
    }

    public void setForUser(Boolean isForUser) {
        this.forUser = isForUser;
    }

}