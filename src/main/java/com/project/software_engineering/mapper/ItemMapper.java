package com.project.software_engineering.mapper;

import com.project.software_engineering.dto.ItemDto;

import java.util.List;

public interface ItemMapper {
    ItemDto.DetailResDto detail(Long id);
    List<ItemDto.DetailResDto> list(ItemDto.ListReqDto param);
}
