package com.mrs.mrs.DTO.Seat;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddSeatRequestDTO {
    private UUID screenId;
    private Character row_number;
    private Integer seat_number;
    private Integer xPos;
    private Integer yPos;
}
