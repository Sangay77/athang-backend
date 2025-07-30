package com.bfs.rma.auth.sso;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class OAuthUserDetailsAdapter implements UserDetails {

    private final CustomOAuth2User oauth2User;

    public OAuthUserDetailsAdapter(CustomOAuth2User oauth2User) {
        this.oauth2User = oauth2User;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getPassword() {
        return ""; // OAuth users don't have password here
    }

    @Override
    public String getUsername() {
        return oauth2User.getEmail();  // Use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
