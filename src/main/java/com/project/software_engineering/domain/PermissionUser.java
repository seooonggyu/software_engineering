package com.project.software_engineering.domain;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionUserDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(
        indexes = {
                @Index(columnList = "deleted")
                ,@Index(columnList = "permissionId")
                ,@Index(columnList = "userId")
        }
        ,uniqueConstraints = {@UniqueConstraint(
        name = "UQ_permissionUser_permissionId_userId"
        ,columnNames = {"permissionId", "userId"}
        )}
)
@Entity
public class PermissionUser extends AuditingFields{
    Long permissionId;
    Long userId;

    protected PermissionUser(){}
    private PermissionUser(Long permissionId, Long userId){
        this.permissionId = permissionId;
        this.userId = userId;
    }
    public static PermissionUser of(Long permissionId, Long userId){
        return new PermissionUser(permissionId, userId);
    }
    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(PermissionUserDto.UpdateReqDto param) {
        if(param.getDeleted() != null){ setDeleted(param.getDeleted()); }
        if(param.getUserId() != null){ setUserId(param.getUserId()); }
    }

}
