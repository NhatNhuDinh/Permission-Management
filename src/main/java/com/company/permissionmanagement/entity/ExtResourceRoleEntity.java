package com.company.permissionmanagement.entity;

import io.jmix.core.entity.annotation.ReplaceEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@JmixEntity
@Entity
@ReplaceEntity(ResourceRoleEntity.class)
public class ExtResourceRoleEntity extends ResourceRoleEntity {
    @Column(name = "IS_FOR_USER")
    private Boolean forUser;

    public Boolean getForUser() {
        return forUser;
    }

    public void setForUser(Boolean forUser) {
        this.forUser = forUser;
    }

}