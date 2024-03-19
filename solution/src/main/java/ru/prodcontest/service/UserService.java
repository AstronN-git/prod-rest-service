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

    public boolean existsByPhone(String phone) {
        if (phone == null)
            return false;

        return userRepository.existsByPhone(phone);
    }

    public String getLoginByPhone(String phone) {
        if (phone == null)
            return null;

        return userRepository.findUserByPhone(phone).getLogin();
    }

    public boolean existsByEmailOrPhoneOrLogin(String email, String phone, String login) {
        if (phone == null)
            return existsByEmailOrLogin(email, login);

        return userRepository.existsByEmailOrPhoneOrLogin(email, phone, login);
    }

    private boolean existsByEmailOrLogin(String email, String login) {
        return userRepository.existsByEmailOrLogin(email, login);
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
