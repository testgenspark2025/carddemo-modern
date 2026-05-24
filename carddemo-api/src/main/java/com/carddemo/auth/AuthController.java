package com.carddemo.auth;

import com.carddemo.domain.user.UserEntity;
import com.carddemo.domain.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public record LoginRequest(
            @NotBlank @Size(max = 8) String userId,
            @NotBlank @Size(max = 8) String password) {}

    public record LoginResponse(
            String token,
            String userId,
            String userType,
            String firstName,
            String lastName) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        // Mirror the legacy COSGN00C behaviour: uppercase the user id (passwords are case-sensitive
        // in modern crypto, but the legacy app uppercased both - we preserve user-id uppercasing only).
        String uid = req.userId().toUpperCase();
        UserEntity user = users.findById(uid).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found. Try again ..."));
        }
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Wrong Password. Try again ..."));
        }
        String token = jwt.issue(user.getUserId(), user.getUserType(),
                                 user.getFirstName().trim(), user.getLastName().trim());
        return ResponseEntity.ok(new LoginResponse(
                token, user.getUserId(), user.getUserType(),
                user.getFirstName().trim(), user.getLastName().trim()));
    }
}
