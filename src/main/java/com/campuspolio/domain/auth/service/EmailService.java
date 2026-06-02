package com.campuspolio.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendAuthCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[Campuspolio] 학교 이메일 인증번호");
            helper.setText(
                    "<div style='font-family: sans-serif; padding: 20px;'>" +
                            "<h3>이메일 인증번호</h3>" +
                            "<p>아래 인증번호를 입력해주세요.</p>" +
                            "<h2 style='color: #4F46E5; letter-spacing: 4px;'>" + code + "</h2>" +
                            "<p style='color: #888;'>인증번호는 5분간 유효합니다.</p>" +
                            "</div>",
                    true
            );
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}