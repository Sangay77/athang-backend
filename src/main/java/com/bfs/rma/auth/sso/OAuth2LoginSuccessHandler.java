package com.bfs.rma.auth.sso;

import com.bfs.rma.auth.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION_TIME;

    @Autowired
    private JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

        OAuthUserDetailsAdapter userDetails = new OAuthUserDetailsAdapter(oauthUser);

        String jwtToken = jwtService.generateToken(Map.of(), userDetails, JWT_EXPIRATION_TIME);
        logger.info("++jwt token generate for oAuth user {}", jwtToken);
        request.getSession().setAttribute("JWT_TOKEN", jwtToken);
        String redirectUrl = "http://localhost:4200/oauth2/redirect?token=" + jwtToken;
        response.sendRedirect(redirectUrl);
    }
}
