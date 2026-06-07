package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.PermissionDetail;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDetailDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.PermissionDetailMapper;
import com.project.software_engineering.repository.PermissionDetailRepository;
import com.project.software_engineering.service.PermissionDetailService;
import com.project.software_engineering.service.PermittedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PermissionDetailServiceimpl implements PermissionDetailService {

    private final String target = "permission";

    private final PermissionDetailRepository permissionDetailRepository;
    private final PermissionDetailMapper permissionDetailMapper;
    private final PermittedService permittedService;

    // 특정 타겟에 대한 접근 허용 여부 반환
    @Override
    public PermissionDetailDto.AllowResDto allow(PermissionDto.ExistReqDto param, Long reqUserId) {
        List<PermissionDetailDto.DetailResDto> list = permissionDetailMapper.access(param);
        return PermissionDetailDto.AllowResDto.builder().allowed(!(list == null || list.isEmpty())).build();
    }

    // 현재 유저가 접근 가능한 권한 상세 목록 반환
    @Override
    public List<PermissionDetailDto.DetailResDto> access(PermissionDto.ExistReqDto param, Long reqUserId) {
        return detailList(permissionDetailMapper.access(param), (long) -200);
    }

    // 권한 상세 토글 (활성/비활성 전환)
    @Override
    public DefaultDto.CreateResDto toggle(PermissionDetailDto.ToggleReqDto param, Long reqUserId) {
        PermissionDetail permissionDetail = permissionDetailRepository.findByPermissionIdAndTargetAndFunc(param.getPermissionId(), param.getTarget(), param.getFunc());
        if (permissionDetail == null) {
            if (param.getAlive()) {
                return create(PermissionDetailDto.CreateReqDto.builder()
                        .permissionId(param.getPermissionId())
                        .target(param.getTarget())
                        .func(param.getFunc())
                        .build(), reqUserId);
            }
        } else {
            permittedService.isPermitted(reqUserId, target, 120);
            permissionDetail.setDeleted(!param.getAlive());
            return permissionDetailRepository.save(permissionDetail).toCreateResDto();
        }
        return DefaultDto.CreateResDto.builder().id((long) -100).build();
    }

    // 권한 상세 생성
    @Override
    public DefaultDto.CreateResDto create(PermissionDetailDto.CreateReqDto param, Long reqUserId) {
        return permissionDetailRepository.save(param.toEntity()).toCreateResDto();
    }

    // 권한 상세 수정
    @Override
    public void update(PermissionDetailDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        PermissionDetail permissionDetail = permissionDetailRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        permissionDetail.update(param);
        permissionDetailRepository.save(permissionDetail);
    }

    // 권한 상세 삭제 (소프트 딜리트)
    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(PermissionDetailDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for (Long id : param.getIds()) {
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    // 권한 상세 단건 조회
    public PermissionDetailDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);
        return permissionDetailMapper.detail(param.getId());
    }

    @Override
    public PermissionDetailDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public List<PermissionDetailDto.DetailResDto> list(PermissionDetailDto.ListReqDto param, Long reqUserId) {
        return detailList(permissionDetailMapper.list(param), reqUserId);
    }

    private List<PermissionDetailDto.DetailResDto> detailList(List<PermissionDetailDto.DetailResDto> list, Long reqUserId) {
        List<PermissionDetailDto.DetailResDto> newList = new ArrayList<>();
        for (PermissionDetailDto.DetailResDto each : list) {
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public DefaultDto.PagedListResDto pagedList(PermissionDetailDto.PagedListReqDto param, Long reqUserId) {
        DefaultDto.PagedListResDto res = param.init(permissionDetailMapper.pagedListCount(param));
        res.setList(detailList(permissionDetailMapper.pagedList(param), reqUserId));
        return res;
    }

    @Override
    public List<PermissionDetailDto.DetailResDto> scrollList(PermissionDetailDto.ScrollListReqDto param, Long reqUserId) {
        param.init();
        return detailList(permissionDetailMapper.scrollList(param), reqUserId);
    }
}
