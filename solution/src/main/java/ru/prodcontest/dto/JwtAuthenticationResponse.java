package ru.prodcontest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JwtAuthenticationResponse {
    private String token;
}
