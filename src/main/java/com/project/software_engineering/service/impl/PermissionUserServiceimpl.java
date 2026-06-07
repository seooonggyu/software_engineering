package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.PermissionUser;
import com.project.software_engineering.domain.User;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionUserDto;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.PermissionUserMapper;
import com.project.software_engineering.repository.PermissionUserRepository;
import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.service.PermissionUserService;
import com.project.software_engineering.service.PermittedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PermissionUserServiceimpl implements PermissionUserService {

    private final String target = "permission";

    private final PermissionUserRepository permissionUserRepository;
    private final UserRepository userRepository;
    private final PermissionUserMapper permissionUserMapper;
    private final PermittedService permittedService;

    // 권한 유저 생성 (username 또는 ID로 유저 조회 후 등록)
    @Override
    public DefaultDto.CreateResDto create(PermissionUserDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);

        User user = userRepository.findByUsername(param.getUsername());
        if (user == null) {
            user = userRepository.findById(Long.parseLong(param.getUsername())).orElse(null);
        }
        if (user == null) {
            throw new RuntimeException("no user");
        }
        param.setUserId(user.getId());

        // 이미 존재하는 데이터면 deleted 상태만 복구
        PermissionUser permissionUser = permissionUserRepository.findByPermissionIdAndUserId(param.getPermissionId(), param.getUserId());
        if (permissionUser == null) {
            permissionUser = param.toEntity();
        } else {
            permissionUser.setDeleted(false);
        }
        return permissionUserRepository.save(permissionUser).toCreateResDto();
    }

    // 권한 유저 수정
    @Override
    public void update(PermissionUserDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        PermissionUser permissionUser = permissionUserRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        permissionUser.update(param);
        permissionUserRepository.save(permissionUser);
    }

    // 권한 유저 삭제 (소프트 딜리트)
    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(PermissionUserDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for (Long id : param.getIds()) {
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    // 권한 유저 단건 조회
    public PermissionUserDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);
        return permissionUserMapper.detail(param.getId());
    }

    @Override
    public PermissionUserDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public List<PermissionUserDto.DetailResDto> list(PermissionUserDto.ListReqDto param, Long reqUserId) {
        return detailList(permissionUserMapper.list(param), reqUserId);
    }

    private List<PermissionUserDto.DetailResDto> detailList(List<PermissionUserDto.DetailResDto> list, Long reqUserId) {
        List<PermissionUserDto.DetailResDto> newList = new ArrayList<>();
        for (PermissionUserDto.DetailResDto each : list) {
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public DefaultDto.PagedListResDto pagedList(PermissionUserDto.PagedListReqDto param, Long reqUserId) {
        DefaultDto.PagedListResDto res = param.init(permissionUserMapper.pagedListCount(param));
        res.setList(detailList(permissionUserMapper.pagedList(param), reqUserId));
        return res;
    }

    @Override
    public List<PermissionUserDto.DetailResDto> scrollList(PermissionUserDto.ScrollListReqDto param, Long reqUserId) {
        param.init();
        return detailList(permissionUserMapper.scrollList(param), reqUserId);
    }
}
