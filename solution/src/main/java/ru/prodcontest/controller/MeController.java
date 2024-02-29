package ru.prodcontest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.dto.UserWithoutPassword;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.CountryRepository;
import ru.prodcontest.service.UserService;

@RestController()
@RequestMapping("/api/me")
public class MeController {
    private final UserService userService;
    private final CountryRepository countryRepository;

    @Autowired
    public MeController(UserService userService, CountryRepository countryRepository) {
        this.userService = userService;
        this.countryRepository = countryRepository;
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
            if (countryRepository.findCountryByAlpha2(updates.getCountryCode()).isEmpty())
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
}
