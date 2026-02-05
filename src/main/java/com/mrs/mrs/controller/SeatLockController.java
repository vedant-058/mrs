package com.mrs.mrs.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.mrs.DTO.SeatLock.SeatLockRequestDTO;
import com.mrs.mrs.DTO.SeatLock.SeatLockViewDTO;
import com.mrs.mrs.DTO.Reservation.AddReservationRequestDTO;
import com.mrs.mrs.DTO.Reservation.AddReservationResponseDTO;
import com.mrs.mrs.DTO.SeatLock.ConfirmBookingRequestDTO;
import com.mrs.mrs.DTO.SeatLock.ReleaseSeatsRequestDTO;
import com.mrs.mrs.exception.InvalidRequestException;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.ReservationService;
import com.mrs.mrs.service.SeatLockService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/booking")
@AllArgsConstructor
public class SeatLockController {

    @Autowired
    private SeatLockService seatLockService;
    @Autowired
    private ReservationService reservationService;

    /**
     * Step 1: Lock seats (user selects seats)
     */
    @PostMapping("/lock-seats")
    public ResponseEntity<ApiResponse<?>> lockSeats(@Valid @RequestBody SeatLockRequestDTO request) {
        // System.out.println("Seat Lock controller, request: "+request.toString());
        try {
            List<SeatLockViewDTO> lockedSeats = seatLockService.lockSeats(request);
            // System.err.println("Locked seats: "+lockedSeats.toString());
            ApiResponse<List<SeatLockViewDTO>> response = ApiResponse.<List<SeatLockViewDTO>>builder()
                    .success(true)
                    .message("Seats locked successfully. You have 10 minutes to complete payment.")
                    .data(lockedSeats)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (InvalidRequestException e) {
            // Seat already booked or locked
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Step 2: Confirm booking (after successful payment)
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> confirmBooking(@RequestBody ConfirmBookingRequestDTO request) {
        try {
            // Confirm all seats

            int booked = seatLockService.confirmBooking(request);
            if (booked != 0) {
                // Now create the actual reservation
                AddReservationRequestDTO requestDTO = new AddReservationRequestDTO();
                requestDTO.setUserId(request.getUserId());
                requestDTO.setShowtimeId(request.getShowtimeId());
                requestDTO.setAmount(new BigDecimal(130));

                AddReservationResponseDTO reservation = reservationService.saveReservation(requestDTO);

                // seatLockService.confirmBooking(request);
                ApiResponse<AddReservationResponseDTO> response = ApiResponse.<AddReservationResponseDTO>builder()
                        .success(true)
                        .message("Booking confirmed!")
                        .data(reservation)
                        .timestamp(Instant.now())
                        .build();
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                throw new Exception("0 Seats booked");
            }

        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Step 3: Cancel/Release seats (user abandons booking)
     */
    @PostMapping("/release-seats")
    public ResponseEntity<ApiResponse<?>> releaseSeats(@RequestBody ReleaseSeatsRequestDTO request) {
        try {
            seatLockService.releaseSeat(request);

            ApiResponse<List<UUID>> response = ApiResponse.<List<UUID>>builder()
                    .success(true)
                    .message("Seats released")
                    .data(request.getSeatIds())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get available seats for a showtime
     */
    @GetMapping("/available-seats/{showtimeId}")
    public ResponseEntity<ApiResponse<?>> getAvailableSeats(@PathVariable("showtimeId") UUID showtimeId) {
        try {
            List<SeatLockViewDTO> availableSeats = seatLockService.getAvailableSeats(showtimeId);
            // returns seats only available after lock expired, not the prelocked seats
            ApiResponse<List<SeatLockViewDTO>> response = ApiResponse.<List<SeatLockViewDTO>>builder()
                    .success(true)
                    .message("Available seats")
                    .data(availableSeats)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/get-booked-seats-by-showtimeId/{showtimeId}")
    public ResponseEntity<ApiResponse<?>> getBookedSeatsByShowtimeId(@PathVariable("showtimeId") UUID showtimeId) {
        try {
            List<SeatLockViewDTO> bookedSeats = seatLockService.getBookedSeats(showtimeId);
            ApiResponse<List<SeatLockViewDTO>> response = ApiResponse.<List<SeatLockViewDTO>>builder()
                    .success(true)
                    .message("Booked seats by showtimeId")
                    .data(bookedSeats)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Booked seats could not be fetched by showtimeId: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    
    // /**
    // * Full seat map for a showtime (all seats + computed status).
    // */
    // @GetMapping("/seat-map/{showtimeId}")
    // public ResponseEntity<ApiResponse<?>> getSeatMap(@PathVariable UUID
    // showtimeId) {
    // try {
    // System.out.println("Seat map controller, showtimeId: "+showtimeId);
    // SeatMapResponseDTO seatMap = seatLockService.getSeatMap(showtimeId);
    // ApiResponse<SeatMapResponseDTO> response =
    // ApiResponse.<SeatMapResponseDTO>builder()
    // .success(true)
    // .message("Seat map")
    // .data(seatMap)
    // .timestamp(Instant.now())
    // .build();

    // return ResponseEntity.status(HttpStatus.OK).body(response);

    // } catch (Exception e) {
    // ApiResponse<?> errorResponse = ApiResponse.builder()
    // .success(false)
    // .message(e.getMessage())
    // .timestamp(Instant.now())
    // .build();
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    // }
    // }
}
