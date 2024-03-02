package ru.prodcontest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.prodcontest.entity.Post;

import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {
}
