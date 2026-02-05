package com.mrs.mrs.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mrs.mrs.DTO.Movie.AddMovieRequestDTO;
import com.mrs.mrs.DTO.Movie.AddMovieResponseDTO;
import com.mrs.mrs.DTO.Movie.MovieViewDTO;
import com.mrs.mrs.exception.ResourceAlreadyExistsException;
import com.mrs.mrs.exception.ResourceNotFoundException;
import com.mrs.mrs.model.Genre;
import com.mrs.mrs.model.Movie;
import com.mrs.mrs.repository.GenreRepository;
import com.mrs.mrs.repository.MovieRepository;

import jakarta.transaction.Transactional;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    public MovieService(MovieRepository movieRepository,GenreRepository genreRepository){
        this.movieRepository=movieRepository;
        this.genreRepository=genreRepository;
    }

    @Transactional
    public AddMovieResponseDTO savemovie(AddMovieRequestDTO requestDTO) {
        try {
            // Check if movie with same name already exists
            if (movieRepository.existsByName(requestDTO.getName())) {
                throw new ResourceAlreadyExistsException("Movie", "Name", requestDTO.getName());
            }

            // Check if genre exists, if not throw exception
            if (!genreRepository.existsByGenre(requestDTO.getGenre())) {
                throw new ResourceNotFoundException("Genre", "Genre Name", requestDTO.getGenre());
            }

            // Find the genre by name
            Genre genre = genreRepository.findByGenre(requestDTO.getGenre());
            if (genre == null) {
                throw new ResourceNotFoundException("Genre", "Genre Name", requestDTO.getGenre());
            }

            // Create and save the new movie
            Movie newMovie = new Movie();
            newMovie.setName(requestDTO.getName());
            newMovie.setGenre(genre);

            Movie savedMovie = movieRepository.save(newMovie);
            return new AddMovieResponseDTO(savedMovie.getId(), savedMovie.getName(), savedMovie.getGenre().getId());
        } catch (ResourceAlreadyExistsException | ResourceNotFoundException e) {
            // Re-throw these exceptions as they are
            throw e;
        } catch (Exception e) {
            System.out.println("Error in adding movie : "+e);
            throw new ResourceAlreadyExistsException("Movie", "Name", requestDTO.getName());
        }
    }

    public List<MovieViewDTO> fetchMovies() {
        List<Movie> list = movieRepository.findAll();
        return list.stream()
            .map(this::mapToViewDTO)
            .collect(Collectors.toList());
        // MovieViewDTO getGenreResponseDTO = new MovieViewDTO();
        // getGenreResponseDTO.setMovies(list);
        // return getGenreResponseDTO;
    }

    private MovieViewDTO mapToViewDTO(Movie movie) {
        MovieViewDTO dto = new MovieViewDTO();
        dto.setId(movie.getId());
        dto.setMovie(movie.getName());
        dto.setGenreId(movie.getGenre().getId());
        // dto.setSeatRowNumber(reservationSeat.getSeat().getRowNumber());
        // dto.setSeatNumber(reservationSeat.getSeat().getSeatNumber());
        dto.setRating(movie.getRating());
        return dto;
    }
}