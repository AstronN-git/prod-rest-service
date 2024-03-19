package ru.prodcontest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.dto.UserWithoutPassword;
import ru.prodcontest.entity.Friend;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.FriendRepository;
import ru.prodcontest.service.UserService;

@RestController
@RequestMapping("/api")
public class ProfileController {
    private final UserService userService;
    private final FriendRepository friendRepository;

    @Autowired
    public ProfileController(UserService userService, FriendRepository friendRepository) {
        this.userService = userService;
        this.friendRepository = friendRepository;
    }

    @GetMapping("/profiles/{login}")
    public ResponseEntity<?> getByLogin(@PathVariable String login) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        User user;
        try {
            user = userService.getByLogin(login);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(new ReasonedError("requested profile does not exists"), HttpStatus.FORBIDDEN);
        }
        Friend me = friendRepository.findByLoginAndUser(
                SecurityContextHolder.getContext().getAuthentication().getName(), user);

        if (user.getIsPublic() || login.equals(SecurityContextHolder.getContext().getAuthentication().getName())
                || me != null) {
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

        return new ResponseEntity<>(new ReasonedError("requested profile is private"), HttpStatus.FORBIDDEN);
    }
}
