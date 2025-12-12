package com.example.backend.controller;

import com.example.backend.dto.request.rag.AskRAGRequest;
import com.example.backend.dto.response.rag.AskRAGResponse;
import com.example.backend.entity.User;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RAGController {

    private final RAGService ragService;
    private final UserRepository userRepository;

    @PostMapping("/ask")
    public ResponseEntity<AskRAGResponse> askQuestion(@RequestBody AskRAGRequest request) {
        // Set userId from authentication if not provided
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            request.setUserId(user.getId().toString());
        }

        AskRAGResponse response = ragService.askQuestion(request);
        return ResponseEntity.ok(response);
    }
}

