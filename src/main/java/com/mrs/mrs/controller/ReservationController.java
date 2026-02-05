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

import com.mrs.mrs.DTO.Reservation.AddReservationRequestDTO;
import com.mrs.mrs.DTO.Reservation.AddReservationResponseDTO;
import com.mrs.mrs.DTO.Reservation.ReservationViewDTO;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.ReservationService;

import jakarta.validation.Valid;

@RestController
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @GetMapping("/reservation/get-reservations")
    public ResponseEntity<ApiResponse<?>> getreservation(){
        try {
            List<ReservationViewDTO> reservations = reservationService.fetchReservations();

            ApiResponse<List<ReservationViewDTO>> response = ApiResponse.<List<ReservationViewDTO>>builder()
                    .success(true)
                    .message("Reservations fetched successfully")
                    .data(reservations)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Reservations could not be fetched: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/reservation/add-reservation")
    public ResponseEntity<ApiResponse<?>> makereservation(@Valid @RequestBody AddReservationRequestDTO addReservationRequestDTO){
        try{
            AddReservationResponseDTO responseDTO = reservationService.saveReservation(addReservationRequestDTO);
            ApiResponse<AddReservationResponseDTO> response = ApiResponse.<AddReservationResponseDTO>builder()
                    .success(true)
                    .message("Reservation added successfully")
                    .data(responseDTO) 
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch(Exception e){
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Reservation could not be added: " + e.getMessage())
                .timestamp(Instant.now())
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
