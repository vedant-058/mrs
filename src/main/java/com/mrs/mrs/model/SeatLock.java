package com.mrs.mrs.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seat_lock",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_showtime_seat",
        columnNames = {"showtime_id", "seat_id"}
    ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatLock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Version
    private Long version; // For optimistic locking

    public enum SeatStatus {
        AVAILABLE,  // Seat is free
        LOCKED,     // Temporarily held (5-10 min timeout)
        BOOKED      // Permanently booked (payment completed)
    }
}
