package ru.prodcontest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.prodcontest.entity.Friend;
import ru.prodcontest.entity.User;

@Repository
public interface FriendRepository extends CrudRepository<Friend, Long> {
    Friend findByLoginAndUser(String login, User user);
}
