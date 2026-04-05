package com.project.software_engineering.domain;

import com.project.software_engineering.dto.DefaultDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class User extends AuditingFields{
    @Column(unique = true)
    String username;
    @Column(nullable = false)
    String password;
    String name;

    Integer rfrom; // 1000 수동 가입, 2100 전화번호, 3100구글 3200애플 3300카카오 3400네이버 3500깃허브


    protected User(){}
    private User(Boolean deleted, String username, String password, String name, Integer rfrom){
        this.deleted = deleted;
        this.username = username;
        this.password = password;
        this.name = name;
        this.rfrom = rfrom;
    }
    public static User of(String username, String password, String name, Integer rfrom){
        return new User(false, username, password, name, rfrom);
    }
    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }
}
