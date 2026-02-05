package com.mrs.mrs.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mrs.mrs.DTO.Reservation.AddReservationRequestDTO;
import com.mrs.mrs.DTO.Reservation.AddReservationResponseDTO;
import com.mrs.mrs.DTO.Reservation.ReservationViewDTO;
import com.mrs.mrs.exception.ResourceAlreadyExistsException;
import com.mrs.mrs.exception.ResourceNotFoundException;
import com.mrs.mrs.model.Reservation;
import com.mrs.mrs.model.Showtime;
import com.mrs.mrs.model.User;
import com.mrs.mrs.repository.ReservationRepository;
import com.mrs.mrs.repository.ShowtimeRepository;
import com.mrs.mrs.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;

    public ReservationService(ReservationRepository reservationRepository, 
                            UserRepository userRepository, 
                            ShowtimeRepository showtimeRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.showtimeRepository = showtimeRepository;
    }

    
    @Transactional
    public AddReservationResponseDTO saveReservation(AddReservationRequestDTO requestDTO) {
        User user = null;
        Showtime showtime = null;
        try{
            // Fetch user
            user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestDTO.getUserId()));
        }catch(ResourceNotFoundException e){
            new ResourceNotFoundException("User", "id", requestDTO.getUserId());
        } 
        try{
            // Fetch showtime
            showtime = showtimeRepository.findById(requestDTO.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", requestDTO.getShowtimeId()));
        } catch(ResourceAlreadyExistsException e){
            throw new ResourceAlreadyExistsException("User or Showtime", "UserId or ShowtimeId does not exist", "ShowtimeId: "+requestDTO.getShowtimeId()+"UserId: "+requestDTO.getUserId());
        }
        // try{    
        //     if(reservationRepository.existsByUserIdAndShowtimeId(requestDTO.getUserId(),requestDTO.getShowtimeId())){
        //         // System.out.println("Found prexisting entries");
        //         // implement the details
        //         // ✅ repeated entries for same user & same showtime & different seat are allowed.
        //         // ❌ repeated entries for same user & same showtime & same seat not allowed.
        //         throw new ResourceAlreadyExistsException("Reservation", "Seat", "User already booked for showtime");
        //     }
        // } catch(ResourceAlreadyExistsException e){
        //     throw new ResourceAlreadyExistsException("Reservation", "Seat", "User already booked for showtime");
        // }
            // Create new reservation
            Reservation newReservation = new Reservation();
            newReservation.setUser(user);
            newReservation.setShowtime(showtime);
            newReservation.setAmount(requestDTO.getAmount());
            
            // Save reservation
            Reservation savedReservation = reservationRepository.save(newReservation);
            
            // Return response DTO
            return new AddReservationResponseDTO(
                savedReservation.getId(),
                savedReservation.getUser().getId(),
                savedReservation.getShowtime().getId(),
                savedReservation.getAmount(),
                savedReservation.getTimestamp()
            );
        

        
    }

    public List<ReservationViewDTO> fetchReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        
        // Map entities to view DTOs and return directly
        return reservations.stream()
            .map(this::mapToViewDTO)
            .collect(Collectors.toList());
    }
    
    private ReservationViewDTO mapToViewDTO(Reservation reservation) {
        ReservationViewDTO dto = new ReservationViewDTO();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getUser().getId());
        // dto.setUserName(reservation.getUser().getName());
        dto.setShowtimeId(reservation.getShowtime().getId());
        dto.setAmount(reservation.getAmount());
        dto.setTimestamp(reservation.getTimestamp());
        return dto;
    }
}
