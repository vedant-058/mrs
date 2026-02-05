package com.mrs.mrs.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrs.mrs.model.ReservationSeat;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat,UUID> {

}
