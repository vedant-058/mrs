package com.mrs.mrs.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.mrs.mrs.DTO.Auth.AuthResponseDTO;
import com.mrs.mrs.DTO.Auth.LoginRequestDTO;
import com.mrs.mrs.DTO.Auth.SignupRequestDTO;
import com.mrs.mrs.model.User;
import com.mrs.mrs.response.ApiResponse;
import com.mrs.mrs.service.TokenService;
import com.mrs.mrs.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    
    public UserController(UserService userService,TokenService tokenService,AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    public final AuthenticationManager authenticationManager;
    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<?>> signupcontroller(
            @Valid @RequestBody SignupRequestDTO request
    ) {
        AuthResponseDTO authResponseDTO = userService.signUp(request);
        
        if(authResponseDTO.getToken() != null){
            ApiResponse<?> response = ApiResponse.builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(authResponseDTO)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        else{
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Email already in use.")
                    .data(authResponseDTO)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<?>> loginController(@RequestBody LoginRequestDTO request) {
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2. If authentication succeeds, load the user details (if necessary)
            // The UserDetails object returned by your UserDetailsService is available here
            // Note: The principal is typically the UserDetails implementation (e.g., your User object)
            User authenticatedUser = (User) authentication.getPrincipal(); 

            // 3. Generate the JWT
            String token = tokenService.generateToken(authenticatedUser.getId(), authenticatedUser.getRole());

            // 4. Build the successful response DTO
            AuthResponseDTO authResponseDTO = new AuthResponseDTO(
                token, 
                authenticatedUser.getName(), 
                authenticatedUser.getRole()
            );

            ApiResponse<AuthResponseDTO> response = ApiResponse.<AuthResponseDTO>builder()
                .success(true)
                .message("Login successful, token provided.")
                .data(authResponseDTO)
                .timestamp(Instant.now())
                .build();
            
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            // 5. Handle authentication failure (e.g., bad credentials)
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Invalid username or password.")
                .data(null)
                .timestamp(Instant.now())
                .build();

            // Use ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                tokenService.blacklistToken(jwt);
            }

            ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("Logout successful.")
                .timestamp(Instant.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Logout failed: " + e.getMessage())
                .timestamp(Instant.now())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    
}
