package com.mrs.mrs.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id",nullable = false)
    private Screen screen;

    @Column(nullable = false)
    private Character rowNumber;
    
    @Column(nullable = false)
    private Integer seatNumber;

    @Column(name = "x_pos")
    private Integer xPos; // Horizontal coordinate for the UI
    
    @Column(name = "y_pos")
    private Integer yPos; // Vertical coordinate for the UI

    // @Enumerated(EnumType.STRING)
    // private SeatType type; // NORMAL, PREMIUM, RECLINER
}
