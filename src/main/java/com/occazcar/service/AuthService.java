package com.occazcar.service;

import com.occazcar.dto.AuthResponse;
import com.occazcar.dto.LoginRequest;
import com.occazcar.dto.RegisterRequest;
import com.occazcar.model.User;
import com.occazcar.repository.UserRepository;
import com.occazcar.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return createAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return createAuthResponse(user, token);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    private AuthResponse createAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().name());
        return response;
    }
}