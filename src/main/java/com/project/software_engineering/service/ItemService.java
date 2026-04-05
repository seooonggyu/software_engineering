package com.project.software_engineering.service;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ItemService {
    DefaultDto.CreateResDto create(ItemDto.CreateReqDto param, List<MultipartFile> images, Long reqUserId);
    void update(ItemDto.UpdateReqDto param, Long reqUserId);
    void delete(DefaultDto.DeleteReqDto param, Long reqUserId);
    void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId);
    ItemDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId);
    List<ItemDto.DetailResDto> list(ItemDto.ListReqDto param, Long reqUserId);
//    DefaultDto.PagedListResDto pagedList(ItemDto.PagedListReqDto param, Long reqUserId);
//    List<ItemDto.DetailResDto> scrollList(ItemDto.ScrollListReqDto param, Long reqUserId);
}
