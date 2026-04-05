package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.dto.PermissionDetailDto;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.PermissionDetailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PermissionDetailService permissionDetailService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/allow")
    public ResponseEntity<PermissionDetailDto.AllowResDto> allow(@RequestBody PermissionDetailDto.AllowReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.allow(PermissionDto.ExistReqDto.builder().userId(reqUserId).target(params.getTarget()).func(200).build(), reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/access")
    public ResponseEntity<List<PermissionDetailDto.DetailResDto>> access(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.access(PermissionDto.ExistReqDto.builder().userId(reqUserId).func(200).build(), reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/toggle")
    public ResponseEntity<DefaultDto.CreateResDto> toggle(@RequestBody PermissionDetailDto.ToggleReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.toggle(params, reqUserId));
    }

    /**/

    public Long getReqUserId(PrincipalDetails principalDetails){
        if(principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null){
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(@RequestBody PermissionDetailDto.CreateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(@RequestBody PermissionDetailDto.UpdateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionDetailService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(@RequestBody DefaultDto.DeleteReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionDetailService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(@RequestBody DefaultDto.DeleteListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionDetailService.deleteList(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //@PreAuthorize("permitAll()")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<PermissionDetailDto.DetailResDto> detail(DefaultDto.DetailReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.detail(params, reqUserId));
    }

    //@PreAuthorize("permitAll()")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<PermissionDetailDto.DetailResDto>> list(PermissionDetailDto.ListReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.list(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(PermissionDetailDto.PagedListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.pagedList(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/scrollList")
    public ResponseEntity<List<PermissionDetailDto.DetailResDto>> scrollList(PermissionDetailDto.ScrollListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionDetailService.scrollList(params, reqUserId));
    }
}
