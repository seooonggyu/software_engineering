package com.project.software_engineering.dto;

import com.project.software_engineering.domain.PermissionUser;
import lombok.*;
import lombok.experimental.SuperBuilder;

public class PermissionUserDto {

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateReqDto {
        Long permissionId;
        Long userId;
        String username;

        public PermissionUser toEntity(){
            return PermissionUser.of(getPermissionId(), getUserId());
        }
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto{
        Long userId;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResDto extends DefaultDto.DetailResDto{
        Long permissionId;
        Long userId;

        String userUsername;
    }

    /**/

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ListReqDto extends DefaultDto.ListReqDto{
        Long permissionId;
        Long userId;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class PagedListReqDto extends DefaultDto.PagedListReqDto{
        Long permissionId;
        Long userId;
    }
    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ScrollListReqDto extends DefaultDto.ScrollListReqDto{
        Long permissionId;
        Long userId;
    }
}
