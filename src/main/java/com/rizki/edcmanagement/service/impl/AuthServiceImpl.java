package com.rizki.edcmanagement.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.SignUpResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.TokenResponse;
import com.rizki.edcmanagement.dto.auth.response.UserResponse;
import com.rizki.edcmanagement.exception.ResourceAlreadyExistsException;
import com.rizki.edcmanagement.mapper.UserMapper;
import com.rizki.edcmanagement.model.User;
import com.rizki.edcmanagement.repository.UserRepository;
import com.rizki.edcmanagement.service.AuthService;
import com.rizki.edcmanagement.service.JwtService;

@Service
public class AuthServiceImpl implements AuthService {
        @Autowired
        private UserRepository repository;

        @Autowired
        private UserMapper userMapper;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtService jwtService;

        @Override
        @Transactional
        public SignUpResponseDTO signUp(SignUpRequestDTO requestDTO) {
                repository.findByUsername(requestDTO.getUsername())
                                .ifPresent(user -> {
                                        throw new ResourceAlreadyExistsException("Username is already exists");
                                });

                User user = userMapper.fromSignUpRequestToUser(requestDTO);
                user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

                User savedUser = repository.save(user);

                String accessToken = jwtService.generateToken(savedUser.getUsername(), savedUser.getUserId());
                String refreshToken = jwtService.generateRefreshToken(savedUser.getUsername(), savedUser.getUserId());

                savedUser.setRefreshToken(refreshToken);
                User finalUser = repository.save(savedUser);

                UserResponse userResponse = userMapper.fromUserToUserResponse(finalUser);
                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build();

                return SignUpResponseDTO.builder()
                                .user(userResponse)
                                .tokens(tokenResponse)
                                .build();
        }
}