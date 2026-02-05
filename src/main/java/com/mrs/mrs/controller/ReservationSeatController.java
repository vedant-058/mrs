package com.mrs.mrs.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.mrs.DTO.ReservationSeat.AddReservationSeatRequestDTO;
import com.mrs.mrs.DTO.ReservationSeat.AddReservationSeatResponseDTO;
import com.mrs.mrs.DTO.ReservationSeat.ReservationSeatViewDTO;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.ReservationSeatService;

import jakarta.validation.Valid;

@RestController
public class ReservationSeatController {
    
    @Autowired
    private ReservationSeatService reservationSeatService;

    public ReservationSeatController(ReservationSeatService reservationSeatService){
        this.reservationSeatService = reservationSeatService;
    }

    @GetMapping("/reservation-seat/get-reservation-seats")
    public ResponseEntity<ApiResponse<?>> getreservationseats(){
        try {
            List<ReservationSeatViewDTO> reservationSeats = reservationSeatService.fetchReservationSeats();

            ApiResponse<List<ReservationSeatViewDTO>> response = ApiResponse.<List<ReservationSeatViewDTO>>builder()
                    .success(true)
                    .message("Reservation seats fetched successfully")
                    .data(reservationSeats)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Reservation seats could not be fetched: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/reservation-seat/add-reservation-seat")
    public ResponseEntity<ApiResponse<?>> addreservationseat(@Valid @RequestBody AddReservationSeatRequestDTO addReservationSeatRequestDTO){
        try{
            AddReservationSeatResponseDTO responseDTO = reservationSeatService.saveReservationSeat(addReservationSeatRequestDTO);
            ApiResponse<AddReservationSeatResponseDTO> response = ApiResponse.<AddReservationSeatResponseDTO>builder()
                    .success(true)
                    .message("Reservation seat added successfully")
                    .data(responseDTO) 
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch(Exception e){
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Reservation seat could not be added: " + e.getMessage())
                .timestamp(Instant.now())
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
