package com.aicoach.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Thử thách giữa bạn bè (Figma 40–42). Giữa kỳ dùng dữ liệu mẫu (MockSocialData); giai đoạn 2 mới có API.
 * Thành viên B có thể thêm field khi dựng màn, nhớ báo nhóm.
 */
public class Challenge {

    private String id;
    private String title;
    private String creatorName;
    private String exerciseId;
    private int targetReps;
    private String startAt;         // "2026-10-20"
    private String endAt;
    private List<String> participantNames = new ArrayList<>();

    public Challenge() {
    }

    public Challenge(String id, String title, String creatorName, String exerciseId, int targetReps,
                     String startAt, String endAt, List<String> participantNames) {
        this.id = id;
        this.title = title;
        this.creatorName = creatorName;
        this.exerciseId = exerciseId;
        this.targetReps = targetReps;
        this.startAt = startAt;
        this.endAt = endAt;
        this.participantNames = participantNames;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCreatorName() { return creatorName; }
    public String getExerciseId() { return exerciseId; }
    public int getTargetReps() { return targetReps; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
    public List<String> getParticipantNames() { return participantNames; }
}
