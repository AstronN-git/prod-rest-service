package ru.prodcontest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.prodcontest.entity.Country;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends CrudRepository<Country, Long> {
    Optional<Country> findCountryByAlpha2IgnoreCase(String alpha2);
    List<Country> findCountriesByRegion(String region);
}
