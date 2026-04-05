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

    @Override
    public void isPermitted(Long userId, String target, int func) {
        // -200인 경우에는, 그냥 무사 통과를 부탁한것!!
        if(userId != -200){
            if(!permitted(PermissionDto.PermittedReqDto.builder().userId(userId).target(target).func(func).build())){
                throw new NoPermissionException("no auth");
            }
        }
    }

    @Override
    public boolean permitted(PermissionDto.PermittedReqDto param) {
//        return (permissionMapper.permitted(param) > 0);
        return true;
    }
    /* 첫 admin 추가 방법 */
    // 1. return true 주석을 푼다.
    // 2. http://localhost:8080/permission/admin_list 에서 권한 생성 후 admin 유저 추가
    // 3. 다시 return 주석 변경
}
