package com.personalfinance.user.dto;

import java.time.Instant;
import java.util.Optional;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private boolean confirmed;
    private Instant confirmedAt;
    private Instant insertedAt;
    private Instant updatedAt;

    public Optional<Instant> getConfirmedAt() {
        return Optional.ofNullable(confirmedAt);
    }


}