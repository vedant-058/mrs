package com.mrs.mrs.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.mrs.mrs.DTO.SeatLock.ConfirmBookingRequestDTO;
import com.mrs.mrs.DTO.SeatLock.ReleaseSeatsRequestDTO;
import com.mrs.mrs.DTO.SeatLock.SeatLockRequestDTO;
import com.mrs.mrs.DTO.SeatLock.SeatLockViewDTO;
import com.mrs.mrs.DTO.SeatLock.SeatUpdateEvent;
import com.mrs.mrs.exception.InvalidRequestException;
import com.mrs.mrs.exception.PastShowtimeBookingException;
import com.mrs.mrs.exception.ResourceAlreadyExistsException;
import com.mrs.mrs.exception.ResourceNotFoundException;
import com.mrs.mrs.model.Seat;
import com.mrs.mrs.model.SeatLock;
import com.mrs.mrs.model.SeatLock.SeatStatus;
import com.mrs.mrs.model.Showtime;
import com.mrs.mrs.model.User;
import com.mrs.mrs.repository.SeatLockRepository;
import com.mrs.mrs.repository.SeatRepository;
import com.mrs.mrs.repository.ShowtimeRepository;
import com.mrs.mrs.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SeatLockService {
    @Autowired
    private SeatLockRepository seatLockRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Value("${seat.lock.timeout.seconds}")
    private int LOCK_TIMEOUT_SECONDS;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<SeatLockViewDTO> lockSeats(SeatLockRequestDTO request) {
        try{
            Showtime s = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", request.getShowtimeId()));

            if (s.getShowtime().isBefore(Instant.now())) {
                throw new InvalidRequestException("Booking for past showtimes is not allowed"+s.getShowtime());
            }
            
            List<SeatLockViewDTO> lockedSeats = new ArrayList<>();
            Set<UUID> uniqueSeatIds = new LinkedHashSet<>(request.getSeatIds());

            for (UUID seatId : uniqueSeatIds) {
                int attempt = 0;
                boolean success = false;
                
                while (attempt < 3 && !success) {
                    try {
                        SeatLockViewDTO lock = attemptLockSeat(request.getShowtimeId(), seatId, request.getUserId());
                        lockedSeats.add(lock);
                        success = true;
                    } catch (ObjectOptimisticLockingFailureException e) {
                        attempt++;
                        if (attempt >= 3) throw new InvalidRequestException("Concurrency", "Too many attempts");
                        handleRetryBackoff(attempt);
                    }
                }
            }
            
            // Publish event instead of calling controller directly
            if (!lockedSeats.isEmpty()) {
                SeatUpdateEvent event = new SeatUpdateEvent(
                    request.getSeatIds(),
                    request.getShowtimeId(),
                    SeatStatus.LOCKED,
                    "LOCKED"
                );
                eventPublisher.publishEvent(event);
            }
            
            return lockedSeats;
        } catch (Exception e) {
            throw new InvalidRequestException("Seat", "Seat could not be locked: "+e.getMessage());
        }
    }
    private void handleRetryBackoff(int attempt) {
        try {
            Thread.sleep(100 * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private SeatLockViewDTO attemptLockSeat(UUID showtimeId, UUID seatId, UUID userId) {
        Instant now = Instant.now();
        
        // Validate entities exist
        Showtime showtime = showtimeRepository.findById(showtimeId)
            .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", showtimeId.toString()));
        
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId.toString()));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        // Check if seat is already unavailable
        if (seatLockRepository.isSeatUnavailable(showtimeId, seatId, now)) {
            throw new InvalidRequestException("Seat", "Seat is already booked or locked by another user");
        }

        // Try to find existing lock record
        SeatLock seatLock = seatLockRepository.findByShowtimeIdAndSeatId(showtimeId, seatId)
            .orElse(null);

        if (seatLock == null) {
            // Create new lock record
            seatLock = new SeatLock();
            seatLock.setShowtime(showtime);
            seatLock.setSeat(seat);
        } else if (seatLock.getStatus() == SeatStatus.BOOKED) {
            throw new InvalidRequestException("Seat", "Seat is already permanently booked");
        } else if (seatLock.getStatus() == SeatStatus.LOCKED && 
                   seatLock.getExpiresAt() != null && 
                   seatLock.getExpiresAt().isAfter(now)) {
            throw new InvalidRequestException("Seat", "Seat is temporarily locked by another user");
        }

        // Lock the seat
        seatLock.setUser(user);
        seatLock.setStatus(SeatStatus.LOCKED);
        seatLock.setLockedAt(now);
        // seatLock.setExpiresAt(now.plus(LOCK_TIMEOUT_MINUTES, ChronoUnit.MINUTES));
        seatLock.setExpiresAt(now.plus(LOCK_TIMEOUT_SECONDS, ChronoUnit.SECONDS));

        SeatLock savedseatlock = seatLockRepository.save(seatLock);
        SeatLockViewDTO seatLockViewDTO = new SeatLockViewDTO();
        seatLockViewDTO.setSeatId(seatId);
        seatLockViewDTO.setShowtimeId(showtimeId);
        seatLockViewDTO.setStatus(savedseatlock.getStatus());
        seatLockViewDTO.setUserId(userId);
        return seatLockViewDTO;
    }

    /**
     * Confirm booking (after payment)
     */
    @Transactional
    public int confirmBooking(ConfirmBookingRequestDTO confirmBookingRequestDTO) {
        //  how to handle the already booked seat in the request
        try{  
            for(UUID seatId:confirmBookingRequestDTO.getSeatIds()){
                Instant now = Instant.now();
                SeatLock seatLock = seatLockRepository.findByShowtimeIdAndSeatId(confirmBookingRequestDTO.getShowtimeId(), seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("SeatLock", "showtimeId-seatId", 
                    confirmBookingRequestDTO.getShowtimeId() + "-" + seatId));
                // System.out.println("Found showtimeid and seatid");
                
                try{
                    if (seatLock.getStatus()==SeatStatus.BOOKED){
                        // System.out.println("This seat is already booked for the given showtime");
                        throw new ResourceAlreadyExistsException("Seat", "Seat-id", "Seat already booked.");
                    }
                } catch (Exception e) {
                    throw new ResourceAlreadyExistsException("Seat", "Seat-id", "Seat already booked "+seatId);
                }

                // Verify user owns this lock
                if (!seatLock.getUser().getId().equals(confirmBookingRequestDTO.getUserId())) {
                    throw new InvalidRequestException("Seat", "Different user locked the seat and another user tries to book the seat.");
                }
                // System.out.println("Verify user owns this lock");
                
                // Check if showtime is in the past
                Showtime s = showtimeRepository.findById(confirmBookingRequestDTO.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", confirmBookingRequestDTO.getShowtimeId()));
                // System.out.println("Check if showtime is in the past");

                try {
                    if (s.getShowtime().isBefore(Instant.now())) {
                        throw new PastShowtimeBookingException("Booking for past showtimes is not allowed"+s.getShowtime());
                    }
                    // System.out.println("PastShowtimeBookingException"); 
                } catch (PastShowtimeBookingException e) {

                    throw new PastShowtimeBookingException("Booking for past showtimes is not allowed"+s.getShowtime());
                }
                
                // Verify lock hasn't expired
                if (seatLock.getStatus() == SeatStatus.LOCKED && 
                seatLock.getExpiresAt() != null && 
                seatLock.getExpiresAt().isBefore(now)) {
                    throw new InvalidRequestException("Seat", "Your seat lock has expired. Please select seats again.");
                }
                // System.out.println("Verify lock hasn't expired");
                
                // // Permanently book the seat
                // seatLock.setStatus(SeatStatus.BOOKED);
                // seatLock.setExpiresAt(null); // No expiration for booked seats
                // seatLockRepository.save(seatLock);
                // System.out.println("Permanently book the seat");
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        int booked = seatLockRepository.confirmSeatsBulk(
            confirmBookingRequestDTO.getSeatIds(),
            confirmBookingRequestDTO.getShowtimeId(), 
            confirmBookingRequestDTO.getUserId(),
            Instant.now()
        );
        
        // Publish event
        if (booked > 0) {
            SeatUpdateEvent event = new SeatUpdateEvent(
                confirmBookingRequestDTO.getSeatIds(),
                confirmBookingRequestDTO.getShowtimeId(),
                SeatStatus.BOOKED,
                "BOOKED"
            );
            eventPublisher.publishEvent(event);
        }
        
        return booked;
    }

    
    /**
     * Release seat lock (user cancels or timeout)
     */
    @Transactional
    public void releaseSeat(ReleaseSeatsRequestDTO releaseSeatsRequestDTO) {
        for(UUID seatId : releaseSeatsRequestDTO.getSeatIds()){
            SeatLock seatLock = seatLockRepository.findByShowtimeIdAndSeatId(
                releaseSeatsRequestDTO.getShowtimeId(), seatId)
                .orElseThrow(() -> new ResourceNotFoundException("SeatLock", "showtimeId-seatId", 
                releaseSeatsRequestDTO.getShowtimeId() + "-" + seatId));
            
            if(seatLock.getStatus() == SeatStatus.BOOKED){
                throw new InvalidRequestException("SeatId","Seats already booked, cannot be released");
            }
            
            if (!seatLock.getUser().getId().equals(releaseSeatsRequestDTO.getUserId())) {
                throw new InvalidRequestException("Seat", "Seats locked by other user");
            }

            if (seatLock.getStatus() == SeatStatus.LOCKED) {
                seatLock.setStatus(SeatStatus.AVAILABLE);
                seatLock.setLockedAt(null);
                seatLock.setExpiresAt(null);
                seatLockRepository.save(seatLock);
            }   
        }
        
        // Publish event
        SeatUpdateEvent event = new SeatUpdateEvent(
            releaseSeatsRequestDTO.getSeatIds(),
            releaseSeatsRequestDTO.getShowtimeId(),
            SeatStatus.AVAILABLE,
            "RELEASED"
        );
        eventPublisher.publishEvent(event);
    }
    /**
     * Scheduled job to release expired locks
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredLocks() {
        Instant now = Instant.now();
        
        // Get expired locks before releasing them
        List<SeatLock> expiredLocks = seatLockRepository.findExpiredLocks(now);
        
        int releasedCount = seatLockRepository.releaseExpiredLocks(now);
        
        if (releasedCount > 0) {
            log.info("Released {} expired seat locks", releasedCount);
            
            // Group by showtime and publish events
            Map<UUID, List<UUID>> seatsByShowtime = expiredLocks.stream()
                .collect(Collectors.groupingBy(
                    sl -> sl.getShowtime().getId(),
                    Collectors.mapping(sl -> sl.getSeat().getId(), Collectors.toList())
                ));
            
            for (Map.Entry<UUID, List<UUID>> entry : seatsByShowtime.entrySet()) {
                SeatUpdateEvent event = new SeatUpdateEvent(
                    entry.getValue(),
                    entry.getKey(),
                    SeatStatus.AVAILABLE,
                    "EXPIRED"
                );
                eventPublisher.publishEvent(event);
            }
        }
    }

    /**
     * Get available seats for a showtime
     */
    @Transactional(readOnly = true)
    public List<SeatLockViewDTO> getAvailableSeats(UUID showtimeId) {
        List<SeatLock> seatLocks = seatLockRepository.findByShowtimeIdAndStatus(showtimeId, SeatStatus.AVAILABLE);
        return seatLocks.stream()
            .map(seatLock -> {
                SeatLockViewDTO dto = new SeatLockViewDTO();
                dto.setShowtimeId(seatLock.getShowtime().getId());
                dto.setSeatId(seatLock.getSeat().getId());
                dto.setStatus(seatLock.getStatus());
                // Use a ternary check to avoid NullPointerException if no user is assigned
                // dto.setUserId(seatLock.getUser() != null ? seatLock.getUser().getId() : null);
                
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<SeatLockViewDTO> getBookedSeats(UUID showtimeId) {
        List<SeatLock> seatLocks = seatLockRepository.findByShowtimeIdAndStatus(showtimeId, SeatStatus.BOOKED);
        // List<SeatLock> seatLocks = seatLockRepository.findByShowtimeIdAndStatus(showtimeId, SeatStatus.BOOKED);
        return seatLocks.stream()
            .map(seatLock -> {
                SeatLockViewDTO dto = new SeatLockViewDTO();
                dto.setShowtimeId(seatLock.getShowtime().getId());
                dto.setSeatId(seatLock.getSeat().getId());
                dto.setStatus(seatLock.getStatus());
                dto.setUserId(seatLock.getUser() != null ? seatLock.getUser().getId() : null); 
                return dto;
            })
            .collect(Collectors.toList());
    }
    public String deleteBookedSeats(UUID showtimeId) {
        int rows = seatLockRepository.deleteByShowtimeId(showtimeId);
        return rows + "Deleted";
    }
}
