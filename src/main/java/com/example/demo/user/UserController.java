package com.example.demo.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(UserCreateForm userCreateForm) {
        return "login_form";
    }

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "login_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "login_form";
        }

        if (userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "login_form";
        }
        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getPassword1(),
                    userCreateForm.getEmail(), userCreateForm.getNickname());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "login_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "login_form";
        }
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myPage")
    public String userMyPage(Model model, Principal principal) {
        String username = principal.getName();
        SiteUser siteUser = userService.getUser(username);
        model.addAttribute("siteUser", siteUser);
        return "mypage";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String userModify(UserUpdateForm userUpdateForm, @PathVariable Long id, Model model) {
        SiteUser siteUser = this.userService.getUser(id);
        userUpdateForm.setNickname(siteUser.getNickname());
        userUpdateForm.setEmail(siteUser.getEmail());
        model.addAttribute("siteUser", siteUser);
        return "user_modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String userModify(@Valid UserUpdateForm userUpdateForm, BindingResult bindingResult, @PathVariable("id") Long id) {
        if (bindingResult.hasErrors()) {
            return "user_modify";
        }
        SiteUser siteUser = this.userService.getUser(id);

        String newNn = (userUpdateForm.getNickname() == null) ? siteUser.getNickname() : userUpdateForm.getNickname();
        String newEm = (userUpdateForm.getEmail() == null) ? siteUser.getEmail() : userUpdateForm.getEmail();

        this.userService.modify(siteUser, newNn, newEm);
        return "redirect:/user/myPage";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/passModify/{id}")
    public String passModify(@PathVariable("id") Long id, Model model) {
        SiteUser siteUser = this.userService.getUser(id);
        model.addAttribute("siteUser", siteUser);
        return "password_modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/passModify")
    public String passModify(Principal principal, String password) {
        SiteUser siteUser = userService.getUser(principal.getName());
        userService.passModify(siteUser, password);
        return "redirect:/user/myPage";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("delete")
    public String userDelete(Principal principal, Model model) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        model.addAttribute("siteUser", siteUser);
        return "user_delete"; // 변경된 부분: 리다이렉트가 아니라 뷰 이름을 반환합니다.
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete")
    public String userDelete(HttpServletRequest request, @RequestParam("password") String password,
                             HttpServletResponse response, Principal principal, RedirectAttributes attributes) {
        SiteUser siteUser = this.userService.getUser(principal.getName());

        if (userService.isCorrectPassword(siteUser.getUsername(), password)) {
            // 비밀번호가 일치하는 경우 사용자를 삭제
            this.userService.deleteUserAndRelatedData(siteUser.getUsername());

            // 사용자 삭제 후 로그아웃 처리
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            return "redirect:/";
        } else {
            // 비밀번호가 일치하지 않을 때 오류 메시지를 전달하고 회원 탈퇴 페이지로 리다이렉트합니다.
            attributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다. 다시 시도해주세요.");
            return "redirect:/user/delete";
        }
    }
        @PreAuthorize("isAuthenticated()")
        @GetMapping("/changePassword")
        public String changePassword (ChangePasswordForm changePasswordForm, Model model){
            return "change_password";
        }

        @PreAuthorize("isAuthenticated()")
        @PostMapping("/changePassword")
        public String changePassword (@Valid ChangePasswordForm changePasswordForm, BindingResult bindingResult){
            if (bindingResult.hasErrors()) {
                return "change_password";
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            SiteUser siteUser = userService.getUser(username);

            if (!userService.isCorrectPassword(siteUser.getUsername(), changePasswordForm.getCurrentPassword())) {
                bindingResult.rejectValue("currentPassword", "currentPasswordIncorrect", "현재 비밀번호가 일치하지 않습니다.");
                return "change_password";
            }

            if (!changePasswordForm.getNewPassword().equals(changePasswordForm.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "passwordMismatch", "새로운 비밀번호가 일치하지 않습니다.");
                return "change_password";
            }

            userService.passModify(siteUser, changePasswordForm.getNewPassword());
            return "redirect:/user/myPage";
        }
    }