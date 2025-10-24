package com.example.modfac.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomUserDetails.class);
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
        LOG.debug("getAuthorities method invoked");
        Collection<? extends GrantedAuthority> result = authorities;
        LOG.debug("getAuthorities method finished");
        return result;
    }

    @Override
    public String getPassword() {
        LOG.debug("getPassword method invoked");
        String result = password;
        LOG.debug("getPassword method finished");
        return result;
    }

    @Override
    public String getUsername() {
        LOG.debug("getUsername method invoked");
        String result = username;
        LOG.debug("getUsername method finished");
        return result;
    }

    @Override
    public boolean isAccountNonExpired() {
        LOG.debug("isAccountNonExpired method invoked");
        boolean result = true; // Update this based on business logic
        LOG.debug("isAccountNonExpired method finished");
        return result;
    }

    @Override
    public boolean isAccountNonLocked() {
        LOG.debug("isAccountNonLocked method invoked");
        boolean result = true; // Update this based on business logic
        LOG.debug("isAccountNonLocked method finished");
        return result;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        LOG.debug("isCredentialsNonExpired method invoked");
        boolean result = true; // Update this based on business logic
        LOG.debug("isCredentialsNonExpired method finished");
        return result;
    }

    @Override
    public boolean isEnabled() {
        LOG.debug("isEnabled method invoked");
        boolean result = enabled;
        LOG.debug("isEnabled method finished");
        return result;
    }
}
