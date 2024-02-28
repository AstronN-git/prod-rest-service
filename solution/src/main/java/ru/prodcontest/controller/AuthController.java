package ru.prodcontest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.dto.UserWithoutPassword;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.CountryRepository;
import ru.prodcontest.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;

    @Autowired
    public AuthController(UserRepository userRepository, CountryRepository countryRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        System.out.println("here");
        ReasonedError e;
        if ((e = validateUser(user)) != null) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }

        if ((e = checkUserNotExists(user)) != null) {
            return new ResponseEntity<>(e, HttpStatus.CONFLICT);
        }

        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
        return new ResponseEntity<>(new UserWithoutPassword(
                new UserWithoutPassword.Profile(
                        user.getLogin(),
                        user.getEmail(),
                        user.getCountryCode(),
                        user.getIsPublic(),
                        user.getPhone(),
                        user.getImage())), HttpStatus.CREATED);
    }

    ReasonedError validateUser(User user) {
        // TODO: validate all fields

        if (countryRepository.findCountryByAlpha2(user.getCountryCode()).isEmpty())
            return new ReasonedError("country already taken");

        if (user.getPassword().length() < 6)
            return new ReasonedError("password is too short");

        return null;
    }

    ReasonedError checkUserNotExists(User user) {
        if (userRepository.existsById(user.getLogin()))
            return new ReasonedError("user with this login already exists");

        if (userRepository.findUserByEmail(user.getEmail()).isPresent())
            return new ReasonedError("user with this email already exists");

        if (userRepository.findUserByPhone(user.getPhone()).isPresent())
            return new ReasonedError("user with this phone already exists");

        return null;
    }
}
