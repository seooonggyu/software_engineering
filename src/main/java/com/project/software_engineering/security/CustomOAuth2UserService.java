package com.project.software_engineering.security;

import com.project.software_engineering.domain.User;
import com.project.software_engineering.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * OAuth2 로그인 시 구글 유저 정보를 가져와 자동 회원가입 처리하는 서비스
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        }

        if (oAuth2UserInfo == null) {
            return super.loadUser(userRequest);
        }

        String username = oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId();
        String password = new BCryptPasswordEncoder().encode(UUID.randomUUID().toString());
        String email = oAuth2UserInfo.getEmail();

        // 이메일 없으면 로그인 거부
        if (email == null) {
            throw new OAuth2AuthenticationException("이메일이 없는 계정입니다.");
        }
        String name = oAuth2UserInfo.getName();
        User userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            userEntity = User.of(username, password, oAuth2UserInfo.getName(), 3100);
            userRepository.save(userEntity);
        } else if (name != null && !name.isBlank()
                && (userEntity.getName() == null || userEntity.getName().isBlank())) {
            // 이름 없이 생성된 기존 구글 계정에 이름을 채워준다 (채팅 목록 등에서 구글 ID 대신 이름 표시)
            userEntity.setName(name);
            userRepository.save(userEntity);
        }

        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}
