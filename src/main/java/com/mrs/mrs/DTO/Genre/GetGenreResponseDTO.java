package com.mrs.mrs.DTO.Genre;

import java.util.List;

import com.mrs.mrs.model.Genre;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetGenreResponseDTO {
    private List<Genre> genre;
}
