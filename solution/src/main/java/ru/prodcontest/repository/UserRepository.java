package ru.prodcontest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.prodcontest.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    boolean existsByEmailOrPhoneOrLogin(String email, String phone, String login);
    boolean existsByEmailOrLogin(String email, String login);

    boolean existsByPhone(String phone);
    User findUserByPhone(String phone);
}
