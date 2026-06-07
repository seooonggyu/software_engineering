package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionUserDto;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.PermissionUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/permissionUser")
@RestController
public class PermissionUserRestController {

    private final PermissionUserService permissionUserService;

    // 요청 유저 ID 추출
    private Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    // 권한 유저 생성
    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestBody PermissionUserDto.CreateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionUserService.create(params, getReqUserId(principalDetails)));
    }

    // 권한 유저 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody PermissionUserDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionUserService.update(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 유저 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionUserService.delete(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 유저 다건 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(
            @RequestBody DefaultDto.DeleteListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        permissionUserService.deleteList(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 권한 유저 단건 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<PermissionUserDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionUserService.detail(params, getReqUserId(principalDetails)));
    }

    // 권한 유저 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<PermissionUserDto.DetailResDto>> list(
            PermissionUserDto.ListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionUserService.list(params, getReqUserId(principalDetails)));
    }

    // 권한 유저 페이지 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(
            PermissionUserDto.PagedListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionUserService.pagedList(params, getReqUserId(principalDetails)));
    }

    // 권한 유저 스크롤 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/scrollList")
    public ResponseEntity<List<PermissionUserDto.DetailResDto>> scrollList(
            PermissionUserDto.ScrollListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(permissionUserService.scrollList(params, getReqUserId(principalDetails)));
    }
}
