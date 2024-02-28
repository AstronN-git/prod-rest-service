package ru.prodcontest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.prodcontest.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByPhone(String phone);
}
