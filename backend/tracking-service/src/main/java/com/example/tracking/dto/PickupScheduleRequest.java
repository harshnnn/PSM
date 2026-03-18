package com.example.tracking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class PickupScheduleRequest {

    @NotNull
    private LocalDateTime pickupDateTime;

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }
}
