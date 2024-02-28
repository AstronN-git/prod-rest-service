package ru.prodcontest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.prodcontest.dto.NotFoundDTO;
import ru.prodcontest.entity.Country;
import ru.prodcontest.repository.CountryRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CountriesController {
    private final CountryRepository countryRepository;

    @Autowired
    public CountriesController(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @GetMapping("/countries")
    public List<Country> countries() {
        return (List<Country>) countryRepository.findAll();
    }

    @GetMapping("/countries/{code}")
    public ResponseEntity<?> countryByAlpha2Code(@PathVariable String code) {
        Optional<Country> country = countryRepository.findCountryByAlpha2(code);
        if (country.isEmpty())
            return new ResponseEntity<>(new NotFoundDTO("invalid country code"), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(country.get(), HttpStatus.OK);
    }
}
