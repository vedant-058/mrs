package com.mrs.mrs.controller;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mrs.mrs.DTO.SeatLock.ConfirmBookingRequestDTO;
import com.mrs.mrs.DTO.SeatLock.ReleaseSeatsRequestDTO;
import com.mrs.mrs.DTO.SeatLock.SeatLockRequestDTO;
import com.mrs.mrs.DTO.SeatLock.SeatLockViewDTO;
import com.mrs.mrs.DTO.Reservation.AddReservationRequestDTO;
import com.mrs.mrs.DTO.Reservation.AddReservationResponseDTO;
import com.mrs.mrs.DTO.SeatLock.SeatUpdateEvent;
import com.mrs.mrs.exception.InvalidRequestException;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.ReservationService;
import com.mrs.mrs.service.SeatLockService;
import org.springframework.beans.factory.annotation.Value;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/booking")
public class SeatLockController {

    @Autowired
    private SeatLockService seatLockService;
    @Autowired
    private ReservationService reservationService;

    @Value("${seat.lock.timeout.seconds}")
    private int LOCK_TIMEOUT_SECONDS;
    // Store SSE emitters by showtimeId
    private final Map<UUID, List<SseEmitter>> emittersByShowtime = new ConcurrentHashMap<>();

    /**
     * SSE endpoint for live seat status updates
     */
    @GetMapping(value = "/seat-status-stream/{showtimeId}", 
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSeatStatus(@PathVariable UUID showtimeId) {
        // Set timeout to 30 minutes (or Long.MAX_VALUE for no timeout)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 minutes
        
        // Add emitter to the list for this showtime
        emittersByShowtime.computeIfAbsent(showtimeId, k -> new CopyOnWriteArrayList<>())
            .add(emitter);
        
        log.info("New SSE client connected for showtime: {} (Total clients: {})", 
            showtimeId, emittersByShowtime.get(showtimeId).size());
        
        // Send initial seat state
        try {
            List<SeatLockViewDTO> availableSeats = seatLockService.getAvailableSeats(showtimeId);
            List<SeatLockViewDTO> bookedSeats = seatLockService.getBookedSeats(showtimeId);
            
            Map<String, Object> initialState = Map.of(
                "type", "initial-state",
                "availableSeats", availableSeats,
                "bookedSeats", bookedSeats
            );
            
            emitter.send(SseEmitter.event()
                .name("initial-state")
                .data(initialState));
                
            log.debug("Sent initial state to client for showtime: {}", showtimeId);
                
        } catch (IOException e) {
            log.error("Error sending initial state to SSE client for showtime: {}", showtimeId, e);
            emitter.completeWithError(e);
            removeEmitter(showtimeId, emitter);
        }
        
        // Handle cleanup - these are called automatically by Spring
        emitter.onCompletion(() -> {
            log.info("SSE client disconnected (completion) for showtime: {}", showtimeId);
            removeEmitter(showtimeId, emitter);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE client timeout for showtime: {}", showtimeId);
            emitter.complete();
            removeEmitter(showtimeId, emitter);
        });
        
        emitter.onError((ex) -> {
            // Suppress the expected AsyncRequestNotUsableException
            if (ex instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException) {
                log.debug("SSE client disconnected (expected): {}", ex.getMessage());
            } else {
                log.warn("SSE client error for showtime: {}", showtimeId, ex);
            }
            removeEmitter(showtimeId, emitter);
        });
        
        return emitter;
    }
    
    /**
     * ✅ Listen to SeatUpdateEvent and broadcast to SSE clients
     */
    @Async
    @EventListener
    public void handleSeatUpdateEvent(SeatUpdateEvent event) {
        try {
            if (event == null || event.getShowtimeId() == null) {
                log.warn("Received invalid seat update event: {}", event);
                return;
            }
            
            log.info("Broadcasting seat update: {} seats {} for showtime {}", 
                event.getSeatIds().size(), event.getEventType(), event.getShowtimeId());
                
            broadcastSeatUpdate(event.getShowtimeId(), event);
            
        } catch (Exception e) {
            log.error("Error handling seat update event", e);
        }
    }
    
    /**
     * Broadcast seat update to all connected clients
     */
    private void broadcastSeatUpdate(UUID showtimeId, SeatUpdateEvent event) {
        List<SseEmitter> emitters = emittersByShowtime.get(showtimeId);
        
        if (emitters == null || emitters.isEmpty()) {
            log.debug("No SSE clients connected for showtime: {}", showtimeId);
            return;
        }
        
        log.info("Broadcasting to {} SSE client(s) for showtime: {}", emitters.size(), showtimeId);
        
        List<SseEmitter> deadEmitters = new ArrayList<>();
        int successCount = 0;
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("seat-update")
                    .data(event));
                successCount++;
                
            } catch (IOException e) {
                log.debug("🔌 Failed to send to SSE client (client disconnected): {}", e.getMessage());
                deadEmitters.add(emitter);
                
                // Try to complete the emitter gracefully
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // Client already disconnected, ignore
                }
            } catch (IllegalStateException e) {
                // Emitter already completed
                log.debug("Emitter already completed: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        
        // Remove dead emitters
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.info("Removed {} dead emitter(s). Active clients: {}", 
                deadEmitters.size(), emitters.size());
        }
        
        log.info("Successfully sent update to {}/{} client(s)", successCount, successCount + deadEmitters.size());
    }
    
    /**
     * Remove emitter from the map
     */
    private void removeEmitter(UUID showtimeId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByShowtime.get(showtimeId);
        if (emitters != null) {
            emitters.remove(emitter);
            
            if (emitters.isEmpty()) {
                emittersByShowtime.remove(showtimeId);
                log.debug("Removed empty emitter list for showtime: {}", showtimeId);
            } else {
                log.debug("Remaining SSE clients for showtime {}: {}", showtimeId, emitters.size());
            }
        }
    }

    // ============= REST API Endpoints (unchanged) =============

    @PostMapping("/lock-seats")
    public ResponseEntity<ApiResponse<?>> lockSeats(@Valid @RequestBody SeatLockRequestDTO request) {
        try {
            List<SeatLockViewDTO> lockedSeats = seatLockService.lockSeats(request);
            ApiResponse<List<SeatLockViewDTO>> response = ApiResponse.<List<SeatLockViewDTO>>builder()
                    .success(true)
                    .message("Seats locked successfully. You have " + (LOCK_TIMEOUT_SECONDS / 60) + " minutes to complete payment.")
                    .data(lockedSeats)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (InvalidRequestException e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> confirmBooking(@RequestBody ConfirmBookingRequestDTO request) {
        try {
            int booked = seatLockService.confirmBooking(request);
            if (booked != 0) {
                AddReservationRequestDTO requestDTO = new AddReservationRequestDTO();
                requestDTO.setUserId(request.getUserId());
                requestDTO.setShowtimeId(request.getShowtimeId());
                requestDTO.setAmount(new java.math.BigDecimal(130));

                AddReservationResponseDTO reservation = reservationService.saveReservation(requestDTO);

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

    @GetMapping("/available-seats/{showtimeId}")
    public ResponseEntity<ApiResponse<?>> getAvailableSeats(@PathVariable("showtimeId") UUID showtimeId) {
        try {
            List<SeatLockViewDTO> availableSeats = seatLockService.getAvailableSeats(showtimeId);
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

    @DeleteMapping("delete-all-seats-by-showtimeId/{showtimeId}")
    public ResponseEntity<ApiResponse<?>> deleteBookedSeatsByShowtimeId(@PathVariable("showtimeId") UUID showtimeId) {
        try {
            String str = seatLockService.deleteBookedSeats(showtimeId);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Deleted seats by showtimeId")
                    .data(str)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Booked seats could not be deleted by showtimeId: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}