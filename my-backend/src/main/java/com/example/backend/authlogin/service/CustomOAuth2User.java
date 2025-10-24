package com.example.backend.authlogin.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.backend.authlogin.domain.User;

public class CustomOAuth2User implements OAuth2User {

    private final Long userId;
    private final String email;
    private final String name;
    private final User.Provider provider;
    private final User.Role role;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public CustomOAuth2User(User user, Map<String, Object> attributes, String nameAttributeKey) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.provider = user.getProvider();
        this.role = user.getRole();
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return List.of();
        }
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getName() {
        return name;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public User.Provider getProvider() {
        return provider;
    }

    public User.Role getRole() {
        return role;
    }

    public String getNameAttributeKey() {
        return nameAttributeKey;
    }
}