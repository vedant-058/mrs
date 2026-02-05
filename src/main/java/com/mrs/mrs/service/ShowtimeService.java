package com.mrs.mrs.service;

import com.mrs.mrs.DTO.Seat.SeatViewDTO;
import com.mrs.mrs.DTO.Showtime.AddShowtimeRequestDTO;
import com.mrs.mrs.DTO.Showtime.AddShowtimeResponseDTO;
import com.mrs.mrs.DTO.Showtime.ShowtimeViewDTO;
import com.mrs.mrs.exception.InvalidRequestException;
import com.mrs.mrs.exception.ResourceNotFoundException;
import com.mrs.mrs.model.Movie;
import com.mrs.mrs.model.Screen;
import com.mrs.mrs.model.Showtime;
import com.mrs.mrs.repository.MovieRepository;
import com.mrs.mrs.repository.ScreenRepository;
import com.mrs.mrs.repository.SeatRepository;
import com.mrs.mrs.repository.ShowtimeRepository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShowtimeService {
    
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private SeatRepository seatRepository;

    @Transactional
    public AddShowtimeResponseDTO saveShowtime(AddShowtimeRequestDTO requestDTO){
        try {
            // Validate that Movie exists
            Movie movie = null;
            Screen screen = null;
            try {
                movie = movieRepository.findById(requestDTO.getMovieId())
                    .orElseThrow(() -> new RuntimeException("Movie ID not found"));
                    
                screen = screenRepository.findById(requestDTO.getScreenId())
                    .orElseThrow(() -> new RuntimeException("Screen ID not found"));
                    
            } catch (RuntimeException e) {
                // This catches both Movie and Screen failures
                throw new ResourceNotFoundException("Showtime Setup", "IDs", "Missing Movie or Screen: " + e.getMessage());
            }
            // Check if screen already has a showtime at this time
            if (requestDTO.getShowtime() != null) {
                var existingShowtime = showtimeRepository.findByScreenIdAndShowtime(
                    requestDTO.getScreenId(), 
                    requestDTO.getShowtime()
                );
                try{
                    if (existingShowtime.isPresent()) {
                        throw new InvalidRequestException("Showtime", 
                            String.format("Screen '%s' already has a showtime scheduled at %s", 
                                screen.getName(), requestDTO.getShowtime()));
                    }
                }
                catch (InvalidRequestException e) {
                    // Re-throw ResourceNotFoundException - it will be handled by the controller
                    throw new InvalidRequestException(e.getMessage());
                } 
            }
            
            // Create and save the new showtime
            Showtime newShowtime = new Showtime();
            newShowtime.setMovie(movie);
            newShowtime.setScreen(screen);
            newShowtime.setShowtime(requestDTO.getShowtime());
            
            Showtime savedShowtime = showtimeRepository.save(newShowtime);
            return new AddShowtimeResponseDTO(
                savedShowtime.getId(),
                savedShowtime.getMovie().getId(), 
                savedShowtime.getScreen().getId(), 
                savedShowtime.getShowtime()
            );
        }
        
         catch (DataIntegrityViolationException e) {
            // Handle database constraint violations as a fallback (shouldn't reach here if validation works)
            throw new InvalidRequestException("Showtime", 
                "Unable to create showtime. This may be due to a conflict (e.g., screen already has a showtime at this time) or invalid data.");
        } catch (Exception e) {
            // Handle any other unexpected errors
            throw new InvalidRequestException("Showtime", 
                "Failed to link movie and screen: " + e.getMessage());
        }
    }

    public List<ShowtimeViewDTO> fetchShowtimes() {
        List<Showtime> showtimes = showtimeRepository.findAll();
        
        // Map entities to view DTOs and return directly
        return showtimes.stream()
            .map(this::mapToViewDTO)
            .collect(Collectors.toList());
    }
    
    private ShowtimeViewDTO mapToViewDTO(Showtime showtime) {
        ShowtimeViewDTO dto = new ShowtimeViewDTO();
        dto.setId(showtime.getId());
        dto.setMovieId(showtime.getMovie().getId());
        // dto.setMovieName(showtime.getMovie().getName());
        dto.setScreenId(showtime.getScreen().getId());
        // dto.setScreenName(showtime.getScreen().getName());
        dto.setShowtime(showtime.getShowtime());
        return dto;
    }

    public List<SeatViewDTO> fetchSeatsviaShowtimeId(UUID showtimeId) {
        try {
            Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow(
                () -> new ResourceNotFoundException("Showtime", "id", showtimeId.toString()));
            List<SeatViewDTO> seats = seatRepository.findByScreen_Id(showtime.getScreen().getId());
            return seats;
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Showtime", "id", showtimeId.toString());
        }
    }
        
}