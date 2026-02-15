package org.example.bankingapi.service;

import org.example.bankingapi.dto.AuthResponse;
import org.example.bankingapi.dto.RegisterRequest;
import org.example.bankingapi.entity.Role;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.exception.DuplicateEmailException;
import org.example.bankingapi.exception.DuplicateUsernameException;
import org.example.bankingapi.repository.RoleRepository;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException(request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            for (String roleName : request.getRoleNames()) {
                roleRepository.findByName(roleName)
                        .ifPresent(roles::add);
            }
        }
        if (roles.isEmpty()) {
            roleRepository.findByName(DEFAULT_ROLE).ifPresent(roles::add);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        String jwt = tokenProvider.generateToken(user.getUsername());
        return buildAuthResponse(user.getUsername(), jwt);
    }

    public AuthResponse buildAuthResponse(String username, String jwt) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }
}
