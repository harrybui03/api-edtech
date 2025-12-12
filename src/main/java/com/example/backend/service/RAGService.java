package com.example.backend.service;

import com.example.backend.dto.request.rag.AskRAGRequest;
import com.example.backend.dto.response.rag.AskRAGResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

    private final RestTemplate restTemplate;

    @Value("${rag.api.url}")
    private String ragApiUrl;

    /**
     * Ask a question to the RAG system
     *
     * @param request The RAG request containing question, userId, and chat history
     * @return The RAG response with answer and sources
     * @throws RestClientException if the RAG API call fails
     */
    public AskRAGResponse askQuestion(AskRAGRequest request) {
        String url = ragApiUrl + "/api/v1/ask";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AskRAGRequest> entity = new HttpEntity<>(request, headers);

        try {
            log.info("Calling RAG API: {} with question: {}", url, request.getQuestion());
            ResponseEntity<AskRAGResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    AskRAGResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("RAG API response received successfully");
                return response.getBody();
            } else {
                log.error("RAG API returned non-success status: {}", response.getStatusCode());
                throw new RestClientException("RAG API returned non-success status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error calling RAG API: {}", e.getMessage(), e);
            throw new RestClientException("Failed to call RAG API: " + e.getMessage(), e);
        }
    }
}

