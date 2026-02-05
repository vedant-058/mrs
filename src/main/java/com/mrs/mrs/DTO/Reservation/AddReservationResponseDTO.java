package com.mrs.mrs.DTO.Reservation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddReservationResponseDTO {
    private UUID reservationId;
    private UUID userId;
    private UUID showtimeId;
    private BigDecimal amount;
    private Instant timestamp;
}
