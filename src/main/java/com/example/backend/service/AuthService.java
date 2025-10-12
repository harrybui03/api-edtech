package com.example.backend.service;

import com.example.backend.config.JwtTokenProvider;
import com.example.backend.constant.UserRoleEnum;
import com.example.backend.constant.UserType;
import com.example.backend.dto.request.auth.LoginRequest;
import com.example.backend.dto.request.auth.SignupRequest;
import com.example.backend.dto.response.auth.JwtAuthResponse;
import com.example.backend.entity.User;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private RoleService roleService;


    @Value("${app.base-url}")
    private String appBaseUrl;

    @Transactional
    public void signUp(SignupRequest signupRequest) throws BadRequestException {
        boolean userExist = userRepository.existsByEmail(signupRequest.getEmail());
        if (userExist) {
            throw new InvalidRequestDataException("User already exists");
        }

        User user = User.builder()
                .email(signupRequest.getEmail())
                .fullName(signupRequest.getFullName())
                .username(genUsername(signupRequest.getEmail()))
                .userType(UserType.WEBSITE_USER)
                .enabled(false)
                .build();
        userRepository.save(user);
        roleService.assignRole(user.getId(), UserRoleEnum.LMS_STUDENT);
        sendConfirmMail(user);
    }

    public void logIn(LoginRequest loginRequest) throws BadRequestException {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidRequestDataException("User not found"));

        sendConfirmMail(user);
    }

    @Transactional
    public JwtAuthResponse verifyToken(String token) throws BadRequestException {
        jwtTokenProvider.validateToken(token);
        String email = jwtTokenProvider.getEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));
        user.setEnabled(true);
        user.setLastActive(OffsetDateTime.now());
        userRepository.save(user);
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        return JwtAuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer").build();
    }

    public JwtAuthResponse refresh(String refreshToken) throws BadRequestException {
        jwtTokenProvider.validateToken(refreshToken);
        String email = jwtTokenProvider.getEmail(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.getEnabled()){
            throw new BadRequestException("User is not enabled");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        return JwtAuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer").build();
    }

    private String genUsername(String email) {
        return email.substring(0, email.indexOf("@")).toLowerCase();
    }

    private void sendConfirmMail(User user) throws BadRequestException {
        try {
            long duration = 5;
            String token = jwtTokenProvider.generateVerificationToken(user.getEmail(), duration);

            String confirmLink = appBaseUrl + "/auth/verify?token=" + token;
            String emailSubject = "Email verification confirmation";
            String rawTemplate = templateService.readHtmlTemplate("confirmation.html");
            String htmlContent = rawTemplate.replace("${confirmationLink}", confirmLink);
            emailService.sendEmail(user.getEmail(), emailSubject, htmlContent);
        } catch (IOException | MessagingException e) {
            throw new BadRequestException("Failed to send confirmation email");
        }
    }
}