package com.mrs.mrs.DTO.Showtime;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddShowtimeResponseDTO {
    private UUID id;
    private UUID movieId;
    private UUID screenId;
    private Instant showtime;
}
