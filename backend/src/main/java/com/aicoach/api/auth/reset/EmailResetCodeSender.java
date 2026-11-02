package com.aicoach.api.auth.reset;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Gửi mã qua email (SMTP, ví dụ Gmail + Mật khẩu ứng dụng).
 * Chưa cấu hình MAIL_HOST thì KHÔNG gửi được email: mã được ghi vào log server để demo
 * (xem bằng: docker compose logs api | grep "Mã quên mật khẩu").
 */
@Component
public class EmailResetCodeSender implements ResetCodeSender {

    private static final Logger log = LoggerFactory.getLogger(EmailResetCodeSender.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String host;
    private final String from;

    public EmailResetCodeSender(ObjectProvider<JavaMailSender> mailSender,
                                @Value("${spring.mail.host:}") String host,
                                @Value("${spring.mail.username:}") String username,
                                @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.host = host;
        // Không khai MAIL_FROM thì gửi từ chính tài khoản đăng nhập SMTP (Gmail bắt buộc khớp)
        this.from = from == null || from.isBlank() ? username : from;
    }

    @Override
    public void send(String email, String displayName, String code, int validMinutes) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (host == null || host.isBlank() || sender == null) {
            log.warn("Chưa cấu hình gửi email (MAIL_HOST). Mã quên mật khẩu của {}: {}", email, code);
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(email);
            if (from != null && !from.isBlank()) {
                helper.setFrom(from, "AI Coach");
            }
            helper.setSubject("Mã xác thực AI Coach: " + code);
            String name = displayName == null ? "" : " " + displayName;
            String text = "Xin chào" + name + ",\n\n"
                    + "Mã xác thực để đặt lại mật khẩu AI Coach của bạn là: " + code + "\n"
                    + "Mã có hiệu lực trong " + validMinutes + " phút. Không chia sẻ mã này với ai.\n\n"
                    + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.";
            String html = "<div style='font-family:Arial,sans-serif;color:#0B2A5B;max-width:480px'>"
                    + "<h2 style='margin:0 0 12px'>Đặt lại mật khẩu AI Coach</h2>"
                    + "<p>Xin chào" + escape(name) + ",</p>"
                    + "<p>Mã xác thực của bạn:</p>"
                    + "<p style='font-size:32px;font-weight:bold;letter-spacing:8px;color:#1E5EFF;margin:8px 0'>"
                    + code + "</p>"
                    + "<p>Mã có hiệu lực trong <b>" + validMinutes + " phút</b>. Không chia sẻ mã này với ai.</p>"
                    + "<p style='color:#6B7A99;font-size:13px'>Nếu bạn không yêu cầu đặt lại mật khẩu, "
                    + "hãy bỏ qua email này.</p></div>";
            helper.setText(text, html);
            sender.send(message);
            log.info("Đã gửi mã quên mật khẩu tới {}", email);
        } catch (Exception e) {
            // Không làm lộ lỗi ra app (tránh dò email); ghi log để Trưởng nhóm kiểm tra cấu hình SMTP.
            log.error("Gửi email mã quên mật khẩu tới {} thất bại", email, e);
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
