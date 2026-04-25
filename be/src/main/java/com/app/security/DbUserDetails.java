package com.app.security;

import com.app.domain.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Wraps {@link UserEntity} for Spring Security. The session stores this via {@link UserDetails}
 * in the HTTP session (default {@link org.springframework.security.web.context.HttpSessionSecurityContextRepository}).
 */
public class DbUserDetails implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;
    private final UserEntity user;

    public DbUserDetails(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getUserId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
        return user.isActive();
    }
}
