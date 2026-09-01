package org.example.itemcounting.rest.controller;

import org.example.itemcounting.business.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class AuthController {
    @Autowired
    private JwtService jwtService;

    @GetMapping("/token")
    public String getToken(@RequestParam String username, @RequestParam String group) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("group", group);
        return jwtService.generateToken(username, claims);
    }
}
