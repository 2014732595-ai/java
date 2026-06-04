package com.example.secondhand.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 修改密码请求 DTO
 */
@Data
public class UpdatePasswordRequest {

    /**
     * 旧密码（首次设置密码时可为空）
     */
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "密码格式不正确，长度为 6-20 位数字或字母")
    private String newPassword;
}
