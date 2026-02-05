package com.mrs.mrs.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mrs.mrs.model.SeatLock;
import com.mrs.mrs.model.SeatLock.SeatStatus;

public interface SeatLockRepository extends JpaRepository<SeatLock, UUID> {
       
       /**
        * Find seat lock for a specific showtime and seat
        */
       Optional<SeatLock> findByShowtimeIdAndSeatId(UUID showtimeId, UUID seatId);

       /**
        * Find all seat locks for a showtime (any status).
        * Useful for building a full seat map without N+1 queries.
        */
       @Query("SELECT sl FROM SeatLock sl WHERE sl.showtime.id = :showtimeId")
       List<SeatLock> findByShowtimeId(@Param("showtimeId") UUID showtimeId);
       
       /**
        * Find all available seats for a showtime
        */
       @Query("SELECT sl FROM SeatLock sl WHERE sl.showtime.id = :showtimeId AND sl.status = :status")
       List<SeatLock> findByShowtimeIdAndStatus(@Param("showtimeId") UUID showtimeId, @Param("status") SeatStatus status);
       
       /**
        * Release expired locks (status = LOCKED and expiresAt < now)
        */
       @Modifying
       @Query("UPDATE SeatLock sl SET sl.status = 'AVAILABLE', sl.lockedAt = NULL, sl.expiresAt = NULL " +
              "WHERE sl.status = 'LOCKED' AND sl.expiresAt < :now")
       int releaseExpiredLocks(@Param("now") Instant now);
       
       /**
        * Find user's locked seats for a showtime
        */
       @Query("SELECT sl FROM SeatLock sl WHERE sl.showtime.id = :showtimeId " +
              "AND sl.user.id = :userId AND sl.status = 'LOCKED'")
       List<SeatLock> findLockedSeatsByUserAndShowtime(@Param("userId") UUID userId, @Param("showtimeId") UUID showtimeId);
       
       /**
        * Check if seat is available (considering expired locks)
        */
       @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM SeatLock sl " + // return true if > 0 false if = 0
              "WHERE sl.showtime.id = :showtimeId AND sl.seat.id = :seatId " + // filter seat of that particualr showtime
              "AND (sl.status = 'BOOKED' OR (sl.status = 'LOCKED' AND sl.expiresAt > :now))") 
                     //If the status is 'BOOKED', the seat has been paid for and is definitely unavailable
                     //If previously locked and lock expired treat as avl
       boolean isSeatUnavailable(@Param("showtimeId") UUID showtimeId, 
                                   @Param("seatId") UUID seatId, 
                                   @Param("now") Instant now);

       @Modifying(clearAutomatically = true, flushAutomatically = true)
       @Query("""
              UPDATE SeatLock sl
              SET sl.status = 'BOOKED',
              sl.expiresAt = NULL
              WHERE sl.showtime.id = :showtimeId
              AND sl.seat.id IN :seatIds
              AND sl.user.id = :userId
              AND sl.status = 'LOCKED'
              AND sl.expiresAt >= :now
       """)
       int confirmSeatsBulk(@Param("seatIds") List<UUID> seatIds,
                            @Param("showtimeId") UUID showtimeId,
                            @Param("userId") UUID userId,
                            @Param("now") Instant now);
                                   
                           
                              
}
