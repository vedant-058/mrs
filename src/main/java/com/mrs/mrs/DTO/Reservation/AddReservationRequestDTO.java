package com.mrs.mrs.DTO.Reservation;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddReservationRequestDTO {
    private UUID userId;
    private UUID showtimeId;
    private BigDecimal amount;
}
