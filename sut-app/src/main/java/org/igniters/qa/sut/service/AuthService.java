package org.igniters.qa.sut.service;

import org.igniters.qa.sut.domain.User;
import org.igniters.qa.sut.dto.LoginResponse;
import org.igniters.qa.sut.error.UnauthorizedException;
import org.igniters.qa.sut.repository.UserRepository;
import org.igniters.qa.sut.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                // Same error for "no such user" and "wrong password" — distinguishing
                // them would let an attacker enumerate which emails have accounts.
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS", "Email or password is incorrect."));
        String token = tokenService.issueToken(user.getEmail());
        return new LoginResponse(token, user.getRole().name(), user.getName());
    }
}
