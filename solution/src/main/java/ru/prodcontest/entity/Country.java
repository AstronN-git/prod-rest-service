package ru.prodcontest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "countries")
@Getter
public class Country {
    @Id
    private long id;
    private String name;
    private String alpha2;
    private String alpha3;
    private String region;
}
