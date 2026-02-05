package com.mrs.mrs.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrs.mrs.model.Movie;
import com.mrs.mrs.model.Screen;
import com.mrs.mrs.model.Showtime;

public interface ShowtimeRepository extends JpaRepository<Showtime,UUID>{
    
    /**
     * Checks if a screen already has a showtime at the given time.
     * @param screenId The screen ID to check
     * @param showtime The showtime to check
     * @return Optional containing the existing Showtime if found, empty otherwise
     */
    Optional<Showtime> findByScreenIdAndShowtime(UUID screenId, Instant showtime);
    
    /**
     * Checks if a screen already has a showtime at the given time (excluding a specific showtime ID for updates).
     * @param screenId The screen ID to check
     * @param showtime The showtime to check
     * @param excludeId The showtime ID to exclude from the check (useful for updates)
     * @return true if a conflicting showtime exists, false otherwise
     */
    boolean existsByMovieAndScreen(Movie Movie, Screen screen, Instant showtime);
}
