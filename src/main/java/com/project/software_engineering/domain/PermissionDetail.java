package com.project.software_engineering.domain;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDetailDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(
        indexes = {
                @Index(columnList = "deleted")
                ,@Index(columnList = "permissionId")
                ,@Index(columnList = "target")
                ,@Index(columnList = "func")
        }
        ,uniqueConstraints = {@UniqueConstraint(
        name = "UQ_permissionDetail_permissionId_target_func"
        ,columnNames = {"permissionId", "target", "func"}
        )}
)
@Entity
public class PermissionDetail extends AuditingFields{
    Long permissionId;
    String target; // 어떤 테이블에 해당하는지! user notice
    Integer func; // 어떤 기능을 할지 create 110 read 200 update 120

    protected PermissionDetail(){}
    private PermissionDetail(Long permissionId, String target, Integer func){
        this.permissionId = permissionId;
        this.target = target;
        this.func = func;
    }
    public static PermissionDetail of(Long permissionId, String target, Integer func){
        return new PermissionDetail(permissionId, target, func);
    }
    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(PermissionDetailDto.UpdateReqDto param) {
        if(param.getDeleted() != null){ setDeleted(param.getDeleted()); }
        if(param.getTarget() != null){ setTarget(param.getTarget()); }
        if(param.getFunc() != null){ setFunc(param.getFunc()); }
    }

}
