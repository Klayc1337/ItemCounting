package org.example.itemcounting.rest.controller;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.User;
import org.example.itemcounting.rest.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.example.itemcounting.business.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest input) {
        return authService.register(input);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest input) {
        return authService.login(input.username(), input.password());
    }

    public record LoginRequest(String username, String password) {}
}
