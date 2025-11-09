package com.example.backend.service;

import com.example.backend.dto.model.UserDTO;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.entity.User;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Current user not found in database"));
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public void updateUser(UserDTO userDTO , UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Current user not found in database"));

        user.setFullName(userDTO.getFullName());
        user.setUserImage(userDTO.getUserImage());

        userRepository.save(user);
    }

    public UserDTO getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + id));
        return userMapper.toUserDTO(user);
    }

    public PaginationResponse<UserDTO> getUsers(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserDTO> userDtoPage = userPage.map(userMapper::toUserDTO);
        return new PaginationResponse<>(userDtoPage);
    }
}
