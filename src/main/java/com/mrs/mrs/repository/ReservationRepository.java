package com.mrs.mrs.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrs.mrs.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation,UUID> {
    boolean existsByUserIdAndShowtimeId(UUID userId, UUID showtimeId);

}
