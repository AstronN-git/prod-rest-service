package ru.prodcontest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.entity.Post;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.FriendRepository;
import ru.prodcontest.repository.PostRepository;
import ru.prodcontest.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/posts/feed")
public class FeedController {
    private final PostRepository postRepository;
    private final UserService userService;
    private final FriendRepository friendRepository;

    public FeedController(PostRepository postRepository, UserService userService, FriendRepository friendRepository) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.friendRepository = friendRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<?> myFeed(
            @RequestParam(required = false, defaultValue = "5") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        return feedByLogin(SecurityContextHolder.getContext().getAuthentication().getName(), limit, offset);
    }

    @GetMapping("/{login}")
    public ResponseEntity<?> feedByLogin(
            @PathVariable String login,
            @RequestParam(required = false, defaultValue = "5") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        if (offset < 0 || limit > 50 || limit < 0) {
            return new ResponseEntity<>(new ReasonedError("limit/offset is out of bound"),
                    HttpStatus.BAD_REQUEST);
        }

        User user = userService.getByLogin(login);

        if (user == null) {
            return new ResponseEntity<>(new ReasonedError("user not found"), HttpStatus.NOT_FOUND);
        }

        if (user != userService.getCurrentUser()
        && friendRepository.findByLoginAndUser(
                SecurityContextHolder.getContext().getAuthentication().getName(),
                user) == null) {
            return new ResponseEntity<>(new ReasonedError("user profile is private"), HttpStatus.NOT_FOUND);
        }

        List<Post> posts = postRepository
                .findAllByAuthor(login)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .skip(offset)
                .limit(limit)
                .toList();

        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
}
