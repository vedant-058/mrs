package com.mrs.mrs.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mrs.mrs.DTO.Seat.AddSeatRequestDTO;
import com.mrs.mrs.DTO.Seat.AddSeatResponseDTO;
import com.mrs.mrs.DTO.Seat.SeatViewDTO;
import com.mrs.mrs.exception.ResourceAlreadyExistsException;
import com.mrs.mrs.exception.ResourceNotFoundException;
import com.mrs.mrs.model.Screen;
import com.mrs.mrs.model.Seat;
import com.mrs.mrs.repository.ScreenRepository;
import com.mrs.mrs.repository.SeatRepository;

import jakarta.transaction.Transactional;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;    

    public SeatService(SeatRepository SeatRepository, ScreenRepository screenRepository){
        this.seatRepository = SeatRepository;
        this.screenRepository=screenRepository;
    }

    public List<SeatViewDTO> fetchSeats() {
        List<Seat> seats = seatRepository.findAll();
        
        // Map entities to view DTOs and return directly
        return seats.stream()
            .map(this::mapToViewDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public AddSeatResponseDTO saveseat(AddSeatRequestDTO requestDTO){
        Screen screen = screenRepository.findById(requestDTO.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "ScreenId", requestDTO.getScreenId()));
        
        if(seatRepository.existsByScreenIdAndRowNumberAndSeatNumber(
            screen.getId(), 
            requestDTO.getRow_number(), 
            requestDTO.getSeat_number()
        ))
            throw new ResourceAlreadyExistsException("Seat", "Seat details", requestDTO.getRow_number()+requestDTO.getSeat_number()+" screenId: "+requestDTO.getScreenId());

        Seat seat = new Seat();
        seat.setScreen(screen);
        seat.setRowNumber(requestDTO.getRow_number());
        seat.setSeatNumber(requestDTO.getSeat_number());
        seat.setXPos(requestDTO.getXPos());
        seat.setYPos(requestDTO.getYPos());

        Seat savedSeat = seatRepository.save(seat);

        return new AddSeatResponseDTO(
            savedSeat.getId(),
            savedSeat.getScreen().getId(),
            savedSeat.getRowNumber(),
            savedSeat.getSeatNumber(),
            savedSeat.getXPos(),
            savedSeat.getYPos()
        );
    }
    
    private SeatViewDTO mapToViewDTO(Seat seat) {
        SeatViewDTO dto = new SeatViewDTO();
        dto.setId(seat.getId());
        dto.setScreenId(seat.getScreen().getId());
        dto.setRowNumber(seat.getRowNumber());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setXPos(seat.getXPos());
        dto.setYPos(seat.getYPos());
        return dto;
    }

    public List<SeatViewDTO> fetchSeatsByScreenId(String screenId) {
       
        try {
            Screen screen = screenRepository.findById(UUID.fromString(screenId))
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "ScreenId", UUID.fromString(screenId)));
            List<SeatViewDTO> seats = seatRepository.findByScreen_Id(screen.getId());
            return seats;
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("Screen", "ScreenId", screenId);
        }
    }

}
