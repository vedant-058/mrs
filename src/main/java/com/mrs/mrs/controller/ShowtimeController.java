package com.mrs.mrs.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.mrs.DTO.Seat.SeatViewDTO;
import com.mrs.mrs.DTO.Showtime.AddShowtimeRequestDTO;
import com.mrs.mrs.DTO.Showtime.AddShowtimeResponseDTO;
import com.mrs.mrs.DTO.Showtime.ShowtimeViewDTO;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.ShowtimeService;

import jakarta.validation.Valid;

@RestController
public class ShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping("/showtime/get-showtimes")
    public ResponseEntity<ApiResponse<?>> getshowtimes() {
        try {
            List<ShowtimeViewDTO> showtimes = showtimeService.fetchShowtimes();

            ApiResponse<List<ShowtimeViewDTO>> response = ApiResponse.<List<ShowtimeViewDTO>>builder()
                    .success(true)
                    .message("Showtimes fetched successfully")
                    .data(showtimes)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Showtimes could not be fetched: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/showtime/add-showtime")
    public ResponseEntity<ApiResponse<?>> addshowtime(@Valid @RequestBody AddShowtimeRequestDTO requestDTO) {
        try {
            AddShowtimeResponseDTO responseDTO = showtimeService.saveShowtime(requestDTO);
            ApiResponse<AddShowtimeResponseDTO> response = ApiResponse.<AddShowtimeResponseDTO>builder()
                    .success(true)
                    .message("Showtime added successfully")
                    .data(responseDTO)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Showtime could not be added: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/showtime/get-seats/{showtimeId}")
    public ResponseEntity<ApiResponse<?>> fetechseats(@PathVariable("showtimeId") UUID showtimeId) {
        try {
            List<SeatViewDTO> seats = showtimeService.fetchSeatsviaShowtimeId(showtimeId);
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
                    .message("Seats could not be fetched via showtimeId: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
