package com.aicoach.api.social;

import com.aicoach.api.common.ApiException;
import com.aicoach.api.social.SocialDtos.NotificationDto;
import com.aicoach.api.social.SocialDtos.NotificationList;
import com.aicoach.api.social.SocialDtos.ReadAllResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Thông báo trong app (Figma 22): tạo, liệt kê theo bộ lọc Tất cả / Thử thách / Hệ thống, đánh dấu đã đọc. */
@Service
public class NotificationService {

    static final int PAGE_SIZE = 50;
    static final List<String> CHALLENGE_TYPES = List.of(
            Notification.CHALLENGE_INVITE, Notification.CHALLENGE_JOINED, Notification.CHALLENGE_DECLINED);
    static final List<String> SYSTEM_TYPES = List.of(Notification.SYSTEM);

    private final NotificationRepository notifications;
    private final ChallengeInviteRepository invites;

    public NotificationService(NotificationRepository notifications, ChallengeInviteRepository invites) {
        this.notifications = notifications;
        this.invites = invites;
    }

    /** Tạo thông báo cho một người (dùng chung cho thử thách, chào mừng...). */
    @Transactional
    public Notification create(String userId, String type, String title, String body, String refId) {
        return notifications.save(new Notification(userId, type, title, body, refId));
    }

    /** Lời chào khi vừa đăng ký — tài khoản mới mở màn Thông báo không bị trống trơn. */
    @Transactional
    public void welcome(String userId) {
        create(userId, Notification.SYSTEM, "Chào mừng bạn đến với AI Coach",
                "Tập cùng AI Camera, giữ chuỗi ngày tập và rủ bạn bè cùng thử thách nhé!", null);
    }

    /** type: all (mặc định) | challenge | system. Mới nhất trước, tối đa 50. */
    @Transactional(readOnly = true)
    public NotificationList list(String userId, String type) {
        PageRequest page = PageRequest.of(0, PAGE_SIZE);
        List<Notification> found;
        if ("challenge".equalsIgnoreCase(type)) {
            found = notifications.findByUserIdAndTypeInOrderByCreatedAtDesc(userId, CHALLENGE_TYPES, page);
        } else if ("system".equalsIgnoreCase(type)) {
            found = notifications.findByUserIdAndTypeInOrderByCreatedAtDesc(userId, SYSTEM_TYPES, page);
        } else {
            found = notifications.findByUserIdOrderByCreatedAtDesc(userId, page);
        }
        return new NotificationList(found.stream().map(this::toDto).toList(),
                notifications.countByUserIdAndReadFalse(userId));
    }

    @Transactional
    public ReadAllResult readAll(String userId) {
        List<Notification> unread = notifications.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notifications.saveAll(unread);
        return new ReadAllResult(unread.size());
    }

    @Transactional
    public NotificationDto read(String userId, String id) {
        Notification n = require(userId, id);
        if (!n.isRead()) {
            n.setRead(true);
            notifications.save(n);
        }
        return toDto(n);
    }

    /** Thông báo của đúng người đang đăng nhập; của người khác thì coi như không tồn tại. */
    public Notification require(String userId, String id) {
        return notifications.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy thông báo"));
    }

    public NotificationDto toDto(Notification n) {
        String inviteStatus = null;
        String challengeId = null;
        if (Notification.CHALLENGE_INVITE.equals(n.getType()) && n.getRefId() != null) {
            ChallengeInvite inv = invites.findById(n.getRefId()).orElse(null);
            if (inv != null) {
                inviteStatus = inv.getStatus();
                challengeId = inv.getChallengeId();
            }
        } else if (n.isChallenge()) {
            challengeId = n.getRefId();
        }
        return new NotificationDto(n.getId(), n.getType(), n.getTitle(), n.getBody(), n.isRead(),
                n.getCreatedAt().toString(), inviteStatus, challengeId);
    }
}
