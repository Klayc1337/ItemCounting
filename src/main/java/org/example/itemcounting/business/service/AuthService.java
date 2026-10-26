package org.example.itemcounting.business.service;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.User;
import org.example.itemcounting.enums.UserRole;
import org.example.itemcounting.repository.WarehouseRepository;
import org.example.itemcounting.rest.dto.RegisterRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import org.example.itemcounting.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    public User register(RegisterRequest input) {
        String username = input.username().strip();

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(input.password()));
        user.setFullName(input.fullName() == null ? null : input.fullName().strip());
        user.setRole(UserRole.STORE_MANAGER);
        user.setIsActive(true);
        if (input.warehouseId() != null) {
            user.setWarehouse(warehouseRepository.findById(input.warehouseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "склад не найден")));
        }
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "не удалось зарегистрироваться", ex);
        }
    }

    @Transactional(readOnly = true)
    public String login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadCredentialsException("неверное имя пользователя или пароль");
        }
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("неверное имя пользователя или пароль"));
        if (!Boolean.TRUE.equals(user.getIsActive()) || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("неверное имя пользователя или пароль");
        }
        return jwtService.generateToken(user);
    }
}
