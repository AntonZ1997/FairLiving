package com.fairliving.backend.auth;

import com.fairliving.backend.auth.dto.LoginRequest;
import com.fairliving.backend.auth.dto.RegisterRequest;
import com.fairliving.backend.user.UserService;
import com.fairliving.backend.user.dto.UserResponse;
import de.fairliving.backend.jooq.tables.records.UserRecord;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest registerRequest) {
        String passwordHash = passwordEncoder.encode(registerRequest.password());
        UserRecord user = userService.createUser(registerRequest.email(), passwordHash, registerRequest.userName());
        return UserResponse.from(user);
    }

    public UserRecord authenticate(LoginRequest request) {
        UserRecord user;
        try {
            user = userService.getByEmail(request.email());
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        return user;
    }

    public UserResponse getUserResponse(String email) {
        return UserResponse.from(userService.getByEmail(email));
    }
}
