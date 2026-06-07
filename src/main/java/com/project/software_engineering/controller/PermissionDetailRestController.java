package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDetailDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.PermissionDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/permissionDetail")
@RestController
public class PermissionDetailRestController {

    private final PermissionDetailService permissionDetailService;

    // 요청 유저 ID 추출
    private Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    // 특정 타겟에 대한 접근 허용 여부 확인
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/allow")
    public ResponseEntity<PermissionDetailDto.AllowResDto> allow(
            @RequestBody PermissionDetailDto.AllowReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.allow(
                PermissionDto.ExistReqDto.builder().userId(reqUserId).target(params.getTarget()).func(200).build(), reqUserId));
    }

    // 현재 유저의 접근 가능한 권한 상세 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/access")
    public ResponseEntity<List<PermissionDetailDto.DetailResDto>> access(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.access(
                PermissionDto.ExistReqDto.builder().userId(reqUserId).func(200).build(), reqUserId));
    }

    // 권한 상세 토글 (활성/비활성)
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/toggle")
    public ResponseEntity<DefaultDto.CreateResDto> toggle(
            @RequestBody PermissionDetailDto.ToggleReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionDetailService.toggle(params, getReqUserId(principalDetails)));
    }

    // 권한 상세 생성
    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestBody PermissionDetailDto.CreateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionDetailService.create(params, getReqUserId(principalDetails)));
    }

    // 권한 상세 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody PermissionDetailDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionDetailService.update(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 상세 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionDetailService.delete(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 상세 다건 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(
            @RequestBody DefaultDto.DeleteListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionDetailService.deleteList(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 상세 단건 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<PermissionDetailDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionDetailService.detail(params, getReqUserId(principalDetails)));
    }

    // 권한 상세 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<PermissionDetailDto.DetailResDto>> list(
            PermissionDetailDto.ListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionDetailService.list(params, getReqUserId(principalDetails)));
    }

    // 권한 상세 페이지 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(
            PermissionDetailDto.PagedListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionDetailService.pagedList(params, getReqUserId(principalDetails)));
    }

    // 권한 상세 스크롤 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/scrollList")
    public ResponseEntity<List<PermissionDetailDto.DetailResDto>> scrollList(
            PermissionDetailDto.ScrollListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionDetailService.scrollList(params, getReqUserId(principalDetails)));
    }
}
