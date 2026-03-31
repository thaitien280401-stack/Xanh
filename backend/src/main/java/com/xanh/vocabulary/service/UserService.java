package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.request.UpdateUserRequest;
import com.xanh.vocabulary.dto.response.UserDto;
import com.xanh.vocabulary.entity.User;
import com.xanh.vocabulary.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toDto(user);
    }

    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toDto(user);
    }

    @Transactional
    public UserDto update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username already taken");
            }
            user.setUsername(request.username());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        return toDto(userRepository.save(user));
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getAvatarUrl(), user.getRole().name(), user.getCreatedAt()
        );
    }
}
