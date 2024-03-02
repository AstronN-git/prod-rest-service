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
import java.util.List;
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
        ResponseEntity<?> responseEntity = checkPostAvailable(post);
        if (responseEntity != null) {
            return responseEntity;
        }

        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @GetMapping("/feed/my")
    public ResponseEntity<?> myFeed(
            @RequestParam(required = false, defaultValue = "5") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        return feedByLogin(SecurityContextHolder.getContext().getAuthentication().getName(), limit, offset);
    }

    @GetMapping("/feed/{login}")
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

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable UUID postId) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return new ResponseEntity<>(new ReasonedError("post not found"), HttpStatus.NOT_FOUND);
        }

        Post post = postOptional.get();
        ResponseEntity<?> responseEntity = checkPostAvailable(post);
        if (responseEntity != null) {
            return responseEntity;
        }

        User user = userService.getCurrentUser();
        if (user.getLikedPosts().contains(postId)) {
            return new ResponseEntity<>(post, HttpStatus.OK);
        }

        if (user.getDislikedPosts().contains(postId)) {
            user.getDislikedPosts().remove(postId);
            userService.save(user);
            post.setDislikesCount(post.getDislikesCount() - 1);
        }

        user.getLikedPosts().add(postId);
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @PostMapping("/{postId}/dislike")
    public ResponseEntity<?> dislikePost(@PathVariable UUID postId) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return new ResponseEntity<>(new ReasonedError("authentication token is not present or incorrect"), HttpStatus.UNAUTHORIZED);
        }

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return new ResponseEntity<>(new ReasonedError("post not found"), HttpStatus.NOT_FOUND);
        }

        Post post = postOptional.get();
        ResponseEntity<?> responseEntity = checkPostAvailable(post);
        if (responseEntity != null) {
            return responseEntity;
        }

        User user = userService.getCurrentUser();
        if (user.getDislikedPosts().contains(postId)) {
            return new ResponseEntity<>(post, HttpStatus.OK);
        }

        if (user.getLikedPosts().contains(postId)) {
            user.getLikedPosts().remove(postId);
            userService.save(user);
            post.setLikesCount(post.getLikesCount() - 1);
        }

        user.getDislikedPosts().add(postId);
        post.setDislikesCount(post.getDislikesCount() + 1);
        postRepository.save(post);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    private ResponseEntity<?> checkPostAvailable(Post post) {
        User author = userService.getByLogin(post.getAuthor());
        if (friendRepository.findByLoginAndUser(
                SecurityContextHolder.getContext().getAuthentication().getName(),
                author
        ) == null) {
            return new ResponseEntity<>(new ReasonedError("post not found"), HttpStatus.NOT_FOUND);
        }

        return null;
    }
}
