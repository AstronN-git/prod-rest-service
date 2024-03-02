package ru.prodcontest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content;
    private String author;

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "likes_count")
    private Long likesCount;

    @Column(name = "dislikes_count")
    private Long dislikesCount;
}
