package com.project.software_engineering.domain;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(indexes = {@Index(columnList = "deleted")})
@Entity
public class Permission extends AuditingFields{
    String title;
    String content;

    protected Permission(){}
    private Permission(String title, String content){
        this.title = title;
        this.content = content;
    }
    public static Permission of(String title, String content){
        return new Permission(title, content);
    }
    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(PermissionDto.UpdateReqDto param) {
        if(param.getDeleted() != null){ setDeleted(param.getDeleted()); }
        if(param.getTitle() != null){ setTitle(param.getTitle()); }
        if(param.getContent() != null){ setContent(param.getContent()); }
    }
}
