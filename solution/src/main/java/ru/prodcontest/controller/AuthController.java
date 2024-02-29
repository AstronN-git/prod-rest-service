package ru.prodcontest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.prodcontest.dto.AuthenticationRequest;
import ru.prodcontest.dto.JwtAuthenticationResponse;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.dto.UserWithoutPassword;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.CountryRepository;
import ru.prodcontest.service.JwtService;
import ru.prodcontest.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, CountryRepository countryRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.countryRepository = countryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (!checkUserNotExists(user)) {
            return new ResponseEntity<>(new ReasonedError("user with this login credentials already exists"), HttpStatus.CONFLICT);
        }

        ReasonedError error = validateUser(user);

        if (error != null) {
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);

        return new ResponseEntity<>(new UserWithoutPassword(
                new UserWithoutPassword.Profile(
                        user.getLogin(),
                        user.getEmail(),
                        user.getCountryCode(),
                        user.getIsPublic(),
                        user.getPhone(),
                        user.getImage())), HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    private ResponseEntity<?> signIn(@RequestBody AuthenticationRequest authenticationRequest) {
        if (authenticationRequest.getPassword() == null
            || authenticationRequest.getLogin() == null) {
            return new ResponseEntity<>(new ReasonedError("login or password is not present"), HttpStatus.UNAUTHORIZED);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getLogin(),
                    authenticationRequest.getPassword()
            ));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(new ReasonedError("incorrect login credentials"), HttpStatus.UNAUTHORIZED);
        }

        var user = (User) userService.userDetailsService().loadUserByUsername(authenticationRequest.getLogin());

        String jwtToken = jwtService.generateAuthToken(user.getUsername(), authenticationRequest.getPassword());
        return new ResponseEntity<>(new JwtAuthenticationResponse(jwtToken), HttpStatus.OK);
    }

    private ReasonedError validateUser(User user) {
        if (user.getLogin() == null || user.getPassword() == null
            || user.getEmail() == null || user.getCountryCode() == null) {
            return new ReasonedError("not all login credentials are present");
        }

        if (user.getLogin().length() > 30 || !user.getLogin().matches("[a-zA-Z0-9-]+")) {
            return new ReasonedError("login should be 30 characters at most and contains only english letters, numbers and dash");
        }

        if (user.getEmail().length() > 50) {
            return new ReasonedError("email is too long");
        }

        if (user.getPhone() != null && !user.getPhone().matches("\\+\\d+")) {
            return new ReasonedError("wrong phone format");
        }

        if (user.getImage() != null && user.getImage().length() > 200) {
            return new ReasonedError("image url is too long");
        }

        if (countryRepository.findCountryByAlpha2(user.getCountryCode()).isEmpty()) {
            return new ReasonedError("country already taken");
        }

        if (user.getPassword().length() < 6) {
            return new ReasonedError("password is too short");
        }

        if (user.getPassword().length() > 100) {
            return new ReasonedError("password is too long");
        }

        return null;
    }

    private boolean checkUserNotExists(User user) {
        return !userService.existsByEmailOrPhoneOrLogin(user.getEmail(), user.getPhone(), user.getLogin());
//            return new ReasonedError("user with this login credentials already exists");
    }
}
