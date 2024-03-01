package ru.prodcontest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}
