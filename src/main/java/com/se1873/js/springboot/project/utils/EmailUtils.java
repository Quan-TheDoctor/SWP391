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
            helper.setSubject("Application Status Update - " + positionTitle);

            String content = switch (newStatus) {
                case "Reviewing" -> String.format("""
                    <html>
                        <body>
                            <p>Dear %s,</p>
                            <p>We are pleased to inform you that your application for the <strong>%s</strong> position has been moved to the reviewing stage.</p>
                            <p>Our recruitment team will carefully review your application and get back to you within 3-5 business days.</p>
                            <p>Best regards,<br>Recruitment Team</p>
                        </body>
                    </html>
                    """, candidateName, positionTitle);

                case "Interviewing" -> String.format("""
                    <html>
                        <body>
                            <p>Dear %s,</p>
                            <p>We are excited to inform you that your application for the <strong>%s</strong> position has been selected for an interview.</p>
                            <p>Your interview has been scheduled for next week. We will send you a separate email with the exact date, time, and interview details.</p>
                            <p>Please prepare for the interview and ensure you have a stable internet connection for the virtual meeting.</p>
                            <p>Best regards,<br>Recruitment Team</p>
                        </body>
                    </html>
                    """, candidateName, positionTitle);

                case "Hired" -> String.format("""
                    <html>
                        <body>
                            <p>Dear %s,</p>
                            <p>Congratulations! We are delighted to inform you that you have been selected for the <strong>%s</strong> position.</p>
                            <p>Our HR team will contact you shortly with the next steps, including:</p>
                            <ul>
                                <li>Employment contract details</li>
                                <li>Onboarding process</li>
                                <li>Required documentation</li>
                            </ul>
                            <p>We look forward to welcoming you to our team!</p>
                            <p>Best regards,<br>Recruitment Team</p>
                        </body>
                    </html>
                    """, candidateName, positionTitle);

                case "Rejected" -> String.format("""
                    <html>
                        <body>
                            <p>Dear %s,</p>
                            <p>Thank you for your interest in the <strong>%s</strong> position and for taking the time to go through our recruitment process.</p>
                            <p>After careful consideration, we regret to inform you that we have decided to move forward with other candidates whose qualifications more closely match our current needs.</p>
                            <p>We encourage you to apply for future positions that match your skills and experience. We will keep your application on file for future opportunities.</p>
                            <p>We wish you the best in your job search and professional future.</p>
                            <p>Best regards,<br>Recruitment Team</p>
                        </body>
                    </html>
                    """, candidateName, positionTitle);

                default -> String.format("""
                    <html>
                        <body>
                            <p>Dear %s,</p>
                            <p>Your application status for the <strong>%s</strong> position has been updated to <strong>%s</strong>.</p>
                            <p>We will contact you soon with further details.</p>
                            <p>Best regards,<br>Recruitment Team</p>
                        </body>
                    </html>
                    """, candidateName, positionTitle, newStatus);
            };

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error sending email: ", e);
        }
    }

    public void sendPayslipEmail(String to, String employeeName, String month, String year, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Payslip - " + month + " " + year);

            String emailContent = String.format("""
                <html>
                    <body>
                        <p>Xin chào %s,</p>
                        <p>Dưới đây là bảng lương của bạn cho tháng %s năm %s:</p>
                        %s
                        <p>Trân trọng,<br>Phòng Nhân sự</p>
                    </body>
                </html>
                """, employeeName, month, year, content);

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error sending payslip email: ", e);
        }
    }
} 