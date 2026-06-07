package com.project.software_engineering.service.impl;

import com.project.software_engineering.dto.PermissionDto;
import com.project.software_engineering.exception.NoPermissionException;
import com.project.software_engineering.mapper.PermissionMapper;
import com.project.software_engineering.service.PermittedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PermittedServiceimpl implements PermittedService {

    private final PermissionMapper permissionMapper;

    // 권한 검증 (-200은 검증 없이 통과)
    @Override
    public void isPermitted(Long userId, String target, int func) {
        if (userId != -200) {
            if (!permitted(PermissionDto.PermittedReqDto.builder().userId(userId).target(target).func(func).build())) {
                throw new NoPermissionException("no auth");
            }
        }
    }

    @Override
    public boolean permitted(PermissionDto.PermittedReqDto param) {
        // TODO: 실제 권한 체크 필요 시 아래 주석을 풀고 return true 제거
        // return (permissionMapper.permitted(param) > 0);
        return true;
    }
}
