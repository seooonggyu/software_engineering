package com.project.software_engineering.dto;

import com.project.software_engineering.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

public class UserDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginReqDto {
        String username;
        String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateReqDto {
        String username;
        String password;
        String name;

        Integer rfrom;

        public User toEntity(){
            return User.of(getUsername(), getPassword(), getName(), getRfrom());
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto {
        String password;
        String name;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
    public static class DetailResDto extends DefaultDto.DetailResDto {
        String username;
        String name;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
    public static class ListReqDto extends DefaultDto.ListReqDto {
        String name;
    }
}
