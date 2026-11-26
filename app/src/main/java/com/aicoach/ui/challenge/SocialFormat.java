package com.aicoach.ui.challenge;

import androidx.annotation.Nullable;
import com.aicoach.data.model.Challenge;
import com.aicoach.data.model.Social;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Quy tắc hiển thị thuần Java (không phụ thuộc Android) để kiểm thử bằng JUnit trên máy. */
public final class SocialFormat {

    /** Server sinh 6 ký tự từ bảng chữ bỏ 0/O, 1/I; app chấp nhận rộng hơn và để server kiểm tra chính xác. */
    private static final Pattern CODE = Pattern.compile("[A-Z0-9]{6}");
    public static final int MAX_INVITES_PER_SEND = 20;
    /** Ngày của thông báo tính theo giờ Việt Nam, giống server. */
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private SocialFormat() {
    }

    /** Chữ cái đầu cho avatar tròn: "đức huy" → "Đ"; rỗng → "?". */
    public static String initial(@Nullable String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String t = name.trim();
        return new String(Character.toChars(t.codePointAt(0))).toUpperCase(Locale.forLanguageTag("vi"));
    }

    /** " 7q2 kx9 " → "7Q2KX9"; "7Q2-KX9" → "7Q2KX9"; sai định dạng → null (báo lỗi ngay, không gọi server). */
    @Nullable
    public static String normalizeCode(@Nullable String raw) {
        if (raw == null) return null;
        String code = raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return CODE.matcher(code).matches() ? code : null;
    }

    /** Thanh tiến độ hôm nay: 0–100, vượt mục tiêu vẫn là 100. */
    public static int percent(int done, int target) {
        if (target <= 0 || done <= 0) return 0;
        return (int) Math.min(100, Math.round(done * 100.0 / target));
    }

    /**
     * Danh sách xếp hạng để vẽ: các dòng server trả, thêm dòng "Bạn" ở cuối nếu mình nằm ngoài top
     * (bảng toàn quốc chỉ trả 50 người đầu).
     */
    public static List<Social.LeaderboardEntry> rows(@Nullable Social.Leaderboard board) {
        List<Social.LeaderboardEntry> out = new ArrayList<>();
        if (board == null) return out;
        out.addAll(board.getItems());
        Social.LeaderboardEntry me = board.getMe();
        if (me == null) return out;
        for (Social.LeaderboardEntry e : out) {
            if (e.isMe() || (me.getUserId() != null && me.getUserId().equals(e.getUserId()))) return out;
        }
        out.add(me);
        return out;
    }

    /** Thử thách đang hiển thị trên thẻ: id đã chọn nếu còn trong danh sách, không thì cái đầu tiên. */
    @Nullable
    public static Challenge pick(@Nullable List<Challenge> list, @Nullable String selectedId) {
        if (list == null || list.isEmpty()) return null;
        if (selectedId != null) {
            for (Challenge c : list) if (selectedId.equals(c.getId())) return c;
        }
        return list.get(0);
    }

    /** Ba chữ cái đầu của thành viên và số người còn lại ("+3"); không ai → mảng rỗng. */
    public static List<String> avatars(@Nullable Challenge c, int max) {
        List<String> out = new ArrayList<>();
        if (c == null) return out;
        for (Challenge.Member m : c.getMembers()) {
            if (out.size() == max) break;
            out.add(initial(m.getDisplayName()));
        }
        return out;
    }

    public static int moreMembers(@Nullable Challenge c, int shown) {
        return c == null ? 0 : Math.max(0, c.getMemberCount() - shown);
    }

    /** "2026-11-26T03:15:00Z" → thời điểm (ms); sai định dạng → -1. */
    public static long epochMillis(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) return -1;
        try {
            return Instant.parse(iso).toEpochMilli();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    /** Ngày (giờ Việt Nam) để chia nhóm "Hôm nay" / "Hôm qua" ở màn Thông báo; sai định dạng → null. */
    @Nullable
    public static LocalDate dateOf(@Nullable String iso) {
        long ms = epochMillis(iso);
        return ms < 0 ? null : Instant.ofEpochMilli(ms).atZone(ZONE).toLocalDate();
    }
}
