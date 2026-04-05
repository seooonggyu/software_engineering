package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.PermissionService;
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
@RequestMapping("/api/permission")
@RestController
public class PermissionRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PermissionService permissionService;

    public Long getReqUserId(PrincipalDetails principalDetails){
        if(principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null){
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(@RequestBody PermissionDto.CreateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(@RequestBody PermissionDto.UpdateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(@RequestBody DefaultDto.DeleteReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(@RequestBody DefaultDto.DeleteListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionService.deleteList(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

//    @PreAuthorize("permitAll()")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<PermissionDto.DetailResDto> detail(DefaultDto.DetailReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionService.detail(params, reqUserId));
    }

//    @PreAuthorize("permitAll()")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<PermissionDto.DetailResDto>> list(PermissionDto.ListReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionService.list(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(PermissionDto.PagedListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        logger.info("reqUserId : " + reqUserId);
        return ResponseEntity.ok(permissionService.pagedList(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/scrollList")
    public ResponseEntity<List<PermissionDto.DetailResDto>> scrollList(PermissionDto.ScrollListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionService.scrollList(params, reqUserId));
    }
}
