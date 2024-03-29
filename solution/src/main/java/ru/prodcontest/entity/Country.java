package ru.prodcontest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "countries")
@Getter
public class Country {
    @Id
    @JsonIgnore
    private Long id;
    private String name;
    private String alpha2;
    private String alpha3;
    private String region;
}
