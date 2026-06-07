package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.Images;
import com.project.software_engineering.domain.Item;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.ItemMapper;
import com.project.software_engineering.repository.ItemRepository;
import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.service.AIService;
import com.project.software_engineering.service.ItemService;
import com.project.software_engineering.service.PermittedService;
import com.project.software_engineering.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final String target = "item";

    private final PermittedService permittedService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final AIService aiService;
    private final S3Service s3Service;

    // 아이템 등록: 이미지 S3 업로드 후 AI 서버 비동기 전송
    @Override
    public DefaultDto.CreateResDto create(ItemDto.CreateReqDto param, List<MultipartFile> images, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);

        Item item = param.toEntity();
        item.setUserId(reqUserId);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String s3Url = s3Service.uploadFile(file);
                    item.addImage(new Images(s3Url));
                }
            }
        }

        item = itemRepository.save(item);

        // 이미지가 있는 경우 AI 서버에 비동기로 등록 요청
        if (!item.getImages().isEmpty()) {
            List<String> imagePaths = item.getImages().stream()
                    .map(Images::getImageUrl)
                    .toList();
            aiService.registerItemToAIAsync(item, imagePaths);
        }

        return item.toCreateResDto();
    }

    // 아이템 단건 상세 조회 (이미지 URL, 유저명 포함)
    @Transactional
    public ItemDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);
        ItemDto.DetailResDto res = itemMapper.detail(param.getId());
        itemRepository.findById(param.getId()).ifPresent(item -> {
            List<String> urls = item.getImages().stream()
                    .map(Images::getImageUrl)
                    .collect(Collectors.toList());
            res.setImageUrls(urls);

            if (item.getUserId() != null) {
                userRepository.findById(item.getUserId())
                        .ifPresent(user -> {
                            res.setUserUsername(user.getUsername());
                            res.setUserName(user.getName());
                        });
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

    private List<ItemDto.DetailResDto> detailList(List<ItemDto.DetailResDto> list, Long reqUserId) {
        List<ItemDto.DetailResDto> newList = new ArrayList<>();
        for (ItemDto.DetailResDto each : list) {
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    // 아이템 수정
    @Override
    public void update(ItemDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        Item item = itemRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if (param.getDeleted() != null) { item.setDeleted(param.getDeleted()); }
        if (param.getTitle() != null) { item.setTitle(param.getTitle()); }
        if (param.getContent() != null) { item.setContent(param.getContent()); }
        if (param.getStartTime() != null) { item.setStartTime(param.getStartTime()); }
        if (param.getEndTime() != null) { item.setEndTime(param.getEndTime()); }
        if (param.getCategory() != null) { item.setCategory(param.getCategory()); }
        if (param.getStatus() != null) { item.setStatus(param.getStatus()); }
        if (param.getLocation() != null) { item.setLocation(param.getLocation()); }
        itemRepository.save(item);
    }

    // 아이템 삭제 (소프트 딜리트)
    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(ItemDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for (Long id : param.getIds()) {
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }
}
