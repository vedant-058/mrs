package com.mrs.mrs.DTO.Seat;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddSeatResponseDTO {
    private UUID id;
    private UUID screenId;
    private Character rowNumber;
    private Integer seatNumber;
    private Integer xpos;
    private Integer ypos;
}
