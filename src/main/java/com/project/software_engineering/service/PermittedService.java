package com.project.software_engineering.service;

import com.project.software_engineering.dto.PermissionDto;
import org.springframework.stereotype.Service;

@Service
public interface PermittedService {
    void isPermitted(Long userId, String target, int func);
    boolean permitted(PermissionDto.PermittedReqDto param);
}
