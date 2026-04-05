package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.PermissionDetail;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.dto.PermissionDetailDto;
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

    @Override
    public PermissionDetailDto.AllowResDto allow(PermissionDto.ExistReqDto param, Long reqUserId) {
        List<PermissionDetailDto.DetailResDto> list = permissionDetailMapper.access(param);
        return PermissionDetailDto.AllowResDto.builder().allowed(!(list == null || list.isEmpty())).build();
    }

    @Override
    public List<PermissionDetailDto.DetailResDto> access(PermissionDto.ExistReqDto param, Long reqUserId) {
        return detailList(permissionDetailMapper.access(param), (long) -200);
    }

    @Override
    public DefaultDto.CreateResDto toggle(PermissionDetailDto.ToggleReqDto param, Long reqUserId) {
        PermissionDetail permissionDetail = permissionDetailRepository.findByPermissionIdAndTargetAndFunc(param.getPermissionId(), param.getTarget(), param.getFunc());
        if(permissionDetail == null) {
            //없는데 생성하라고 하네!
            if(param.getAlive()){
                return create(PermissionDetailDto.CreateReqDto.builder()
                        .permissionId(param.getPermissionId())
                        .target(param.getTarget())
                        .func(param.getFunc())
                        .build(), reqUserId);
            }
        } else {
            permittedService.isPermitted(reqUserId, target, 120);
            //있는데 바꿔주기!
            permissionDetail.setDeleted(!param.getAlive());
            return permissionDetailRepository.save(permissionDetail).toCreateResDto();
        }
        return DefaultDto.CreateResDto.builder().id((long) -100).build();
    }

    /**/
    @Override
    public DefaultDto.CreateResDto create(PermissionDetailDto.CreateReqDto param, Long reqUserId) {
        DefaultDto.CreateResDto res = permissionDetailRepository.save(param.toEntity()).toCreateResDto();
        return res;
    }

    @Override
    public void update(PermissionDetailDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        PermissionDetail permissionDetail = permissionDetailRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        permissionDetail.update(param);
        permissionDetailRepository.save(permissionDetail);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(PermissionDetailDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for(Long id : param.getIds()){
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    public PermissionDetailDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 200);
        PermissionDetailDto.DetailResDto res = permissionDetailMapper.detail(param.getId());
        return res;
    }

    @Override
    public PermissionDetailDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public List<PermissionDetailDto.DetailResDto> list(PermissionDetailDto.ListReqDto param, Long reqUserId) {
        return detailList(permissionDetailMapper.list(param), reqUserId);
    }

    public List<PermissionDetailDto.DetailResDto> detailList(List<PermissionDetailDto.DetailResDto> list, Long reqUserId){
        List<PermissionDetailDto.DetailResDto> newList = new ArrayList<>();
        for(PermissionDetailDto.DetailResDto each : list){
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

        //타이틀 로 스크롤 더 요청하는 경우 어쩔수 없이 작업!
        /*if("title".equals(param.getOrderby())){
            String mark = param.getMark();
            if(mark != null && !mark.isEmpty()){
                PermissionDetailDto.DetailResDto permissionDetail = permissionDetailMapper.detail(Long.parseLong(mark));
                if(permissionDetail != null){
                    mark = permissionDetail.getTitle() + "_" + permissionDetail.getId();
                    param.setMark(mark);
                }
            }
        }*/

        return detailList(permissionDetailMapper.scrollList(param), reqUserId);
    }


}
