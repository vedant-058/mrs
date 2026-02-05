package com.mrs.mrs.DTO.Auth;

import com.mrs.mrs.model.User.UserRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDTO {
    
    private String token;
    private String tokenType = "Bearer"; 
    private String username;
    private UserRole role;
    
    public AuthResponseDTO(String token, String username, UserRole role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }
}