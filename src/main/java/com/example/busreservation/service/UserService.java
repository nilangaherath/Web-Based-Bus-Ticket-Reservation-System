package com.example.busreservation.service;

import com.example.busreservation.model.User;
import com.example.busreservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.busreservation.repository.BookingRepository;
import com.example.busreservation.repository.FeedbackRepository;
import com.example.busreservation.repository.NotificationRepository;
import com.example.busreservation.repository.SavedCardRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SavedCardRepository savedCardRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    public User registerUser(User user) {
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("Passenger");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isPresent() && passwordEncoder.matches(password, u.get().getPassword())) {
            User user = u.get();
            if (!user.getPassword().startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
            }
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean resetPassword(String email, String newPassword) {
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isPresent()) {
            User user = u.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> updateUser(Long id, User updates) {
        return userRepository.findById(id).map(existing -> {
            if (updates.getFirstName() != null) existing.setFirstName(updates.getFirstName());
            if (updates.getLastName() != null) existing.setLastName(updates.getLastName());
            if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
            if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
            if (updates.getNic() != null) existing.setNic(updates.getNic());
            if (updates.getPassword() != null) existing.setPassword(passwordEncoder.encode(updates.getPassword()));
            if (updates.getRole() != null) existing.setRole(updates.getRole());
            return userRepository.save(existing);
        });
    }

    public boolean deleteUser(Long id) {
        try {
            return userRepository.findById(id).map(u -> {
                userRepository.delete(u);
                userRepository.flush();
                return true;
            }).orElse(false);
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Transactional
    public AccountDeletionResult deletePassengerAccount(Long id) {
        return userRepository.findById(id).map(user -> {
            if (bookingRepository.countByUserNic(user.getNic()) > 0) {
                return AccountDeletionResult.HAS_BOOKINGS;
            }

            notificationRepository.deleteByUserNic(user.getNic());
            savedCardRepository.deleteByUserId(id);
            feedbackRepository.deleteByUserId(id);
            userRepository.delete(user);
            return AccountDeletionResult.DELETED;
        }).orElse(AccountDeletionResult.NOT_FOUND);
    }

    public enum AccountDeletionResult {
        DELETED,
        HAS_BOOKINGS,
        NOT_FOUND
    }
}
