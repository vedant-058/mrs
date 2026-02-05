package com.mrs.mrs.DTO.Movie;

import java.util.UUID;

import com.mrs.mrs.model.Genre;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddMovieResponseDTO {
    private UUID id;
    private String name;
    private UUID genre;
}
