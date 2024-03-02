package ru.prodcontest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.prodcontest.dto.PasswordChangeRequest;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.dto.StatusResponse;
import ru.prodcontest.dto.UserWithoutPassword;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.CountryRepository;
import ru.prodcontest.service.UserService;
import ru.prodcontest.util.Validation;

@RestController()
@RequestMapping("/api/me")
public class MeController {
    private final UserService userService;
    private final CountryRepository countryRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MeController(UserService userService, CountryRepository countryRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.countryRepository = countryRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByLogin(username);
        return new ResponseEntity<>(
                new UserWithoutPassword.Profile(
                        user.getLogin(),
                        user.getEmail(),
                        user.getCountryCode(),
                        user.getIsPublic(),
                        user.getPhone(),
                        user.getImage()
                ), HttpStatus.OK
        );
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> patchProfile(@RequestBody UserWithoutPassword.Profile updates) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByLogin(username);
        if (updates.getIsPublic() != null) {
            user.setIsPublic(updates.getIsPublic());
        }

        if (updates.getPhone() != null) {
            if (!updates.getPhone().matches("\\+\\d+"))
                return new ResponseEntity<>(new ReasonedError("wrong phone format"), HttpStatus.BAD_REQUEST);
            user.setPhone(updates.getPhone());
        }

        if (updates.getImage() != null) {
            if (updates.getImage().length() > 200)
                return new ResponseEntity<>(new ReasonedError("phone is too long"), HttpStatus.BAD_REQUEST);
            user.setImage(updates.getImage());
        }

        if (updates.getCountryCode() != null) {
            if (countryRepository.findCountryByAlpha2IgnoreCase(updates.getCountryCode()).isEmpty())
                return new ResponseEntity<>(new ReasonedError("invalid country code"), HttpStatus.BAD_REQUEST);
            user.setCountryCode(updates.getCountryCode());
        }

        userService.save(user);
        return new ResponseEntity<>(
                new UserWithoutPassword.Profile(
                        user.getLogin(),
                        user.getEmail(),
                        user.getCountryCode(),
                        user.getIsPublic(),
                        user.getPhone(),
                        user.getImage()
                ), HttpStatus.OK
        );
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        ReasonedError passwordValidation = Validation.validatePassword(passwordChangeRequest.getNewPassword());
        if (passwordValidation != null) {
            return new ResponseEntity<>(passwordValidation, HttpStatus.BAD_REQUEST);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    passwordChangeRequest.getOldPassword()
            ));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(new ReasonedError("incorrect login credentials"), HttpStatus.FORBIDDEN);
        }

        User user = userService.getByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userService.save(user);

        return new ResponseEntity<>(new StatusResponse("ok"), HttpStatus.OK);
    }
}
