package com.mrs.mrs.DTO.SeatMap;

import java.util.UUID;

import java.util.List;

import com.mrs.mrs.DTO.Seat.SeatViewDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatMapResponseDTO {
    private UUID showtimeId;
    private UUID screenId;
    private List<SeatViewDTO> seats;
}

