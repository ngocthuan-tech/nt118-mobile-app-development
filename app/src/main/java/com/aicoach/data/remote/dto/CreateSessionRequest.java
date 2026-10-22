package com.aicoach.data.remote.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Body của POST /sessions — Trưởng nhóm gửi ở màn Kết quả buổi tập (Figma 35).
 * startedAt dạng ISO UTC ("2026-10-22T12:00:00Z"); để null thì server lấy giờ hiện tại.
 * calories để null thì server tự tính theo bài tập.
 */
public class CreateSessionRequest {

    public String exerciseId;
    public String startedAt;
    public int durationSec;
    public int targetReps;
    public int totalReps;
    public int correctReps;
    public Double calories;
    public Map<String, Long> formErrors = new HashMap<>();
    public String planId;
}
