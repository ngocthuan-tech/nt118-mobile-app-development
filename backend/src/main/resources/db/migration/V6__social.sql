-- =====================================================================
-- V6: Cộng đồng — bạn bè, thử thách, lời mời, thông báo (Figma 22, 40, 41, 42).
-- Tiến độ thử thách KHÔNG lưu riêng: tính từ workout_sessions (cùng bài, trong khoảng ngày của thử thách)
-- nên luôn khớp với lịch sử tập, không lệch số.
-- =====================================================================

-- Bạn bè: lưu 2 chiều (A→B và B→A) để truy vấn "bạn của tôi" chỉ cần 1 điều kiện.
-- Hai người thành bạn khi một người chấp nhận lời mời thử thách của người kia, hoặc tham gia bằng mã mời.
CREATE TABLE friendships (
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    friend_id  VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_friendship UNIQUE (user_id, friend_id),
    CONSTRAINT ck_friendship_not_self CHECK (user_id <> friend_id)
);

-- Thử thách: mỗi ngày tập đủ daily_target lần đúng của exercise_id, trong `days` ngày (giờ Việt Nam).
CREATE TABLE challenges (
    id            VARCHAR(36) PRIMARY KEY,
    title         VARCHAR(50) NOT NULL,
    exercise_id   VARCHAR(40) NOT NULL REFERENCES exercises (id),
    daily_target  INTEGER     NOT NULL CHECK (daily_target BETWEEN 10 AND 1000),
    days          INTEGER     NOT NULL CHECK (days IN (3, 7, 14, 30)),
    start_date    DATE        NOT NULL,
    end_date      DATE        NOT NULL,
    private_group BOOLEAN     NOT NULL DEFAULT TRUE,
    invite_code   VARCHAR(12) NOT NULL,
    creator_id    VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_challenge_invite_code UNIQUE (invite_code),
    CONSTRAINT ck_challenge_dates CHECK (end_date >= start_date)
);

-- Thành viên thử thách (người tạo cũng là thành viên)
CREATE TABLE challenge_members (
    id           VARCHAR(36) PRIMARY KEY,
    challenge_id VARCHAR(36) NOT NULL REFERENCES challenges (id) ON DELETE CASCADE,
    user_id      VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    joined_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_challenge_member UNIQUE (challenge_id, user_id)
);
CREATE INDEX idx_challenge_members_user ON challenge_members (user_id);

-- Lời mời: PENDING → ACCEPTED | DECLINED. Mỗi người chỉ có 1 lời mời cho 1 thử thách (mời lại thì mở lại).
CREATE TABLE challenge_invites (
    id           VARCHAR(36) PRIMARY KEY,
    challenge_id VARCHAR(36) NOT NULL REFERENCES challenges (id) ON DELETE CASCADE,
    inviter_id   VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    invitee_id   VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status       VARCHAR(10) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED')),
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    responded_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT uk_challenge_invite UNIQUE (challenge_id, invitee_id)
);

-- Thông báo trong app (Figma 22). type: CHALLENGE_INVITE | CHALLENGE_JOINED | CHALLENGE_DECLINED | SYSTEM
-- ref_id: lời mời (CHALLENGE_INVITE) hoặc thử thách (CHALLENGE_JOINED / DECLINED)
CREATE TABLE notifications (
    id         VARCHAR(36)  PRIMARY KEY,
    user_id    VARCHAR(36)  NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type       VARCHAR(20)  NOT NULL,
    title      VARCHAR(120) NOT NULL,
    body       VARCHAR(300) NOT NULL,
    ref_id     VARCHAR(36),
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_notifications_user_time ON notifications (user_id, created_at);

-- Tính tiến độ thử thách: buổi tập của nhiều người, cùng bài, trong khoảng ngày
CREATE INDEX idx_session_exercise_time ON workout_sessions (exercise_id, started_at);

-- Bảng xếp hạng toàn quốc: lấy top 50 theo XP và đếm số người nhiều XP hơn mình — không quét cả bảng users
CREATE INDEX idx_users_total_xp ON users (total_xp DESC, created_at);
