package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionUserDto;
import com.project.software_engineering.security.PrincipalDetails;
import com.project.software_engineering.service.PermissionUserService;
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
@RequestMapping("/api/permissionUser")
@RestController
public class PermissionUserRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PermissionUserService permissionUserService;

    public Long getReqUserId(PrincipalDetails principalDetails){
        if(principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null){
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(@RequestBody PermissionUserDto.CreateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionUserService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(@RequestBody PermissionUserDto.UpdateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionUserService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(@RequestBody DefaultDto.DeleteReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionUserService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(@RequestBody DefaultDto.DeleteListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        permissionUserService.deleteList(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //@PreAuthorize("permitAll()")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<PermissionUserDto.DetailResDto> detail(DefaultDto.DetailReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionUserService.detail(params, reqUserId));
    }

    /**/

    //@PreAuthorize("permitAll()")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<PermissionUserDto.DetailResDto>> list(PermissionUserDto.ListReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionUserService.list(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(PermissionUserDto.PagedListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionUserService.pagedList(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/scrollList")
    public ResponseEntity<List<PermissionUserDto.DetailResDto>> scrollList(PermissionUserDto.ScrollListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(permissionUserService.scrollList(params, reqUserId));
    }

}
