package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.UserDto;
import com.project.software_engineering.security.ExternalProperties;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserRestController {

    final UserService userService;
    final ExternalProperties externalProperties;

    // 요청 유저 ID 추출
    private Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    // 회원가입
    @PreAuthorize("permitAll()")
    @PostMapping("/signup")
    public ResponseEntity<DefaultDto.CreateResDto> signup(@RequestBody UserDto.CreateReqDto params) {
        return ResponseEntity.ok(userService.signup(params, null));
    }

    // 관리자 유저 생성
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestBody UserDto.CreateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(userService.create(params, getReqUserId(principalDetails)));
    }

    // 유저 정보 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody UserDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        userService.update(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 유저 삭제 (소프트 딜리트)
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        userService.delete(params, getReqUserId(principalDetails));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 유저 단건 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<UserDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(userService.detail(params, getReqUserId(principalDetails)));
    }

    // 유저 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<UserDto.DetailResDto>> list(
            UserDto.ListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(userService.list(params, getReqUserId(principalDetails)));
    }
}
