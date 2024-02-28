package ru.prodcontest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    private String login;
    private String email;

    private String password;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "is_public")
    private Boolean isPublic;

    private String phone;
    private String image;
}
