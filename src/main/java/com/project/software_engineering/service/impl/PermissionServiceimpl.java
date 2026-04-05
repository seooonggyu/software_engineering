package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.Permission;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.dto.PermissionDetailDto;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.PermissionMapper;
import com.project.software_engineering.repository.PermissionRepository;
import com.project.software_engineering.service.PermissionService;
import com.project.software_engineering.service.PermissionDetailService;
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


    @Override
    public DefaultDto.CreateResDto create(PermissionDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);
        DefaultDto.CreateResDto res = permissionRepository.save(param.toEntity()).toCreateResDto();
        return res;
    }

    @Override
    public void update(PermissionDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        Permission permission = permissionRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        permission.update(param);
        permissionRepository.save(permission);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(PermissionDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for(Long id : param.getIds()){
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    public PermissionDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);
        PermissionDto.DetailResDto res = permissionMapper.detail(param.getId());
        //이 권한에 해당되는 모든 상세 목록 가져오기!
        res.setDetails(
                permissionDetailService.list(PermissionDetailDto.ListReqDto.builder().deleted(false).permissionId(res.getId()).build(), reqUserId)
        );
        //타겟 이름 리스트 관리!
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

    public List<PermissionDto.DetailResDto> detailList(List<PermissionDto.DetailResDto> list, Long reqUserId){
        List<PermissionDto.DetailResDto> newList = new ArrayList<>();
        for(PermissionDto.DetailResDto each : list){
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

        //타이틀 로 스크롤 더 요청하는 경우 어쩔수 없이 작업!
        /*if("title".equals(param.getOrderby())){
            String mark = param.getMark();
            if(mark != null && !mark.isEmpty()){
                PermissionDto.DetailResDto permission = permissionMapper.detail(Long.parseLong(mark));
                if(permission != null){
                    mark = permission.getTitle() + "_" + permission.getId();
                    param.setMark(mark);
                }
            }
        }*/

        return detailList(permissionMapper.scrollList(param), reqUserId);
    }

}
