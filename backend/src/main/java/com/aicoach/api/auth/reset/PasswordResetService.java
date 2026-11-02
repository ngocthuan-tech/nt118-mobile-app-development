package com.aicoach.api.auth.reset;

import com.aicoach.api.auth.AuthService;
import com.aicoach.api.common.ApiException;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Quên mật khẩu 3 bước (Figma 08 → 09 → 10 → 11):
 * <ol>
 *   <li>{@link #requestCode}: sinh mã 6 số ngẫu nhiên (SecureRandom), lưu bản băm BCrypt, gửi email.</li>
 *   <li>{@link #verifyCode}: kiểm tra mã — sai thì tăng số lần thử, sai 5 lần thì khoá mã.</li>
 *   <li>{@link #resetPassword}: kiểm tra mã lần cuối, đặt mật khẩu mới, huỷ mọi mã,
 *       mọi token đăng nhập cũ hết hiệu lực.</li>
 * </ol>
 * Câu trả lời của bước 1 luôn giống nhau dù email có tồn tại hay không (chống dò email).
 */
@Service
public class PasswordResetService {

    public static final int CODE_VALID_MINUTES = 10;
    public static final int RESEND_AFTER_SECONDS = 60;
    public static final int MAX_ATTEMPTS = 5;

    /** Trả về cho app để hiện "Gửi lại mã sau 00:60" và "Mã có hiệu lực trong 10 phút". */
    public record CodeSentDto(int resendAfterSeconds, int expiresInSeconds) {
    }

    private static final String INVALID = "Mã xác thực không đúng hoặc đã hết hạn";

    private final UserRepository users;
    private final PasswordResetCodeRepository codes;
    private final ResetCodeSender sender;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository users, PasswordResetCodeRepository codes, ResetCodeSender sender,
                                PasswordEncoder passwordEncoder, Clock clock) {
        this.users = users;
        this.codes = codes;
        this.sender = sender;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public CodeSentDto requestCode(String rawEmail) {
        CodeSentDto result = new CodeSentDto(RESEND_AFTER_SECONDS, CODE_VALID_MINUTES * 60);
        Optional<UserAccount> found = users.findByEmail(AuthService.normalizeEmail(rawEmail));
        if (found.isEmpty()) {
            return result;
        }
        UserAccount user = found.get();
        Instant now = clock.instant();

        // Bấm gửi lại trước 60 giây: không sinh mã mới, không gửi thêm email (chống spam hộp thư).
        // Vẫn trả cùng câu trả lời để không lộ email nào đã đăng ký.
        Optional<PasswordResetCode> last = codes.findFirstByUserIdOrderByCreatedAtDesc(user.getId());
        if (last.isPresent() && last.get().getCreatedAt().plusSeconds(RESEND_AFTER_SECONDS).isAfter(now)) {
            return result;
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        PasswordResetCode entry = new PasswordResetCode();
        entry.setUserId(user.getId());
        entry.setCodeHash(passwordEncoder.encode(code));
        entry.setCreatedAt(now);
        entry.setExpiresAt(now.plus(CODE_VALID_MINUTES, ChronoUnit.MINUTES));
        codes.save(entry);

        sender.send(user.getEmail(), user.getDisplayName(), code, CODE_VALID_MINUTES);
        return result;
    }

    /**
     * noRollbackFor: khi nhập sai, {@link #check} tăng số lần thử rồi ném ApiException —
     * giao dịch vẫn được lưu để số lần thử không bị mất (chống đoán mã).
     */
    @Transactional(noRollbackFor = ApiException.class)
    public void verifyCode(String rawEmail, String code) {
        check(rawEmail, code);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void resetPassword(String rawEmail, String code, String newPassword) {
        PasswordResetCode entry = check(rawEmail, code);
        UserAccount user = users.findById(entry.getUserId())
                .orElseThrow(() -> ApiException.badRequest(INVALID));
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw ApiException.badRequest("Mật khẩu mới phải khác mật khẩu cũ");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        // JWT ghi thời điểm cấp theo giây → mốc = giây kế tiếp: mọi token cấp tới giây hiện tại đều bị huỷ,
        // token đăng nhập lại bằng mật khẩu mới (từ giây kế tiếp) hợp lệ.
        user.setPasswordChangedAt(clock.instant().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1));
        codes.deleteAllForUser(user.getId());
    }

    /** Kiểm tra mã mới nhất của email; sai thì tăng số lần thử (được lưu lại) rồi báo lỗi. */
    private PasswordResetCode check(String rawEmail, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            throw ApiException.badRequest("Mã xác thực gồm 6 chữ số");
        }
        UserAccount user = users.findByEmail(AuthService.normalizeEmail(rawEmail))
                .orElseThrow(() -> ApiException.badRequest(INVALID));
        PasswordResetCode entry = codes.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> ApiException.badRequest(INVALID));

        if (entry.isUsed() || entry.getExpiresAt().isBefore(clock.instant())) {
            throw ApiException.badRequest(INVALID);
        }
        if (entry.getAttempts() >= MAX_ATTEMPTS) {
            throw ApiException.badRequest("Bạn đã nhập sai quá " + MAX_ATTEMPTS + " lần. Vui lòng gửi lại mã mới");
        }
        if (!passwordEncoder.matches(code, entry.getCodeHash())) {
            entry.setAttempts(entry.getAttempts() + 1);
            int left = MAX_ATTEMPTS - entry.getAttempts();
            throw ApiException.badRequest(left > 0
                    ? "Mã xác thực không đúng. Bạn còn " + left + " lần thử"
                    : "Bạn đã nhập sai quá " + MAX_ATTEMPTS + " lần. Vui lòng gửi lại mã mới");
        }
        return entry;
    }
}
