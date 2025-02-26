package com.example.blps.service.auth;

import com.example.blps.dto.auth.AuthenticationRequest;
import com.example.blps.dto.auth.AuthenticationResponse;
import com.example.blps.dto.auth.RegisterRequest;
import com.example.blps.errorHandler.UserAlreadyExistsException;
import com.example.blps.model.authEntity.Role;
import com.example.blps.model.authEntity.User;
import com.example.blps.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public AuthenticationResponse register(RegisterRequest request){
        boolean userExists = userRepository.findByUsername(request.getUsername()).isPresent();

        if (userExists) {
            throw new UserAlreadyExistsException("A user with the same username already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();



        userRepository.save(user);
        var token = jwtService.generateToken(user);
        Role role =  user.getRole();
        return AuthenticationResponse.builder()
                .token(token)
                .role(role)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("user not found"));
        var token = jwtService.generateToken(user);
        Role role =  user.getRole();
        return AuthenticationResponse.builder()
                .token(token)
                .role(role)
                .build();
    }
}
