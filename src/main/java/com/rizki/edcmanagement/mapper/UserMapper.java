package com.rizki.edcmanagement.mapper;

import org.springframework.stereotype.Component;

import com.rizki.edcmanagement.dto.auth.request.SignUpRequestDTO;
import com.rizki.edcmanagement.dto.auth.response.UserResponse;
import com.rizki.edcmanagement.model.User;

@Component
public class UserMapper {
    public UserResponse fromUserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .build();
    }

    public User fromSignUpRequestToUser(SignUpRequestDTO signUpRequestDTO) {
        return User.builder()
                .username(signUpRequestDTO.getUsername())
                .password(signUpRequestDTO.getPassword())
                .build();
    }
}