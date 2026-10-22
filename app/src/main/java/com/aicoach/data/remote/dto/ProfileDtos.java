package com.aicoach.data.remote.dto;

/** Request của PUT /users/me và PUT /users/me/password. */
public final class ProfileDtos {

    private ProfileDtos() {
    }

    /**
     * Chỉ field nào được gán mới gửi lên (Gson bỏ qua field null) → server chỉ sửa đúng field đó.
     * Ví dụ màn Khảo sát 1/3: new UpdateProfileRequest().heightCm(170.0).weightKg(65.0)
     * Chọn giáo án:           new UpdateProfileRequest().activePlanId("fat-burn-4w")
     */
    public static class UpdateProfileRequest {
        private String displayName;
        private String avatarUrl;
        private Double heightCm;
        private Double weightKg;
        private String goal;
        private String level;
        private Integer weeklyGoalSessions;
        private Boolean profileCompleted;
        private String activePlanId;

        public UpdateProfileRequest displayName(String v) { displayName = v; return this; }
        public UpdateProfileRequest avatarUrl(String v) { avatarUrl = v; return this; }
        public UpdateProfileRequest heightCm(Double v) { heightCm = v; return this; }
        public UpdateProfileRequest weightKg(Double v) { weightKg = v; return this; }
        public UpdateProfileRequest goal(String v) { goal = v; return this; }
        public UpdateProfileRequest level(String v) { level = v; return this; }
        public UpdateProfileRequest weeklyGoalSessions(Integer v) { weeklyGoalSessions = v; return this; }
        public UpdateProfileRequest profileCompleted(Boolean v) { profileCompleted = v; return this; }
        /** Truyền "" để bỏ giáo án đang theo. */
        public UpdateProfileRequest activePlanId(String v) { activePlanId = v; return this; }
    }

    public static class ChangePasswordRequest {
        final String currentPassword;
        final String newPassword;

        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
    }
}
