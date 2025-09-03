package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class TemplateService {
    @Autowired
    private ResourceLoader resourceLoader;

    public String readHtmlTemplate(String fileName) throws IOException {
        String resourcePath = "classpath:/static/" + fileName;

        Resource resource = resourceLoader.getResource(resourcePath);

        if (!resource.exists()) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
