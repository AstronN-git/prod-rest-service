package ru.prodcontest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.prodcontest.dto.PostCreationRequest;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.entity.Post;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.FriendRepository;
import ru.prodcontest.repository.PostRepository;
import ru.prodcontest.service.UserService;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostRepository postRepository;
    private final FriendRepository friendRepository;
    private final UserService userService;

    public PostController(PostRepository postRepository, FriendRepository friendRepository, UserService userService) {
        this.postRepository = postRepository;
        this.friendRepository = friendRepository;
        this.userService = userService;
    }

    @PostMapping("/new")
    public ResponseEntity<?> newPost(@RequestBody PostCreationRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        Post post = Post.builder()
                .content(request.getContent())
                .tags(request.getTags())
                .author(SecurityContextHolder.getContext().getAuthentication().getName())
                .dislikesCount(0L)
                .likesCount(0L)
                .createdAt(new Date())
                .build();

        postRepository.save(post);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable UUID postId) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return new ResponseEntity<>(new ReasonedError("post not found"), HttpStatus.NOT_FOUND);
        }

        Post post = postOptional.get();
        User author = userService.getByLogin(post.getAuthor());
        if (friendRepository.findByLoginAndUser(
                SecurityContextHolder.getContext().getAuthentication().getName(),
                author
        ) == null) {
            return new ResponseEntity<>(new ReasonedError("post not found"), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(post, HttpStatus.OK);
    }
}
