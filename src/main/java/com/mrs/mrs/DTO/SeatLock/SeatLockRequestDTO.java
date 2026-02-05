package com.mrs.mrs.DTO.SeatLock;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Getter
public class SeatLockRequestDTO{
    @NotEmpty(message = "Seat IDs are required")
    private List<UUID> seatIds;
    @NotBlank(message = "Showtime ID is required")
    private UUID showtimeId;
    @NotBlank(message = "User ID is required")
    private UUID userId;
}



