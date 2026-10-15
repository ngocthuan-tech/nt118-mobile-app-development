package com.aicoach.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

/** Document challenges/{id}. Điểm từng người nằm ở challenges/{id}/scores/{uid}. */
public class Challenge {

    @DocumentId
    private String id;
    private String title;
    private String creatorUid;
    private String exerciseId;
    private int targetReps;
    private Timestamp startAt;
    private Timestamp endAt;
    private List<String> participantUids = new ArrayList<>();

    public Challenge() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatorUid() { return creatorUid; }
    public void setCreatorUid(String creatorUid) { this.creatorUid = creatorUid; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public Timestamp getStartAt() { return startAt; }
    public void setStartAt(Timestamp startAt) { this.startAt = startAt; }

    public Timestamp getEndAt() { return endAt; }
    public void setEndAt(Timestamp endAt) { this.endAt = endAt; }

    public List<String> getParticipantUids() { return participantUids; }
    public void setParticipantUids(List<String> participantUids) { this.participantUids = participantUids; }
}
