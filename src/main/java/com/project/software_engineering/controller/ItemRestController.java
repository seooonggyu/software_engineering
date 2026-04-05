package com.project.software_engineering.controller;

import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import com.project.software_engineering.dto.UserDto;
import com.project.software_engineering.security.PrincipalDetails;
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

    public Long getReqUserId(PrincipalDetails principalDetails){
        if(principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null){
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
//    @PreAuthorize("permitAll()")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(@RequestPart(value = "params") ItemDto.CreateReqDto params, @RequestPart(value = "files", required = false) List<MultipartFile> images, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(itemService.create(params, images, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<ItemDto.DetailResDto> detail(DefaultDto.DetailReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(itemService.detail(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/list")
    public ResponseEntity<List<ItemDto.DetailResDto>> list(ItemDto.ListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(itemService.list(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(@RequestBody ItemDto.UpdateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        itemService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(@RequestBody DefaultDto.DeleteReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        itemService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

//    final UserService userService;
//    final ExternalProperties externalProperties;
//
//    public Long getReqUserId(PrincipalDetails principalDetails){
//        if(principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null){
//            return null;
//        }
//        return principalDetails.getUser().getId();
//    }
//
//    @PreAuthorize("permitAll()")
//    @PostMapping("/signup")
//    public ResponseEntity<DefaultDto.CreateResDto> signup(@RequestBody UserDto.CreateReqDto params){
//        return ResponseEntity.ok(userService.signup(params, null));
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("")
//    public ResponseEntity<DefaultDto.CreateResDto> create(@RequestBody UserDto.CreateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
//        Long reqUserId = getReqUserId(principalDetails);
//        return ResponseEntity.ok(userService.create(params, reqUserId));
//    }
//
//    @PreAuthorize("hasRole('USER')")
//    @PutMapping("")
//    public ResponseEntity<Void> update(@RequestBody UserDto.UpdateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
//        Long reqUserId = getReqUserId(principalDetails);
//        userService.update(params, reqUserId);
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }
//
//    @PreAuthorize("hasRole('USER')")
//    @DeleteMapping("")
//    public ResponseEntity<Void> delete(@RequestBody DefaultDto.DeleteReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
//        Long reqUserId = getReqUserId(principalDetails);
//        userService.delete(params, reqUserId);
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }
//
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("")
//    public ResponseEntity<UserDto.DetailResDto> detail(DefaultDto.DetailReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
//        Long reqUserId = getReqUserId(principalDetails);
//        return ResponseEntity.ok(userService.detail(params, reqUserId));
//    }
//
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/list")
//    public ResponseEntity<List<UserDto.DetailResDto>> list(UserDto.ListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
//        Long reqUserId = getReqUserId(principalDetails);
//        return ResponseEntity.ok(userService.list(params, reqUserId));
//    }
//
//    /*
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/pagedList")
//    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(UserDto.PagedListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
//        Long reqUserId = getReqUserId(principalDetails);
//        return ResponseEntity.ok(userService.pagedList(params, reqUserId));
//    }
//    */
//    /*
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/scrollList")
//    public ResponseEntity<List<UserDto.DetailResDto>> scrollList(UserDto.ScrollListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
//        Long reqUserId = getReqUserId(principalDetails);
//        return ResponseEntity.ok(userService.scrollList(params, reqUserId));
//    }


}
