package com.example.modfac.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CustomUserDetails.class);

    private String username;
    private String password;
    private boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails() {
    }

    public CustomUserDetails(String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        LOGGER.debug("getAuthorities method invoked");
        Collection<? extends GrantedAuthority> result = authorities;
        LOGGER.debug("getAuthorities method finished");
        return result;
    }

    @Override
    public String getPassword() {
        LOGGER.debug("getPassword method invoked");
        String result = password;
        LOGGER.debug("getPassword method finished");
        return result;
    }

    @Override
    public String getUsername() {
        LOGGER.debug("getUsername method invoked");
        String result = username;
        LOGGER.debug("getUsername method finished");
        return result;
    }

    @Override
    public boolean isAccountNonExpired() {
        LOGGER.debug("isAccountNonExpired method invoked");
        boolean result = true; // Update this based on business logic
        LOGGER.debug("isAccountNonExpired method finished");
        return result;
    }

    @Override
    public boolean isAccountNonLocked() {
        LOGGER.debug("isAccountNonLocked method invoked");
        boolean result = true; // Update this based on business logic
        LOGGER.debug("isAccountNonLocked method finished");
        return result;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        LOGGER.debug("isCredentialsNonExpired method invoked");
        boolean result = true; // Update this based on business logic
        LOGGER.debug("isCredentialsNonExpired method finished");
        return result;
    }

    @Override
    public boolean isEnabled() {
        LOGGER.debug("isEnabled method invoked");
        boolean result = enabled;
        LOGGER.debug("isEnabled method finished");
        return result;
    }
}
