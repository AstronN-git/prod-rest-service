package ru.prodcontest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.prodcontest.dto.ReasonedError;
import ru.prodcontest.entity.Country;
import ru.prodcontest.repository.CountryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CountriesController {
    private final CountryRepository countryRepository;
    private final List<String> regions = List.of("Europe", "Africa", "Americas", "Oceania", "Asia");

    @Autowired
    public CountriesController(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @GetMapping("/countries")
    public ResponseEntity<?> countries(@RequestParam(required = false) List<String> regions) {
        if (regions == null || regions.isEmpty()) {
            return new ResponseEntity<>((List<Country>) countryRepository.findAll(), HttpStatus.OK);
        }

        for (String region : regions) {
            if (!isRegionValid(region))
                return new ResponseEntity<>(new ReasonedError("region is invalid"), HttpStatus.BAD_REQUEST);
        }

        List<Country> countries = new ArrayList<>();
        for (String region : regions) {
            countries.addAll(countryRepository.findCountriesByRegion(region));
        }

        return new ResponseEntity<>(countries, HttpStatus.OK);
    }

    private boolean isRegionValid(String region) {
        for (String reg : regions)
            if (region.equals(reg))
                return true;

        return false;
    }

    @GetMapping("/countries/{code}")
    public ResponseEntity<?> countryByAlpha2Code(@PathVariable String code) {
        Optional<Country> country = countryRepository.findCountryByAlpha2IgnoreCase(code);

        if (country.isEmpty())
            return new ResponseEntity<>(new ReasonedError("invalid country code"), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(country.get(), HttpStatus.OK);
    }
}
