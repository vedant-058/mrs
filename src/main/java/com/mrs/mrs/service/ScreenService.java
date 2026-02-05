package com.mrs.mrs.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mrs.mrs.DTO.Screen.AddScreenRequestDTO;
import com.mrs.mrs.DTO.Screen.AddScreenResponseDTO;
import com.mrs.mrs.DTO.Screen.ScreenViewDTO;
import com.mrs.mrs.exception.ResourceAlreadyExistsException;
import com.mrs.mrs.model.Screen;
import com.mrs.mrs.repository.ScreenRepository;

import jakarta.transaction.Transactional;

@Service
public class ScreenService {
    private final ScreenRepository screenRepository;

    public ScreenService(ScreenRepository screenRepository){
        this.screenRepository = screenRepository;
    }

    @Transactional
    public AddScreenResponseDTO saveScreen(AddScreenRequestDTO requestDTO){
        try {
            Screen newScreen = new Screen();
            newScreen.setName(requestDTO.getName());
            newScreen.setCapacity(requestDTO.getCapacity());
            if (screenRepository.existsByName(requestDTO.getName())) {
                throw new ResourceAlreadyExistsException("Screen", "Name", requestDTO.getName());
            }
            Screen savedScreen = screenRepository.save(newScreen);
            return new AddScreenResponseDTO( savedScreen.getName(), savedScreen.getCapacity());
        } catch (Exception e) {
            throw new ResourceAlreadyExistsException("Screen", "Name", requestDTO.getName());
        }
    }

    public List<ScreenViewDTO> fetchScreens() {
        List<Screen> screens = screenRepository.findAll();
        
        // Map entities to view DTOs and return directly
        return screens.stream()
            .map(this::mapToViewDTO)
            .collect(Collectors.toList());
    }
    
    private ScreenViewDTO mapToViewDTO(Screen screen) {
        ScreenViewDTO dto = new ScreenViewDTO();
        dto.setId(screen.getId());
        // dto.setName(screen.getName());
        dto.setCapacity(screen.getCapacity());
        return dto;
    }
}
