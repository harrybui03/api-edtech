package com.example.backend.service;

import com.example.backend.entity.Transaction;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

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
        return String.format("""
            <html>
            <body>
                <h2>Payment Successful!</h2>
                <p>Dear %s,</p>
                <p>Your payment for the course <strong>%s</strong> has been processed successfully.</p>
                
                <h3>Payment Details:</h3>
                <ul>
                    <li><strong>Order Code:</strong> %s</li>
                    <li><strong>Course:</strong> %s</li>
                    <li><strong>Amount:</strong> %s %s</li>
                    <li><strong>Payment Date:</strong> %s</li>
                </ul>
                
                <p>You can now access your course content and start learning!</p>
                <p>Thank you for choosing our platform.</p>
                
                <p>Best regards,<br>EdTech Team</p>
            </body>
            </html>
            """,
            transaction.getStudent().getFullName(),
            transaction.getCourse().getTitle(),
            transaction.getOrderCode(),
            transaction.getCourse().getTitle(),
            formatCurrency(transaction.getAmount()),
            transaction.getCurrency(),
            transaction.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    private String buildPaymentReceivedEmailContent(Transaction transaction) {
        return String.format("""
            <html>
            <body>
                <h2>Payment Received!</h2>
                <p>Dear %s,</p>
                <p>You have received a payment for your course <strong>%s</strong>.</p>
                
                <h3>Payment Details:</h3>
                <ul>
                    <li><strong>Order Code:</strong> %s</li>
                    <li><strong>Student:</strong> %s</li>
                    <li><strong>Course:</strong> %s</li>
                    <li><strong>Amount:</strong> %s %s</li>
                    <li><strong>Payment Date:</strong> %s</li>
                </ul>
                
                <p>Congratulations on your new enrollment!</p>
                
                <p>Best regards,<br>EdTech Team</p>
            </body>
            </html>
            """,
            transaction.getInstructor().getFullName(),
            transaction.getCourse().getTitle(),
            transaction.getOrderCode(),
            transaction.getStudent().getFullName(),
            transaction.getCourse().getTitle(),
            formatCurrency(transaction.getAmount()),
            transaction.getCurrency(),
            transaction.getPaidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    private String buildPaymentFailureEmailContent(Transaction transaction) {
        return String.format("""
            <html>
            <body>
                <h2>Payment Failed</h2>
                <p>Dear %s,</p>
                <p>Unfortunately, your payment for the course <strong>%s</strong> could not be processed.</p>
                
                <h3>Transaction Details:</h3>
                <ul>
                    <li><strong>Order Code:</strong> %s</li>
                    <li><strong>Course:</strong> %s</li>
                    <li><strong>Amount:</strong> %s %s</li>
                    <li><strong>Status:</strong> %s</li>
                </ul>
                
                <p>Please try again or contact support if you continue to experience issues.</p>
                <p>You can retry the payment using the same course enrollment link.</p>
                
                <p>Best regards,<br>EdTech Team</p>
            </body>
            </html>
            """,
            transaction.getStudent().getFullName(),
            transaction.getCourse().getTitle(),
            transaction.getOrderCode(),
            transaction.getCourse().getTitle(),
            formatCurrency(transaction.getAmount()),
            transaction.getCurrency(),
            transaction.getStatus()
        );
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f", amount);
    }

}
