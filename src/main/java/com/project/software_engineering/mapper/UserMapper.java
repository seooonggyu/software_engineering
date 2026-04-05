package com.project.software_engineering.mapper;

import com.project.software_engineering.dto.UserDto;

import java.util.List;

public interface UserMapper {
	UserDto.DetailResDto detail(Long id);
	List<UserDto.DetailResDto> list(UserDto.ListReqDto param);

//	List<UserDto.DetailResDto> pagedList(UserDto.PagedListReqDto param);
//	int pagedListCount(UserDto.PagedListReqDto param);
//	List<UserDto.DetailResDto> scrollList(UserDto.ScrollListReqDto param);


}