package com.mrs.mrs.DTO.Movie;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.mrs.mrs.model.Genre;
import com.mrs.mrs.model.Movie;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieViewDTO {
    private UUID id;
    private String movie;
    // private String userName;
    private UUID genreId;
    private Integer rating;
    // private Instant timestamp;
}
