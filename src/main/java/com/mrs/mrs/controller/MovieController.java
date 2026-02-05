package com.mrs.mrs.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.mrs.DTO.Movie.AddMovieRequestDTO;
import com.mrs.mrs.DTO.Movie.AddMovieResponseDTO;
import com.mrs.mrs.DTO.Movie.MovieViewDTO;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.MovieService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class MovieController {

    @Autowired
    MovieService movieService;

    public MovieController(MovieService movieService){
        this.movieService = movieService;
    }

    @GetMapping("/movies/get-movies")
    public ResponseEntity<ApiResponse<?>> getgenre(){
        try {
            List<MovieViewDTO> responseDTO = movieService.fetchMovies();

            ApiResponse<List<MovieViewDTO>> response = ApiResponse.<List<MovieViewDTO>>builder()
                    .success(true)
                    .message("Movies fetched successfully")
                    .data(responseDTO)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Movies could not be fetched: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/movies/add-movie")
    public ResponseEntity<ApiResponse<?>> addmovie(@Valid @RequestBody AddMovieRequestDTO requestDTO){
        try {
            AddMovieResponseDTO responseDTO = movieService.savemovie(requestDTO);
            
            ApiResponse<AddMovieResponseDTO> response = ApiResponse.<AddMovieResponseDTO>builder()
                    .success(true)
                    .message("Movie added successfully")
                    .data(responseDTO) 
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Movie could not be added: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}