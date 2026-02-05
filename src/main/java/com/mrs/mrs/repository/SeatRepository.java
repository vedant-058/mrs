package com.mrs.mrs.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mrs.mrs.model.Seat;
import com.mrs.mrs.DTO.Seat.SeatViewDTO;

public interface SeatRepository extends JpaRepository<Seat,UUID> {
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seat s WHERE s.screen.id = :screenId AND s.rowNumber = :rowNumber AND s.seatNumber = :seatNumber")
    boolean existsByScreenIdAndRowNumberAndSeatNumber(@Param("screenId") UUID screenId, @Param("rowNumber") Character rowNumber, @Param("seatNumber") Integer seatNumber);

    @Query("SELECT new com.mrs.mrs.DTO.Seat.SeatViewDTO(s.id, s.screen.id, s.rowNumber, s.seatNumber, s.xPos, s.yPos) FROM Seat s WHERE s.screen.id = :screenId")
    List<SeatViewDTO> findByScreen_Id(@Param("screenId") UUID screenId);
}
