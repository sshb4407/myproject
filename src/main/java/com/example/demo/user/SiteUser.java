package com.example.demo.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
public class SiteUser implements OAuth2User, UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String nickname;

    @Column(unique = true)
    private String email;

    @Override
    public Map<String, Object> getAttributes() { // OAuth2 인증 후 사용자의 속성 정보를 반환하는 메서드 현재 구현에서는 사용하지 않으므로 null을 반환합니다.
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 사용자에게 부여된 권한을 반환하는 메서드.
        return getGrantedAuthorities(); // 현재는 "ROLE_SITEUSER"와 "ROLE_ADMIN" 두 가지 권한만 사용하고 있습니다.
    }

    public List<GrantedAuthority> getGrantedAuthorities() {
        // 실제 권한 목록을 생성하고 반환하는 helper 메서드.
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // 모든 사용자는 기본적으로 'SITEUSER' 권한을 갖습니다.
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SITEUSER"));

        // "admin"이라는 사용자 이름을 가진 사용자는 'ADMIN' 권한도 부여받습니다.
        if ("admin".equals(username)) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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

    @Override
    public String getName() {
        return username;
    }
}