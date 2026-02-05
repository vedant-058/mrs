package com.mrs.mrs.DTO.ReservationSeat;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationSeatViewDTO {
    private UUID id;
    private UUID reservationId;
    private UUID seatId;
    // private Character seatRowNumber;
    // private Integer seatNumber;
    private BigDecimal amount;
}
