package com.mrs.mrs.DTO.Reservation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationViewDTO {
    private UUID id;
    private UUID userId;
    // private String userName;
    private UUID showtimeId;
    private BigDecimal amount;
    private Instant timestamp;
}
