package com.mrs.mrs.DTO.SeatLock;

import java.util.List;
import java.util.UUID;
import com.mrs.mrs.model.SeatLock.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatUpdateEvent {
    private List<UUID> seatIds;
    private UUID showtimeId;
    private SeatStatus status;
    private String eventType; // "LOCKED", "BOOKED", "RELEASED", "EXPIRED"
}