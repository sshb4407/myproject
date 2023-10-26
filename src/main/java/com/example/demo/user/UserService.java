package com.example.demo.user;

import com.example.demo.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SiteUser create(String username, String password, String email, String nickname) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        /*user.setEmail(email);*/
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found");
        }
    }
    public SiteUser getUser(Long id) {
        Optional<SiteUser> siteUser = this.userRepository.findById(id);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found");
        }
    }

    @Transactional
    public void modify(SiteUser siteUser, String nickname, String email) {
        siteUser.setNickname(nickname);
        siteUser.setEmail(email);
        this.userRepository.save(siteUser);
    }

    @Transactional
    public void passModify(SiteUser siteUser, String password) {
        if (password != null && password.length() > 0) {
            siteUser.setPassword(passwordEncoder.encode(password));
        }
    }

    public void delete(SiteUser siteUser) {
        this.userRepository.delete(siteUser);
    }

    public boolean isCorrectPassword(String username, String password) {
        SiteUser user = getUser(username);
        String actualPassword = user.getPassword();
        return passwordEncoder.matches(password, actualPassword);
    }

    @Transactional
    public void deleteUserAndRelatedData(String username) {
        // 사용자 조회
        SiteUser siteUser = userRepository.findByUsername(username).orElse(null);

        if (siteUser != null) {

            // 사용자 삭제
            userRepository.delete(siteUser);
        }
    }
    @Transactional
    public SiteUser whenSocialLogin(String providerTypeCode, String username, String nickname) {
        Optional<SiteUser> oSiteUser = findByUsername(username);

        if (oSiteUser.isPresent()) return oSiteUser.get();

        // 소셜 로그인를 통한 가입시 비번은 없다.
        return create(username, "", "",nickname); // 최초 로그인 시 딱 한번 실행
    }

    private Optional<SiteUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void SetTempPassword(String to, String authNum) {
    }
}