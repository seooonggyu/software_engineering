package com.project.software_engineering.controller;

import com.project.software_engineering.dto.AIDto;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.AIService;
import com.project.software_engineering.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/item")
@RestController
public class ItemRestController {

    private final ItemService itemService;
    private final AIService aiService;

    // 요청 유저 ID 추출
    private Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    // 아이템 등록 (인증 없이도 접근 가능)
    @PreAuthorize("permitAll()")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestPart(value = "params") ItemDto.CreateReqDto params,
            @RequestPart(value = "files", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long reqUserId = getReqUserId(principalDetails);
        if (reqUserId == null) reqUserId = -200L; // 비인증 접근 시 권한 우회 더미 ID
        return ResponseEntity.ok(itemService.create(params, images, reqUserId));
    }

    // 아이템 단건 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<ItemDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(itemService.detail(params, getReqUserId(principalDetails)));
    }

    // 아이템 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<ItemDto.DetailResDto>> list(
            ItemDto.ListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(itemService.list(params, getReqUserId(principalDetails)));
    }

    // 아이템 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody ItemDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        itemService.update(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 아이템 삭제 (소프트 딜리트)
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        itemService.delete(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // AI 서버에서 분실물 매칭 결과 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}/matches")
    public ResponseEntity<AIDto.LostItemRegisterResDto> getMatches(@PathVariable Long id) {
        return ResponseEntity.ok(aiService.getMatchesForLostItem(id));
    }
}
