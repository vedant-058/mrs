package com.mrs.mrs.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mrs.mrs.DTO.Genre.AddGenreRequestDTO;
import com.mrs.mrs.DTO.Genre.AddGenreResponseDTO;
import com.mrs.mrs.DTO.Genre.GetGenreResponseDTO;
import com.mrs.mrs.exception.ResourceAlreadyExistsException;
import com.mrs.mrs.model.Genre;
import com.mrs.mrs.repository.GenreRepository;

import jakarta.transaction.Transactional;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository){
        this.genreRepository=genreRepository;
    }

    @Transactional
    public AddGenreResponseDTO savegenre(AddGenreRequestDTO genreRequestDTO){
        Genre newGenre = new Genre();

        if (genreRepository.existsByGenre(genreRequestDTO.getGenre())) {
            throw new ResourceAlreadyExistsException("Genre", "Genre Value", genreRequestDTO.getGenre());
        }

        newGenre.setGenre(genreRequestDTO.getGenre()); 
        
        Genre savedGenre = genreRepository.save(newGenre);
        return new AddGenreResponseDTO(savedGenre.getGenre());
    }

    public GetGenreResponseDTO fetchgenre() {
        List<Genre> list = genreRepository.findAll();
        GetGenreResponseDTO getGenreResponseDTO = new GetGenreResponseDTO();
        getGenreResponseDTO.setGenre(list);
        return getGenreResponseDTO;
    }
}