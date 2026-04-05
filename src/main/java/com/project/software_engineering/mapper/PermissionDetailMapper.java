package com.project.software_engineering.mapper;

import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.dto.PermissionDetailDto;

import java.util.List;

public interface PermissionDetailMapper {
	List<PermissionDetailDto.DetailResDto> access(PermissionDto.ExistReqDto params);
	/**/
	PermissionDetailDto.DetailResDto detail(Long id);
	List<PermissionDetailDto.DetailResDto> list(PermissionDetailDto.ListReqDto param);

	List<PermissionDetailDto.DetailResDto> pagedList(PermissionDetailDto.PagedListReqDto param);
	int pagedListCount(PermissionDetailDto.PagedListReqDto param);
	List<PermissionDetailDto.DetailResDto> scrollList(PermissionDetailDto.ScrollListReqDto param);
}