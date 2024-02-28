package ru.prodcontest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserWithoutPassword {
    @AllArgsConstructor
    @Getter
    public static class Profile {
        private String login;
        private String email;
        private String countryCode;
        private Boolean isPublic;
        private String phone;
        private String image;
    }

    Profile profile;
}
