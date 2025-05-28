package com.lms_platform.service;

import com.lms_platform.dto.*; // Updated import
import com.lms_platform.model.User; // Updated import
import com.lms_platform.model.role.UserRole; // Updated import
import com.lms_platform.repository.UserRepository; // Updated import
import com.lms_platform.security.jwt.JwtUtil; // Updated import
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserInfoResponse registerUser(RegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User(
                registrationRequest.getFirstName(),
                registrationRequest.getLastName(),
                registrationRequest.getEmail(),
                passwordEncoder.encode(registrationRequest.getPassword()),
                UserRole.USER
        );

        User savedUser = userRepository.save(user);
        return new UserInfoResponse(savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(), savedUser.getEmail(), savedUser.getRole().name());
    }

    @Transactional
    public UserInfoResponse registerAdmin(RegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        User admin = new User(
                registrationRequest.getFirstName(),
                registrationRequest.getLastName(),
                registrationRequest.getEmail(),
                passwordEncoder.encode(registrationRequest.getPassword()),
                UserRole.ADMIN
        );
        User savedAdmin = userRepository.save(admin);
        return new UserInfoResponse(savedAdmin.getId(), savedAdmin.getFirstName(), savedAdmin.getLastName(), savedAdmin.getEmail(), savedAdmin.getRole().name());
    }


    public AuthResponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userDetails = (User) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        return new AuthResponse(jwt, userDetails.getEmail(), userDetails.getRole().name(), userDetails.getId());
    }
}
