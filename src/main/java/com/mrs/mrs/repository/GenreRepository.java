package com.mrs.mrs.repository;


import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrs.mrs.model.Genre;

public interface GenreRepository extends JpaRepository<Genre,UUID> {
    boolean existsByGenre(String genre);
    Genre findByGenre(String genre);
}
