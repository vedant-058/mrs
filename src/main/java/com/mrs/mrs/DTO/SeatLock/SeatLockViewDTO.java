package com.mrs.mrs.DTO.SeatLock;

import java.util.UUID;

import com.mrs.mrs.model.SeatLock.SeatStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatLockViewDTO {
    private UUID showtimeId;
    private UUID seatId;
    private UUID userId;
    private SeatStatus status;
    // private Instant lockedAt;
    // private Instant expiresAt;
    // private Long version;
}
