package ru.prodcontest.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class UnauthorizedAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(401);
        PrintWriter outputStream = new PrintWriter(new BufferedOutputStream(response.getOutputStream()));
        outputStream.print("{\"reason\": \"authentication token is not present or incorrect\"}");
        outputStream.flush();
    }
}
