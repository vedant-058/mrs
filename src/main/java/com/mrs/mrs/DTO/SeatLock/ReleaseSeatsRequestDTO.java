package com.mrs.mrs.DTO.SeatLock;

import java.util.List;
import java.util.UUID;

import lombok.Getter;

@Getter
public class ReleaseSeatsRequestDTO{
    private List<UUID> seatIds;
    private UUID showtimeId;
    private UUID userId;
}