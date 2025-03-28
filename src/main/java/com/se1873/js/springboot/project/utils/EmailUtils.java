package com.se1873.js.springboot.project.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtils {
    private final JavaMailSender mailSender;

    public void sendStatusUpdateEmail(String to, String candidateName, String positionTitle, String newStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Cập nhật trạng thái đơn ứng tuyển");

            String content = String.format("""
                <html>
                    <body>
                        <p>Xin chào %s,</p>
                        <p>Đơn ứng tuyển của bạn cho vị trí <strong>%s</strong> đã được cập nhật trạng thái thành <strong>%s</strong>.</p>
                        <p>Chúng tôi sẽ liên hệ với bạn sớm nhất có thể.</p>
                        <p>Trân trọng,<br>Đội ngũ Tuyển dụng</p>
                    </body>
                </html>
                """, candidateName, positionTitle, newStatus);

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Đã gửi email thông báo cập nhật trạng thái đến {}", to);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email thông báo cập nhật trạng thái: {}", e.getMessage());
        }
    }
} 