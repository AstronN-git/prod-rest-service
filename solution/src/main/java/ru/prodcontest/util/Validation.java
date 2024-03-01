package ru.prodcontest.util;

import ru.prodcontest.dto.ReasonedError;

public class Validation {
    public static ReasonedError validatePassword(String password) {
        if (password.length() < 6) {
            return new ReasonedError("password is too short");
        }

        if (password.length() > 100) {
            return new ReasonedError("password is too long");
        }

        boolean lower = false;
        boolean upper = false;
        boolean digit = false;
        for (char ch : password.toCharArray()) {
            if (ch >= 'a' && ch <= 'z') {
                lower = true;
            }

            if (ch >= 'A' && ch <= 'Z') {
                upper = true;
            }

            if (ch >= '0' && ch <= '9') {
                digit = true;
            }
        }

        if (!lower) {
            return new ReasonedError("password must contain lower latin letter");
        }

        if (!upper) {
            return new ReasonedError("password must contain upper latin letter");
        }

        if (!digit) {
            return new ReasonedError("password must contain at least one digit");
        }

        return null;
    }
}
