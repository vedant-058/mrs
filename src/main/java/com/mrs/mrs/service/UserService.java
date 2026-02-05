package com.mrs.mrs.service;


import org.springframework.stereotype.Service;

import com.mrs.mrs.DTO.Auth.AuthResponseDTO;
import com.mrs.mrs.DTO.Auth.LoginRequestDTO;
import com.mrs.mrs.DTO.Auth.SignupRequestDTO;
import com.mrs.mrs.exception.InvalidCredentialsException;
import com.mrs.mrs.exception.ResourceAlreadyExistsException;
import com.mrs.mrs.model.User;
import com.mrs.mrs.model.User.UserRole;
import com.mrs.mrs.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService{
    
    // UserRole defaultRole = UserRole.ADMIN;
    UserRole defaultRole = UserRole.CUSTOMER;
    //19DEC 11:14 eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI5OTFhOGQ3Mi1jMTA5LTQwYmItYjZiNS05MGI1YjdhY2JhNTciLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3NjYxMjMwMzAsImV4cCI6MTc2NjIwOTQzMH0.Y7Oqv37brH-6KPyrE_ehG7ishxH79WUy2r6aCowiseg

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponseDTO signUp(SignupRequestDTO requestDTO){
        
        if (userRepository.existsByEmail(requestDTO.getEmail())){
            // return new AuthResponseDTO(null, null, null);
            throw new ResourceAlreadyExistsException("User","email", requestDTO.getEmail());
        }

        User user = new User();
        user.setEmail(requestDTO.getEmail());
        user.setName(requestDTO.getUsername());
        String hashedPassword = passwordEncoder.encode(requestDTO.getPassword());
        
        user.setHashedpassword(hashedPassword);
        
        user.setRole(defaultRole);

        User savedUser = userRepository.save(user);
        
        String token = tokenService.generateToken(savedUser.getId(), defaultRole);
        
        return new AuthResponseDTO(token, savedUser.getName(), defaultRole);
    }

    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        Optional<User> userOptional = userRepository.findByEmail(requestDTO.getEmail());
        
        if (userOptional.isEmpty()) {
            System.out.println("User not found with email: " + requestDTO.getEmail());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userOptional.get();
        
        if (!passwordEncoder.matches(requestDTO.getPassword(), user.getHashedpassword())) {
            System.out.println("Invalid password for email: " + requestDTO.getEmail());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = tokenService.generateToken(user.getId(), user.getRole());
        return new AuthResponseDTO(token, user.getName(), user.getRole());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // 1. Get the Optional from the repository
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 2. Use the Optional's orElseThrow method to handle the conversion and error:
        return userOptional.orElseThrow(() -> 
            new UsernameNotFoundException("User not found with email: " + email)
        );
    }
    
    public UserDetails loadUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }
}