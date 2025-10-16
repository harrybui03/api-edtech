package com.example.backend.service;

import com.example.backend.entity.Transaction;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StreamUtils;

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(mimeMessage);
        log.info("Sent email successfully to {}", to);
    }

    @Async
    public void sendPaymentSuccessEmail(String to, Transaction transaction) {
        try {
            String subject = "Payment Successful - Course Enrollment Confirmed";
            String content = buildPaymentSuccessEmailContent(transaction);
            sendEmail(to, subject, content);
        } catch (Exception e) {
            log.error("Error sending payment success email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendPaymentReceivedEmail(String to, Transaction transaction) {
        try {
            String subject = "Payment Received - New Course Enrollment";
            String content = buildPaymentReceivedEmailContent(transaction);
            sendEmail(to, subject, content);
        } catch (Exception e) {
            log.error("Error sending payment received email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendPaymentFailureEmail(String to, Transaction transaction) {
        try {
            String subject = "Payment Failed - Course Enrollment";
            String content = buildPaymentFailureEmailContent(transaction);
            sendEmail(to, subject, content);
        } catch (Exception e) {
            log.error("Error sending payment failure email to {}: {}", to, e.getMessage(), e);
        }
    }

    private String buildPaymentSuccessEmailContent(Transaction transaction) {
        Map<String, String> variables = new HashMap<>();
        String title = transaction.getCourse() != null ? transaction.getCourse().getTitle() :
                (transaction.getBatch() != null ? transaction.getBatch().getTitle() : "");
        variables.put("studentName", transaction.getStudent().getFullName());
        variables.put("courseTitle", title);
        variables.put("orderCode", String.valueOf(transaction.getOrderCode()));
        variables.put("amount", formatCurrency(transaction.getAmount()));
        variables.put("currency", transaction.getCurrency());
        variables.put("paymentDate", transaction.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return renderTemplate("static/email/payment-success.html", variables);
    }

    private String buildPaymentReceivedEmailContent(Transaction transaction) {
        Map<String, String> variables = new HashMap<>();
        String title = transaction.getCourse() != null ? transaction.getCourse().getTitle() :
                (transaction.getBatch() != null ? transaction.getBatch().getTitle() : "");
        variables.put("instructorName", transaction.getInstructor().getFullName());
        variables.put("courseTitle", title);
        variables.put("orderCode", String.valueOf(transaction.getOrderCode()));
        variables.put("studentName", transaction.getStudent().getFullName());
        variables.put("amount", formatCurrency(transaction.getAmount()));
        variables.put("currency", transaction.getCurrency());
        variables.put("paymentDate", transaction.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return renderTemplate("static/email/payment-received.html", variables);
    }

    private String buildPaymentFailureEmailContent(Transaction transaction) {
        Map<String, String> variables = new HashMap<>();
        String title = transaction.getCourse() != null ? transaction.getCourse().getTitle() :
                (transaction.getBatch() != null ? transaction.getBatch().getTitle() : "");
        variables.put("studentName", transaction.getStudent().getFullName());
        variables.put("courseTitle", title);
        variables.put("orderCode", String.valueOf(transaction.getOrderCode()));
        variables.put("amount", formatCurrency(transaction.getAmount()));
        variables.put("currency", transaction.getCurrency());
        variables.put("status", String.valueOf(transaction.getStatus()));
        return renderTemplate("static/email/payment-failure.html", variables);
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f", amount);
    }

    private String renderTemplate(String classpathLocation, Map<String, String> variables) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathLocation);
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            String rendered = template;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                rendered = rendered.replace(placeholder, safeString(entry.getValue()));
            }
            return rendered;
        } catch (Exception ex) {
            log.error("Failed to render email template {}: {}", classpathLocation, ex.getMessage(), ex);
            return "";
        }
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

}
