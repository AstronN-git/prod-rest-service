package ru.prodcontest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserWithoutPassword {
    @AllArgsConstructor
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
