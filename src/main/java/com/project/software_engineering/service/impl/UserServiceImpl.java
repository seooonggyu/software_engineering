package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.User;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.UserDto;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.UserMapper;
import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.service.PermittedService;
import com.project.software_engineering.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final String target = "user";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PermittedService permittedService;

    // 일반 회원가입
    @Override
    public DefaultDto.CreateResDto signup(UserDto.CreateReqDto param, Long reqUserId) {
        param.setRfrom(1000);
        return processUserCreation(param);
    }

    // 관리자 유저 생성
    @Override
    public DefaultDto.CreateResDto create(UserDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);
        return processUserCreation(param);
    }

    // 유저 생성 공통 로직
    private DefaultDto.CreateResDto processUserCreation(UserDto.CreateReqDto param) {
        if (userRepository.findByUsername(param.getUsername()) != null) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        param.setPassword(bCryptPasswordEncoder.encode(param.getPassword()));
        User newUser = userRepository.save(param.toEntity());
        return newUser.toCreateResDto();
    }

    // 유저 정보 수정
    @Override
    public void update(UserDto.UpdateReqDto param, Long reqUserId) {
        if (param.getId() == 0) { param.setId(reqUserId); }
        if (!param.getId().equals(reqUserId)) {
            permittedService.isPermitted(reqUserId, target, 120);
        }
        User user = userRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if (param.getPassword() != null) { user.setPassword(bCryptPasswordEncoder.encode(param.getPassword())); }
        if (param.getDeleted() != null) { user.setDeleted(param.getDeleted()); }
        if (param.getName() != null) { user.setName(param.getName()); }
        userRepository.save(user);
    }

    // 유저 삭제 (소프트 딜리트)
    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(UserDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    // 유저 단건 조회
    @Override
    public UserDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        if (param.getId() == null || param.getId() == 0) {
            param.setId(reqUserId);
        }
        return get(param, reqUserId);
    }

    private UserDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        if (!param.getId().equals(reqUserId)) {
            permittedService.isPermitted(reqUserId, target, 200);
        }
        return userMapper.detail(param.getId());
    }

    // 유저 목록 조회
    @Override
    public List<UserDto.DetailResDto> list(UserDto.ListReqDto param, Long reqUserId) {
        return detailList(userMapper.list(param), reqUserId);
    }

    private List<UserDto.DetailResDto> detailList(List<UserDto.DetailResDto> list, Long reqUserId) {
        List<UserDto.DetailResDto> newList = new ArrayList<>();
        for (UserDto.DetailResDto each : list) {
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }
}
