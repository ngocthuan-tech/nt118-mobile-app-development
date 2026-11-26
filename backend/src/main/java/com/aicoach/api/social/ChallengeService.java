package com.aicoach.api.social;

import com.aicoach.api.common.ApiException;
import com.aicoach.api.common.VnTime;
import com.aicoach.api.exercise.Exercise;
import com.aicoach.api.exercise.ExerciseRepository;
import com.aicoach.api.progress.ProgressService;
import com.aicoach.api.session.WorkoutSessionRepository;
import com.aicoach.api.social.SocialDtos.CandidateDto;
import com.aicoach.api.social.SocialDtos.ChallengeDto;
import com.aicoach.api.social.SocialDtos.CreateChallengeRequest;
import com.aicoach.api.social.SocialDtos.InviteRequest;
import com.aicoach.api.social.SocialDtos.InviteResult;
import com.aicoach.api.social.SocialDtos.MemberDto;
import com.aicoach.api.social.SocialDtos.NotificationDto;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Thử thách giữa bạn bè (Figma 40, 41, 42 và lời mời ở màn Thông báo 22).
 *
 * Quy tắc:
 *  - Thử thách bắt đầu từ hôm nay (giờ Việt Nam), kéo dài 3 / 7 / 14 / 30 ngày; người tạo tự là thành viên.
 *  - Mỗi ngày "hoàn thành" khi tổng số lần ĐÚNG của bài đó trong ngày ≥ mục tiêu — tính từ lịch sử buổi tập,
 *    không có số liệu nào nhập tay.
 *  - Chỉ thành viên mới xem, mời người khác; mời lần nữa người đã từ chối thì lời mời được mở lại.
 *  - Công khai: ai có mã mời cũng vào được. Riêng tư ("Chỉ người được mời"): không lộ mã, chỉ vào qua lời mời.
 *  - Chấp nhận lời mời / tham gia bằng mã → thành bạn bè với người mời (hoặc người tạo).
 *  - Mỗi người tham gia tối đa 10 thử thách chưa kết thúc cùng lúc.
 */
@Service
public class ChallengeService {

    static final Set<Integer> ALLOWED_DAYS = Set.of(3, 7, 14, 30);
    static final int MAX_OPEN_CHALLENGES = 10;
    static final int LIST_LIMIT = 20;
    /** Mã mời 6 ký tự (~1 tỷ tổ hợp — không dò thử được), bỏ ký tự dễ nhầm 0/O, 1/I khi đọc cho nhau. */
    static final int CODE_LENGTH = 6;
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private final ChallengeRepository challenges;
    private final ChallengeMemberRepository members;
    private final ChallengeInviteRepository invites;
    private final NotificationService notifications;
    private final FriendService friends;
    private final UserRepository users;
    private final ExerciseRepository exercises;
    private final WorkoutSessionRepository sessions;
    private final ProgressService progress;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public ChallengeService(ChallengeRepository challenges, ChallengeMemberRepository members,
                            ChallengeInviteRepository invites, NotificationService notifications,
                            FriendService friends, UserRepository users, ExerciseRepository exercises,
                            WorkoutSessionRepository sessions, ProgressService progress, Clock clock) {
        this.challenges = challenges;
        this.members = members;
        this.invites = invites;
        this.notifications = notifications;
        this.friends = friends;
        this.users = users;
        this.exercises = exercises;
        this.sessions = sessions;
        this.progress = progress;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ Xem

    /**
     * Thử thách mình tham gia: đang diễn ra trước, sắp diễn ra, rồi đã kết thúc; trong mỗi nhóm, cái mình vừa
     * tạo / vừa tham gia đứng đầu (bấm "Tham gia" ở Thông báo xong mở tab Thử thách là thấy ngay).
     */
    @Transactional(readOnly = true)
    public List<ChallengeDto> list(String userId) {
        Map<String, Instant> joinedAt = new HashMap<>();
        for (ChallengeMember m : members.findByUserId(userId)) joinedAt.put(m.getChallengeId(), m.getJoinedAt());
        if (joinedAt.isEmpty()) return List.of();
        LocalDate today = today();
        return challenges.findByIdIn(joinedAt.keySet()).stream()
                .sorted(Comparator.comparingInt((Challenge c) -> statusOrder(c, today))
                        .thenComparing((Challenge c) -> joinedAt.get(c.getId()),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(LIST_LIMIT)
                .map(c -> toDto(c, userId, today))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChallengeDto get(String userId, String challengeId) {
        return toDto(requireMember(userId, challengeId), userId, today());
    }

    // ------------------------------------------------------------------ Tạo

    @Transactional
    public ChallengeDto create(String userId, CreateChallengeRequest req) {
        String title = req.title() == null ? "" : req.title().trim();
        if (title.length() < 3 || title.length() > 50) {
            throw ApiException.badRequest("Tên thử thách 3–50 ký tự");
        }
        if (req.dailyTarget() < 10 || req.dailyTarget() > 1000) {
            throw ApiException.badRequest("Mục tiêu mỗi ngày từ 10 đến 1000");
        }
        if (!ALLOWED_DAYS.contains(req.days())) {
            throw ApiException.badRequest("Thời gian thử thách chỉ chọn 3, 7, 14 hoặc 30 ngày");
        }
        Exercise ex = exercises.findById(req.exerciseId())
                .orElseThrow(() -> ApiException.badRequest("Bài tập không tồn tại"));
        requireRoomForAnother(userId);

        LocalDate today = today();
        Challenge c = new Challenge();
        c.setTitle(title);
        c.setExerciseId(ex.getId());
        c.setDailyTarget(req.dailyTarget());
        c.setDays(req.days());
        c.setStartDate(today);
        c.setEndDate(today.plusDays(req.days() - 1L));
        c.setPrivateGroup(req.privateGroup());
        c.setInviteCode(newInviteCode());
        c.setCreatorId(userId);
        challenges.save(c);
        members.save(new ChallengeMember(c.getId(), userId));
        return toDto(c, userId, today);
    }

    // ------------------------------------------------------------------ Mời

    /**
     * Danh sách người có thể mời (Figma 42): bạn bè, lọc theo tên (không dấu cũng tìm được);
     * gõ đúng email của một người dùng thì người đó cũng hiện ra (mời người chưa là bạn).
     */
    @Transactional(readOnly = true)
    public List<CandidateDto> candidates(String userId, String challengeId, String q) {
        Challenge c = requireMember(userId, challengeId);
        Set<String> memberIds = memberIds(c.getId());
        Map<String, ChallengeInvite> inviteByUser = new HashMap<>();
        for (ChallengeInvite inv : invites.findByChallengeId(c.getId())) inviteByUser.put(inv.getInviteeId(), inv);

        String query = q == null ? "" : q.trim();
        String needle = normalize(query);
        List<UserAccount> pool = new ArrayList<>();
        Set<String> friendIds = new HashSet<>();
        for (UserAccount f : friends.friendAccounts(userId)) {
            friendIds.add(f.getId());
            if (needle.isEmpty() || normalize(f.getDisplayName()).contains(needle)) pool.add(f);
        }
        if (query.contains("@")) {
            users.findByEmail(query.toLowerCase(Locale.ROOT))
                    .filter(u -> !u.getId().equals(userId) && !friendIds.contains(u.getId()))
                    .ifPresent(pool::add);
        }
        pool.sort(Comparator.comparing(u -> normalize(u.getDisplayName())));

        List<CandidateDto> out = new ArrayList<>();
        for (UserAccount u : pool) {
            String status = memberIds.contains(u.getId()) ? "MEMBER"
                    : Optional.ofNullable(inviteByUser.get(u.getId())).filter(ChallengeInvite::isPending).isPresent()
                    ? "INVITED" : "NONE";
            out.add(new CandidateDto(u.getId(), u.getDisplayName(), progress.effectiveStreak(u),
                    friendIds.contains(u.getId()), status));
        }
        return out;
    }

    @Transactional
    public InviteResult invite(String userId, String challengeId, InviteRequest req) {
        Challenge c = requireMember(userId, challengeId);
        if (isEnded(c, today())) throw ApiException.badRequest("Thử thách đã kết thúc, không mời thêm được");
        String inviterName = displayName(userId);

        int invited = 0;
        int skipped = 0;
        for (String targetId : new LinkedHashSet<>(req.userIds())) {
            if (targetId == null || targetId.equals(userId) || !users.existsById(targetId)
                    || members.existsByChallengeIdAndUserId(c.getId(), targetId)) {
                skipped++;
                continue;
            }
            Optional<ChallengeInvite> existing = invites.findByChallengeIdAndInviteeId(c.getId(), targetId);
            ChallengeInvite inv;
            if (existing.isPresent()) {
                inv = existing.get();
                if (inv.isPending()) {
                    skipped++;
                    continue;
                }
                inv.reopen(userId);
            } else {
                inv = new ChallengeInvite(c.getId(), userId, targetId);
            }
            invites.save(inv);
            notifications.create(targetId, Notification.CHALLENGE_INVITE,
                    inviterName + " mời bạn tham gia thử thách",
                    "“" + c.getTitle() + "” • " + c.getDailyTarget() + " " + unit(c) + "/ngày trong " + c.getDays()
                            + " ngày", inv.getId());
            invited++;
        }
        return new InviteResult(invited, skipped);
    }

    // ------------------------------------------------------------------ Tham gia

    /** Tham gia bằng mã mời "7Q2KX9" (gõ "7q2k x9" cũng được). Thử thách riêng tư không nhận mã. */
    @Transactional
    public ChallengeDto joinByCode(String userId, String code) {
        String normalized = normalizeCode(code);
        if (normalized.length() != CODE_LENGTH) throw ApiException.notFound("Mã mời không đúng");
        Challenge c = challenges.findByInviteCode(normalized)
                .filter(ch -> !ch.isPrivateGroup() || members.existsByChallengeIdAndUserId(ch.getId(), userId))
                .orElseThrow(() -> ApiException.notFound("Mã mời không đúng"));
        LocalDate today = today();
        if (isEnded(c, today)) throw ApiException.badRequest("Thử thách đã kết thúc");
        if (!members.existsByChallengeIdAndUserId(c.getId(), userId)) {
            requireRoomForAnother(userId);
            members.save(new ChallengeMember(c.getId(), userId));
            friends.befriend(userId, c.getCreatorId());
            invites.findByChallengeIdAndInviteeId(c.getId(), userId)
                    .filter(ChallengeInvite::isPending)
                    .ifPresent(inv -> {
                        inv.respond(true);
                        invites.save(inv);
                    });
            if (!c.getCreatorId().equals(userId)) {
                notifications.create(c.getCreatorId(), Notification.CHALLENGE_JOINED,
                        displayName(userId) + " vừa tham gia thử thách của bạn", "“" + c.getTitle() + "”", c.getId());
            }
        }
        return toDto(c, userId, today);
    }

    /** Bấm Tham gia / Từ chối trên thông báo lời mời. Bấm lại lần 2 không đổi gì (trả trạng thái hiện tại). */
    @Transactional
    public NotificationDto respond(String userId, String notificationId, boolean accept) {
        Notification n = notifications.require(userId, notificationId);
        if (!Notification.CHALLENGE_INVITE.equals(n.getType()) || n.getRefId() == null) {
            throw ApiException.badRequest("Thông báo này không phải lời mời thử thách");
        }
        ChallengeInvite inv = invites.findById(n.getRefId())
                .filter(i -> i.getInviteeId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("Lời mời không còn tồn tại"));
        n.setRead(true);
        if (inv.isPending()) {
            Challenge c = challenges.findById(inv.getChallengeId())
                    .orElseThrow(() -> ApiException.notFound("Thử thách không còn tồn tại"));
            String me = displayName(userId);
            if (accept) {
                if (isEnded(c, today())) throw ApiException.badRequest("Thử thách đã kết thúc");
                if (!members.existsByChallengeIdAndUserId(c.getId(), userId)) {
                    requireRoomForAnother(userId);
                    members.save(new ChallengeMember(c.getId(), userId));
                }
                friends.befriend(userId, inv.getInviterId());
                inv.respond(true);
                notifications.create(inv.getInviterId(), Notification.CHALLENGE_JOINED,
                        me + " đã tham gia thử thách", "“" + c.getTitle() + "”", c.getId());
            } else {
                inv.respond(false);
                notifications.create(inv.getInviterId(), Notification.CHALLENGE_DECLINED,
                        me + " đã từ chối lời mời", "“" + c.getTitle() + "”", c.getId());
            }
            invites.save(inv);
        }
        return notifications.toDto(n);
    }

    // ------------------------------------------------------------------ Tiến độ

    private ChallengeDto toDto(Challenge c, String userId, LocalDate today) {
        List<ChallengeMember> ms = members.findByChallengeIdOrderByJoinedAtAsc(c.getId());
        List<String> memberIds = ms.stream().map(ChallengeMember::getUserId).toList();
        Set<String> peopleIds = new HashSet<>(memberIds);
        peopleIds.add(c.getCreatorId());
        Map<String, UserAccount> people = new HashMap<>();
        for (UserAccount u : users.findAllById(peopleIds)) people.put(u.getId(), u);

        Instant from = VnTime.startOf(c.getStartDate());
        Instant to = VnTime.startOf(c.getEndDate().plusDays(1));
        List<ChallengeProgress.Point> points = memberIds.isEmpty() ? List.of()
                : sessions.findByUserIdInAndExerciseIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                        memberIds, c.getExerciseId(), from, to).stream()
                .map(s -> new ChallengeProgress.Point(s.getUserId(), VnTime.dateOf(s.getStartedAt()),
                        s.getCorrectReps()))
                .toList();
        Map<String, ChallengeProgress.Result> progressByUser = ChallengeProgress.compute(
                c.getStartDate(), c.getEndDate(), c.getDailyTarget(), today, points);

        List<MemberDto> memberDtos = new ArrayList<>();
        for (ChallengeMember m : ms) {
            ChallengeProgress.Result r = ChallengeProgress.of(progressByUser, m.getUserId());
            memberDtos.add(new MemberDto(m.getUserId(), nameOf(people.get(m.getUserId())), r.todayReps(),
                    r.totalReps(), r.completedDays(), m.getUserId().equals(userId)));
        }
        memberDtos.sort(Comparator.comparingInt(MemberDto::totalReps).reversed()
                .thenComparing(MemberDto::displayName));

        Exercise ex = exercises.findById(c.getExerciseId()).orElse(null);
        ChallengeProgress.Result mine = ChallengeProgress.of(progressByUser, userId);
        return new ChallengeDto(c.getId(), c.getTitle(), c.getExerciseId(),
                ex != null ? ex.getName() : c.getExerciseId(),
                ex != null ? ex.getMeasureType() : "reps",
                c.getDailyTarget(), c.getDays(), c.getStartDate().toString(), c.getEndDate().toString(),
                ChallengeProgress.daysLeft(c.getStartDate(), c.getEndDate(), today),
                ChallengeProgress.status(c.getStartDate(), c.getEndDate(), today),
                c.isPrivateGroup(), c.isPrivateGroup() ? null : c.getInviteCode(), nameOf(people.get(c.getCreatorId())),
                c.getCreatorId().equals(userId), ms.size(), mine.todayReps(), mine.completedDays(), memberDtos);
    }

    // ------------------------------------------------------------------ Tiện ích

    /** Thử thách mà người dùng là thành viên; người ngoài thì báo "không tìm thấy" (không lộ thử thách riêng). */
    private Challenge requireMember(String userId, String challengeId) {
        Challenge c = challenges.findById(challengeId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy thử thách"));
        if (!members.existsByChallengeIdAndUserId(c.getId(), userId)) {
            throw ApiException.notFound("Không tìm thấy thử thách");
        }
        return c;
    }

    private void requireRoomForAnother(String userId) {
        List<String> ids = members.findByUserId(userId).stream().map(ChallengeMember::getChallengeId).toList();
        if (ids.isEmpty()) return;
        LocalDate today = today();
        long open = challenges.findByIdIn(ids).stream().filter(c -> !isEnded(c, today)).count();
        if (open >= MAX_OPEN_CHALLENGES) {
            throw ApiException.badRequest("Bạn đang tham gia tối đa " + MAX_OPEN_CHALLENGES
                    + " thử thách chưa kết thúc");
        }
    }

    private Set<String> memberIds(String challengeId) {
        Set<String> ids = new HashSet<>();
        for (ChallengeMember m : members.findByChallengeIdOrderByJoinedAtAsc(challengeId)) ids.add(m.getUserId());
        return ids;
    }

    private String newInviteCode() {
        for (int attempt = 0; attempt < 30; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            String code = sb.toString();
            if (!challenges.existsByInviteCode(code)) return code;
        }
        throw ApiException.conflict("Không tạo được mã mời, vui lòng thử lại");
    }

    private String unit(Challenge c) {
        return exercises.findById(c.getExerciseId())
                .map(e -> "seconds".equals(e.getMeasureType()) ? "giây" : "lần")
                .orElse("lần");
    }

    private String displayName(String userId) {
        return users.findById(userId).map(UserAccount::getDisplayName).orElse("Một người bạn");
    }

    private static String nameOf(UserAccount u) {
        return u == null ? "Người dùng" : u.getDisplayName();
    }

    private static boolean isEnded(Challenge c, LocalDate today) {
        return today.isAfter(c.getEndDate());
    }

    private static int statusOrder(Challenge c, LocalDate today) {
        switch (ChallengeProgress.status(c.getStartDate(), c.getEndDate(), today)) {
            case "ACTIVE":
                return 0;
            case "UPCOMING":
                return 1;
            default:
                return 2;
        }
    }

    private LocalDate today() {
        return VnTime.today(clock);
    }

    /** Mã người dùng gõ: bỏ khoảng trắng, gạch nối…, viết hoa. */
    static String normalizeCode(String code) {
        return code == null ? "" : code.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    /** Tìm tên không dấu: "hoang nam" khớp "Hoàng Nam", "duc" khớp "Đức". */
    static String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
    }
}
