package com.example.busreservation.controller;

import com.example.busreservation.model.User;
import com.example.busreservation.security.AuthenticatedUser;
import com.example.busreservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * UserController - Handles user-related API endpoints
 * Used by: ALL ROLES (Passenger, IT Support, Customer Service, Finance, Ticketing, Operation Manager)
 * Functions: User registration, login, profile management, password reset, user listing
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // allow frontend requests
public class UserController {

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session,
                                   HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Optional<User> u = userService.loginUser(request.get("email"), request.get("password"));
        if (u.isPresent()) {
            AuthenticatedUser identity = AuthenticatedUser.from(u.get());
            String role = u.get().getRole() == null ? "PASSENGER" : u.get().getRole().toUpperCase().replace(' ', '_');
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                identity, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            return ResponseEntity.ok(safeUser(u.get()));
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        Optional<User> current = userService.findById(user.id());
        if (current.isEmpty()) return ResponseEntity.status(401).body("Not authenticated");
        return ResponseEntity.ok(safeUser(current.get()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> safeUser(User user) {
        Map<String, Object> safe = new HashMap<>();
        safe.put("id", user.getId());
        safe.put("firstName", user.getFirstName());
        safe.put("lastName", user.getLastName());
        safe.put("phone", user.getPhone());
        safe.put("email", user.getEmail());
        safe.put("role", user.getRole());
        safe.put("nic", user.getNic());
        return safe;
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        boolean ok = userService.resetPassword(request.get("email"), request.get("newPassword"));
        if (ok) return ResponseEntity.ok("Password reset successful");
        return ResponseEntity.badRequest().body("Email not found");
    }

    @GetMapping("/role")
    public ResponseEntity<?> getRole(@RequestParam("email") String email) {
        Optional<User> byEmail = userService.findByEmail(email);
        if (byEmail.isPresent()) {
            return ResponseEntity.ok(Map.of("role", byEmail.get().getRole()));
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @GetMapping("")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updates) {
        Optional<User> updated = userService.updateUser(id, updates);
        if (updated.isPresent()) return ResponseEntity.ok(updated.get());
        return ResponseEntity.badRequest().body("User not found");
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteCurrentPassenger(Authentication authentication, HttpSession session) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return ResponseEntity.status(401).body("Please sign in before deleting your account");
        }
        if (!"passenger".equalsIgnoreCase(user.role())) {
            return ResponseEntity.status(403).body("Only passenger accounts can be deleted from the profile page");
        }

        UserService.AccountDeletionResult result = userService.deletePassengerAccount(user.id());
        if (result == UserService.AccountDeletionResult.DELETED) {
            SecurityContextHolder.clearContext();
            session.invalidate();
            return ResponseEntity.noContent().build();
        }
        if (result == UserService.AccountDeletionResult.HAS_BOOKINGS) {
            return ResponseEntity.status(409).body("Your account has booking history and cannot be deleted. Contact support for help.");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body("User not found");
    }
}
