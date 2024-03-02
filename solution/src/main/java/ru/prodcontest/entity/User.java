package ru.prodcontest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User implements UserDetails {
    @Id
    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "is_public")
    private Boolean isPublic;

    private String phone;
    private String image;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Friend> friends = new HashSet<>();

    public void addFriend(Friend friend) {
        friends.add(friend);
    }

    public void removeFriend(Friend friend) {
        if (!friends.contains(friend)) {
            return;
        }
        friends.remove(friend);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
