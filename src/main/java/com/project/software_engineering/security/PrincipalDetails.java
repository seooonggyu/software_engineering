package com.project.software_engineering.security;

import com.project.software_engineering.domain.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final User user;
    private Map<String, Object> attributes;

    // 일반 로그인 시 사용
    public PrincipalDetails(User user) {
        this.user = user;
    }

    // OAuth2 로그인 시 사용
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getUsername(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // 모든 유저에게 USER 역할 부여
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> "ROLE_USER");
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() { return user.getId() + ""; }
}