package com.mrs.mrs.DTO.Screen;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddScreenRequestDTO {
    private String name;
    private Integer capacity;
}
