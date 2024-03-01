package ru.prodcontest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.prodcontest.entity.User;
import ru.prodcontest.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public boolean existsByLogin(String login) {
        return userRepository.existsById(login);
    }

    public boolean existsByEmailOrPhoneOrLogin(String email, String phone, String login) {
        return userRepository.existsByEmailOrPhoneOrLogin(email, phone, login);
    }

    public User getByLogin(String login) {
        return userRepository.findById(login)
                .orElseThrow(() -> new UsernameNotFoundException("username not found"));
    }

    public UserDetailsService userDetailsService() {
        return this::getByLogin;
    }

    @SuppressWarnings("unused")
    public User getCurrentUser() {
        var login = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByLogin(login);
    }
}
