package com.mrs.mrs.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.mrs.DTO.Genre.AddGenreRequestDTO;
import com.mrs.mrs.DTO.Genre.AddGenreResponseDTO;
import com.mrs.mrs.DTO.Genre.GetGenreResponseDTO;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.GenreService;
// impetus
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class GenreController {

    @Autowired
    private GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/genre/get-genre")
    public ResponseEntity<ApiResponse<?>> getgenre(){
        try {
            GetGenreResponseDTO responseDTO = genreService.fetchgenre();

            ApiResponse<GetGenreResponseDTO> response = ApiResponse.<GetGenreResponseDTO>builder()
                    .success(true)
                    .message("Genres fetched successfully")
                    .data(responseDTO)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Genre could not be fetched: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/genre/add-genre")
    public ResponseEntity<ApiResponse<?>> addgenre(@Valid @RequestBody AddGenreRequestDTO genreRequestDTO){
        try {
            AddGenreResponseDTO responseDTO = genreService.savegenre(genreRequestDTO);

            ApiResponse<AddGenreResponseDTO> response = ApiResponse.<AddGenreResponseDTO>builder()
                    .success(true)
                    .message("Genre added successfully")
                    .data(responseDTO) 
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Genre could not be added: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        
    }

}
