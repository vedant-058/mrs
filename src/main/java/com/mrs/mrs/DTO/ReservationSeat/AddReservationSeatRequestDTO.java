package com.mrs.mrs.DTO.ReservationSeat;

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
public class AddReservationSeatRequestDTO {
    private UUID reservationId;
    private UUID seatId;
    private BigDecimal amount;
}
