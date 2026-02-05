package com.mrs.mrs.DTO.Screen;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScreenViewDTO {
    private UUID id;
    // private String name;
    private Integer capacity;
}
