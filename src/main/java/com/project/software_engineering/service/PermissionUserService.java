package com.project.software_engineering.service;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionUserDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PermissionUserService {
    DefaultDto.CreateResDto create(PermissionUserDto.CreateReqDto param, Long reqUserId);
    void update(PermissionUserDto.UpdateReqDto param, Long reqUserId);
    void delete(DefaultDto.DeleteReqDto param, Long reqUserId);
    void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId);
    PermissionUserDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId);
    List<PermissionUserDto.DetailResDto> list(PermissionUserDto.ListReqDto param, Long reqUserId);
    DefaultDto.PagedListResDto pagedList(PermissionUserDto.PagedListReqDto param, Long reqUserId);
    List<PermissionUserDto.DetailResDto> scrollList(PermissionUserDto.ScrollListReqDto param, Long reqUserId);
}
