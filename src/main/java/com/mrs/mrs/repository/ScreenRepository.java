package com.mrs.mrs.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrs.mrs.model.Screen;

public interface ScreenRepository extends JpaRepository<Screen,UUID> {
    boolean existsByName(String ScreenName);
}
