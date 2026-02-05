package com.mrs.mrs.DTO.Showtime;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeViewDTO {
    private UUID id;
    private UUID movieId;
    // private String movieName;
    private UUID screenId;
    // private String screenName;
    private Instant showtime;
}
