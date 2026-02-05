package com.mrs.mrs.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mrs.mrs.DTO.ReservationSeat.AddReservationSeatRequestDTO;
import com.mrs.mrs.DTO.ReservationSeat.AddReservationSeatResponseDTO;
import com.mrs.mrs.DTO.ReservationSeat.ReservationSeatViewDTO;
import com.mrs.mrs.exception.ResourceNotFoundException;
import com.mrs.mrs.model.Reservation;
import com.mrs.mrs.model.ReservationSeat;
import com.mrs.mrs.model.Seat;
import com.mrs.mrs.repository.ReservationRepository;
import com.mrs.mrs.repository.ReservationSeatRepository;
import com.mrs.mrs.repository.SeatRepository;

import jakarta.transaction.Transactional;

@Service
public class ReservationSeatService {
    private final ReservationSeatRepository reservationSeatRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public ReservationSeatService(ReservationSeatRepository reservationSeatRepository, 
                                 ReservationRepository reservationRepository, 
                                 SeatRepository seatRepository) {
        this.reservationSeatRepository = reservationSeatRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public AddReservationSeatResponseDTO saveReservationSeat(AddReservationSeatRequestDTO requestDTO) {
        // Fetch reservation
        Reservation reservation = reservationRepository.findById(requestDTO.getReservationId())
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", requestDTO.getReservationId().toString()));
        
        // Fetch seat
        Seat seat = seatRepository.findById(requestDTO.getSeatId())
            .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", requestDTO.getSeatId().toString()));
        
        // Create new reservation seat
        ReservationSeat newReservationSeat = new ReservationSeat();
        newReservationSeat.setReservation(reservation);
        newReservationSeat.setSeat(seat);
        newReservationSeat.setAmount(requestDTO.getAmount());
        
        // Save reservation seat
        ReservationSeat savedReservationSeat = reservationSeatRepository.save(newReservationSeat);
        
        // Return response DTO
        return new AddReservationSeatResponseDTO(
            savedReservationSeat.getId(),
            savedReservationSeat.getReservation().getId(),
            savedReservationSeat.getSeat().getId(),
            savedReservationSeat.getAmount()
        );
    }

    public List<ReservationSeatViewDTO> fetchReservationSeats() {
        List<ReservationSeat> reservationSeats = reservationSeatRepository.findAll();
        
        // Map entities to view DTOs and return directly
        return reservationSeats.stream()
            .map(this::mapToViewDTO)
            .collect(Collectors.toList());
    }
    
    private ReservationSeatViewDTO mapToViewDTO(ReservationSeat reservationSeat) {
        ReservationSeatViewDTO dto = new ReservationSeatViewDTO();
        dto.setId(reservationSeat.getId());
        dto.setReservationId(reservationSeat.getReservation().getId());
        dto.setSeatId(reservationSeat.getSeat().getId());
        // dto.setSeatRowNumber(reservationSeat.getSeat().getRowNumber());
        // dto.setSeatNumber(reservationSeat.getSeat().getSeatNumber());
        dto.setAmount(reservationSeat.getAmount());
        return dto;
    }
}
