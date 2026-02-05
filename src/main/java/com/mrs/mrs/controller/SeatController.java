package com.mrs.mrs.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.mrs.DTO.Seat.AddSeatRequestDTO;
import com.mrs.mrs.DTO.Seat.AddSeatResponseDTO;
import com.mrs.mrs.DTO.Seat.SeatViewDTO;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.SeatService;

import jakarta.validation.Valid;

@RestController
public class SeatController {

    @Autowired
    private SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/seat/get-seats")
    public ResponseEntity<ApiResponse<?>> getseats() {
        try {
            List<SeatViewDTO> seats = seatService.fetchSeats();

            ApiResponse<List<SeatViewDTO>> response = ApiResponse.<List<SeatViewDTO>>builder()
                    .success(true)
                    .message("Seats fetched successfully")
                    .data(seats)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Seats could not be fetched: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/seat/add-seat")
    public ResponseEntity<ApiResponse<?>> addSeat(@Valid @RequestBody AddSeatRequestDTO requestDTO) {
        try {
            AddSeatResponseDTO responseDTO = seatService.saveseat(requestDTO);
            ApiResponse<AddSeatResponseDTO> response = ApiResponse.<AddSeatResponseDTO>builder()
                    .success(true)
                    .message("Seat added successfully")
                    .data(responseDTO)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Seat could not be added: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/seat/get-seats/{screenId}")
    public ResponseEntity<ApiResponse<?>> getseatsbyscreenid(@PathVariable("screenId") UUID screenId) {
        List<SeatViewDTO> seats = seatService
                .fetchSeatsByScreenId(screenId.toString() == null ? null : screenId.toString());
        ApiResponse<List<SeatViewDTO>> response = ApiResponse.<List<SeatViewDTO>>builder()
                .success(true)
                .message("Seats fetched successfully")
                .data(seats)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
