package com.rizki.edcmanagement.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rizki.edcmanagement.dto.auth.request.RefreshRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignInRequestDTO;
import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.RefreshResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.SignInResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.SignUpResponseDTO;
import com.rizki.edcmanagement.dto.auth.response.TokenResponse;
import com.rizki.edcmanagement.dto.auth.response.UserResponse;
import com.rizki.edcmanagement.exception.ResourceAlreadyExistsException;
import com.rizki.edcmanagement.exception.ResourceNotFoundException;
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

        @Override
        public SignInResponseDTO signIn(SignInRequestDTO requestDTO) {
                User user = repository.findByUsername(requestDTO.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("Username or password is incorrect"));

                if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPassword()))
                        throw new ResourceNotFoundException("Username or password is incorrect");

                String accessToken = jwtService.generateToken(user.getUsername(), user.getUserId());
                String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getUserId());

                user.setRefreshToken(refreshToken);
                User modifiedUser = repository.save(user);

                UserResponse userResponse = userMapper.fromUserToUserResponse(modifiedUser);
                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build();

                return SignInResponseDTO.builder()
                                .user(userResponse)
                                .tokens(tokenResponse)
                                .build();
        }

        @Override
        public RefreshResponseDTO refresh(RefreshRequestDTO requestDTO) {
                User user = repository.findByRefreshToken(requestDTO.getRefreshToken())
                                .orElseThrow(() -> new ResourceNotFoundException("Invalid refresh token"));

                if (jwtService.isTokenExpired(requestDTO.getRefreshToken())) {
                        throw new ResourceNotFoundException("Refresh token has expired");
                }

                String accessToken = jwtService.generateToken(user.getUsername(), user.getUserId());

                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(requestDTO.getRefreshToken())
                                .build();
                UserResponse userResponse = userMapper.fromUserToUserResponse(user);

                return RefreshResponseDTO.builder()
                                .user(userResponse)
                                .tokens(tokenResponse)
                                .build();
        }
}