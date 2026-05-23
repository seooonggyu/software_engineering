package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.Images;
import com.project.software_engineering.domain.Item;
import com.project.software_engineering.domain.Location;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import jakarta.transaction.Transactional;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.ItemMapper;
import com.project.software_engineering.repository.ItemRepository;
import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.service.ItemService;
import com.project.software_engineering.service.PermittedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final String target = "item";

    private final PermittedService permittedService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public DefaultDto.CreateResDto create(ItemDto.CreateReqDto param, List<MultipartFile> images, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);

        Item item = param.toEntity();
        item.setUserId(reqUserId);

        String projectPath = System.getProperty("user.dir");
        String uploadDir = projectPath + "/src/main/resources/static/images/";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        // 원본 파일명 추출
                        String originalFilename = file.getOriginalFilename();

                        // 파일명 중복을 막기 위해 UUID 생성 (예: 123e4567-e89b-12d3_myphoto.jpg)
                        String uuid = UUID.randomUUID().toString();
                        String savedFileName = uuid + "_" + originalFilename;

                        // 실제 폴더에 파일 저장 (Transfer)
                        File saveFile = new File(uploadDir, savedFileName);
                        file.transferTo(saveFile);

                        // Images 엔티티 생성 (DB에는 브라우저에서 접근할 URL 경로로 저장)
                        // src/main/resources/static 에 있는 파일은 도메인 바로 뒤부터 접근 가능하므로 "/images/파일명" 형태로 저장
                        Images imageEntity = new Images("/images/" + savedFileName);

                        // Item 엔티티에 만들어둔 편의 메서드로 연관관계 매핑
                        item.addImage(imageEntity);

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
                    }
                }
            }
        }

        return itemRepository.save(item).toCreateResDto();
    }

    @Transactional
    public ItemDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);
        ItemDto.DetailResDto res = itemMapper.detail(param.getId());
        itemRepository.findById(param.getId()).ifPresent(item -> {
            List<String> urls = item.getImages().stream()
                    .map(Images::getImageUrl)
                    .collect(java.util.stream.Collectors.toList());
            res.setImageUrls(urls);

            if (item.getUserId() != null) {
                userRepository.findById(item.getUserId())
                        .ifPresent(user -> res.setUserUsername(user.getUsername()));
            }
        });
        return res;
    }
    @Override
    public ItemDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public List<ItemDto.DetailResDto> list(ItemDto.ListReqDto param, Long reqUserId) {
        return detailList(itemMapper.list(param), reqUserId);
    }
    public List<ItemDto.DetailResDto> detailList(List<ItemDto.DetailResDto> list, Long reqUserId){
        List<ItemDto.DetailResDto> newList = new ArrayList<>();
        for(ItemDto.DetailResDto each : list){
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public void update(ItemDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        Item item = itemRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if(param.getDeleted() != null){ item.setDeleted(param.getDeleted()); }
        if(param.getTitle() != null){ item.setTitle(param.getTitle()); }
        if(param.getContent() != null){ item.setContent(param.getContent()); }
        if(param.getStartTime() != null){ item.setStartTime(param.getStartTime()); }
        if(param.getEndTime() != null){ item.setEndTime(param.getEndTime()); }
        if(param.getCategory() != null){ item.setCategory(param.getCategory()); }
        if(param.getStatus() != null){ item.setStatus(param.getStatus()); }
        if(param.getLocation() != null){ item.setLocation(param.getLocation()); }
        itemRepository.save(item);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(ItemDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }
    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for(Long id : param.getIds()){
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }
}
