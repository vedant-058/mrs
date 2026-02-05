package com.mrs.mrs.DTO.Seat;

import java.util.UUID;

// import com.mrs.mrs.model.SeatLock.SeatStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatViewDTO {
    private UUID id;
    private UUID screenId;
    private Character rowNumber;
    private Integer seatNumber;
    private Integer xPos;
    private Integer yPos;

    // For seat map usage (lock/booking overlay)
    // private SeatStatus status;
    // private UUID userId;

    // Constructor used by JPA projections (SeatRepository.findByScreen_Id)
    // public SeatViewDTO(UUID id,
    //                    UUID screenId,
    //                    Character rowNumber,
    //                    Integer seatNumber,
    //                    Integer xPos,
    //                    Integer yPos) {
    //     this.id = id;
    //     this.screenId = screenId;
    //     this.rowNumber = rowNumber;
    //     this.seatNumber = seatNumber;
    //     this.xPos = xPos;
    //     this.yPos = yPos;
    // }
}