package com.example.demo.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
public class ChangePasswordForm {

    @NotEmpty(message = "현재 비밀번호를 입력하세요.")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    private String currentPassword;

    @NotEmpty(message = "새 비밀번호를 입력하세요.")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    private String newPassword;

    @NotEmpty(message = "새 비밀번호 확인을 입력하세요.")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    private String confirmPassword;

}