package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public User registerUser(String name, String email, String password, String phone, String address, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(name, email, hashedPassword, phone, address, role);
        return userRepository.save(newUser);
    }

    public String loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT Token
        return jwtService.generateToken(user.getEmail(), user.getRole());
    }
}