package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/permission")
@RestController
public class PermissionRestController {

    private final PermissionService permissionService;

    // 요청 유저 ID 추출
    private Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    // 권한 생성
    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestBody PermissionDto.CreateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionService.create(params, getReqUserId(principalDetails)));
    }

    // 권한 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody PermissionDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionService.update(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionService.delete(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 다건 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(
            @RequestBody DefaultDto.DeleteListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionService.deleteList(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 단건 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<PermissionDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionService.detail(params, getReqUserId(principalDetails)));
    }

    // 권한 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<PermissionDto.DetailResDto>> list(
            PermissionDto.ListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionService.list(params, getReqUserId(principalDetails)));
    }

    // 권한 페이지 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(
            PermissionDto.PagedListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionService.pagedList(params, getReqUserId(principalDetails)));
    }

    // 권한 스크롤 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/scrollList")
    public ResponseEntity<List<PermissionDto.DetailResDto>> scrollList(
            PermissionDto.ScrollListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionService.scrollList(params, getReqUserId(principalDetails)));
    }
}
