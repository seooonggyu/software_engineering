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

        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;
        
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String password = bCryptPasswordEncoder.encode(UUID.randomUUID().toString()); // 임시 비밀번호
        
        String email = oAuth2UserInfo.getEmail();
        
        // 한동대학교 이메일(@handong.ac.kr, @handong.edu)만 허용
//        if (email == null || (!email.endsWith("@handong.ac.kr") && !email.endsWith("@handong.edu"))) {
//            throw new OAuth2AuthenticationException("허용되지 않은 이메일 도메인입니다. 한동대학교 이메일로 로그인해주세요.");
//        }
        if (email == null) {
            throw new OAuth2AuthenticationException("허용되지 않은 이메일 도메인입니다. 한동대학교 이메일로 로그인해주세요.");
        }

        String name = oAuth2UserInfo.getName();

        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            userEntity = User.of(username, password, name, 3100);
            userRepository.save(userEntity);
        }

        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}
