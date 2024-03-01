package ru.prodcontest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.prodcontest.dto.LoginRequestBody;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.dto.StatusResponse;
import ru.prodcontest.entity.Friend;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.FriendRepository;
import ru.prodcontest.service.UserService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendsController {

    private final UserService userService;
    private final FriendRepository friendRepository;

    public FriendsController(UserService userService, FriendRepository friendRepository) {
        this.userService = userService;
        this.friendRepository = friendRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestBody LoginRequestBody login) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        if (!userService.existsByLogin(login.getLogin())) {
            return new ResponseEntity<>(new ReasonedError("user with this login is not found"), HttpStatus.NOT_FOUND);
        }

        User currentUser = userService.getCurrentUser();
        Friend friend = new Friend();
        friend.setLogin(login.getLogin());
        friend.setDateAdded(new Date());
        friend.setUser(currentUser);
        currentUser.addFriend(friend);
        userService.save(currentUser);

        return new ResponseEntity<>(new StatusResponse("ok"), HttpStatus.OK);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFriend(@RequestBody LoginRequestBody login) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        User currentUser = userService.getCurrentUser();
        Friend friend = friendRepository.findByLoginAndUser(login.getLogin(), currentUser);

        if (friend != null) {
            currentUser.removeFriend(friend);
            userService.save(currentUser);
        }

        return new ResponseEntity<>(new StatusResponse("ok"), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<?> getFriends(
            @RequestParam(required = false, defaultValue = "1000000000") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getCurrentUser();
        List<Friend> friends = user.getFriends().stream()
                .sorted((a, b) -> b.getDateAdded().compareTo(a.getDateAdded()))
                .skip(offset)
                .limit(limit)
                .toList();
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }
}
