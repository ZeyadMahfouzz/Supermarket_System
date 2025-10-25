package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Autowired
    private JwtUtils jwtUtils;

    public String generateToken(String email, String role) {
        return jwtUtils.generateToken(email, role);
    }

    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    public String getEmail(String token) {
        return jwtUtils.getEmail(token);
    }

    public String getRole(String token) {
        return jwtUtils.getRole(token);
    }
}
