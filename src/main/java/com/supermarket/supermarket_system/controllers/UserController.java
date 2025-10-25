package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user.getName(), user.getEmail(), user.getPassword(), user.getPhone(), user.getAddress(), user.getRole());
    }

    @PostMapping("/login")
    public Map<String, String> loginUser(@RequestBody User user) {
        String token = userService.loginUser(user.getEmail(), user.getPassword());
        if (token == null) {
            return Map.of("message", "Invalid email or password.");
        }
        return Map.of(
                "message", "Login successful!",
                "token", token
        );
    }
}
