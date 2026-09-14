package com.example.busreservation.security;

import com.example.busreservation.model.User;

/** Minimal authenticated identity kept in the server-side SecurityContext. */
public record AuthenticatedUser(Long id, String email, String role) {
    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
    }
}
