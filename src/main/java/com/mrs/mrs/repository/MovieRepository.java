package com.mrs.mrs.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrs.mrs.model.Movie;

public interface MovieRepository extends JpaRepository<Movie,UUID> {
    boolean existsByName(String name);
}