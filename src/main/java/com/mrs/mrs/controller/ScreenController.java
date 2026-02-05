package com.mrs.mrs.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.mrs.DTO.Screen.AddScreenRequestDTO;
import com.mrs.mrs.DTO.Screen.AddScreenResponseDTO;
import com.mrs.mrs.DTO.Screen.ScreenViewDTO;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.ScreenService;

import jakarta.validation.Valid;

@RestController
public class ScreenController {
    
    @Autowired
    private ScreenService screenService;

    public ScreenController(ScreenService screenService){
        this.screenService = screenService;
    }

    @GetMapping("/screen/get-screens")
    public ResponseEntity<ApiResponse<?>> getscreen(){
        try {
            List<ScreenViewDTO> screens = screenService.fetchScreens();

            ApiResponse<List<ScreenViewDTO>> response = ApiResponse.<List<ScreenViewDTO>>builder()
                    .success(true)
                    .message("Screen fetched successfully")
                    .data(screens)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Screen could not be fetched: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/screen/add-screen")
    public ResponseEntity<ApiResponse<?>> addscreen(@Valid @RequestBody AddScreenRequestDTO requestDTO){
        try {
            AddScreenResponseDTO responseDTO = screenService.saveScreen(requestDTO);
            ApiResponse<AddScreenResponseDTO> response = ApiResponse.<AddScreenResponseDTO>builder()
                    .success(true)
                    .message("Screen added successfully")
                    .data(responseDTO) 
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Screen could not be added: " + e.getMessage())
                .timestamp(Instant.now())
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
