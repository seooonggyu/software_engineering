package com.project.software_engineering;

import com.project.software_engineering.security.AuthServiceImpl;
import com.project.software_engineering.security.ExternalProperties;
import org.junit.jupiter.api.Test;

public class JwtTest {
    @Test
    public void test() {
        ExternalProperties props = new ExternalProperties();
        props.setTokenSecretKey("cfc6f8968e16e357e2a588674174bc2ca34b538e9f759d081d48721fd0aa1f626188fca140ca58a5b88d6e98b3302c52f49f7fa1dc06924c34ecd45b31ba675e");
        props.setAccessTokenExpirationTime(1800000L);
        AuthServiceImpl authService = new AuthServiceImpl(props, null);
        
        String token = authService.createAccessToken(2L);
        System.out.println("Token: " + token);
        Long id = authService.verifyAccessToken(token);
        System.out.println("ID: " + id);
    }
}
