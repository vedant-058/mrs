package com.mrs.mrs.DTO.Showtime;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor  
public class AddShowtimeRequestDTO {

    @NotBlank(message = "Movie ID is required")
    private UUID movieId;

    @NotBlank(message = "Screen ID is required")
    private UUID screenId;

    private Instant showtime;
}