package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.Permission;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDetailDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.PermissionMapper;
import com.project.software_engineering.repository.PermissionRepository;
import com.project.software_engineering.service.PermissionDetailService;
import com.project.software_engineering.service.PermissionService;
import com.project.software_engineering.service.PermittedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PermissionServiceimpl implements PermissionService {

    private final String target = "permission";

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final PermissionDetailService permissionDetailService;
    private final PermittedService permittedService;

    // 권한 생성
    @Override
    public DefaultDto.CreateResDto create(PermissionDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);
        return permissionRepository.save(param.toEntity()).toCreateResDto();
    }

    // 권한 수정
    @Override
    public void update(PermissionDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        Permission permission = permissionRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        permission.update(param);
        permissionRepository.save(permission);
    }

    // 권한 삭제 (소프트 딜리트)
    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(PermissionDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for (Long id : param.getIds()) {
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    // 권한 단건 조회 (상세 목록 + 타겟 목록 포함)
    public PermissionDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);
        PermissionDto.DetailResDto res = permissionMapper.detail(param.getId());
        res.setDetails(
                permissionDetailService.list(PermissionDetailDto.ListReqDto.builder().deleted(false).permissionId(res.getId()).build(), reqUserId)
        );
        res.setTargets(PermissionDto.targets);
        return res;
    }

    @Override
    public PermissionDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public List<PermissionDto.DetailResDto> list(PermissionDto.ListReqDto param, Long reqUserId) {
        return detailList(permissionMapper.list(param), reqUserId);
    }

    private List<PermissionDto.DetailResDto> detailList(List<PermissionDto.DetailResDto> list, Long reqUserId) {
        List<PermissionDto.DetailResDto> newList = new ArrayList<>();
        for (PermissionDto.DetailResDto each : list) {
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public DefaultDto.PagedListResDto pagedList(PermissionDto.PagedListReqDto param, Long reqUserId) {
        DefaultDto.PagedListResDto res = param.init(permissionMapper.pagedListCount(param));
        res.setList(detailList(permissionMapper.pagedList(param), reqUserId));
        return res;
    }

    @Override
    public List<PermissionDto.DetailResDto> scrollList(PermissionDto.ScrollListReqDto param, Long reqUserId) {
        param.init();
        return detailList(permissionMapper.scrollList(param), reqUserId);
    }
}
