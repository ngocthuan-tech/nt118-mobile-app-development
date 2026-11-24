package com.aicoach.pose;

/**
 * Các lỗi tư thế AI phát hiện được.
 *  - key   : khoá gửi lên server trong formErrors của POST /sessions (ổn định, không dấu)
 *  - label : tên ngắn hiện trên chip "Sai tư thế – ..."
 *  - voice : câu nhắc đọc bằng giọng nói
 *  - joints: khớp cần khoanh đỏ trên khung xương
 */
public enum FormIssue {

    // Squat
    SQUAT_DEPTH("squat_depth", "chưa xuống đủ sâu", "Hạ hông thấp hơn nữa!",
            Landmarks.LEFT_KNEE, Landmarks.RIGHT_KNEE),
    KNEES_IN("knees_in", "gối chụm vào trong", "Đẩy hai gối ra ngoài!",
            Landmarks.LEFT_KNEE, Landmarks.RIGHT_KNEE),
    TORSO_LEAN("torso_lean", "ngả người quá nhiều", "Giữ ngực mở, lưng thẳng!",
            Landmarks.LEFT_SHOULDER, Landmarks.RIGHT_SHOULDER),

    // Hít đất / Plank
    HIP_SAG("hip_sag", "võng hông", "Siết bụng, nâng hông lên!",
            Landmarks.LEFT_HIP, Landmarks.RIGHT_HIP),
    HIP_PIKE("hip_pike", "nhô mông quá cao", "Hạ mông xuống thành đường thẳng!",
            Landmarks.LEFT_HIP, Landmarks.RIGHT_HIP),
    PUSHUP_DEPTH("pushup_depth", "chỉ hạ nửa chừng", "Hạ ngực sâu hơn!",
            Landmarks.LEFT_ELBOW, Landmarks.RIGHT_ELBOW),

    // Lunge
    LUNGE_DEPTH("lunge_depth", "gối chưa gập đủ", "Hạ người sâu hơn!",
            Landmarks.LEFT_KNEE, Landmarks.RIGHT_KNEE),

    // Cầu mông
    BRIDGE_HEIGHT("bridge_height", "hông chưa nâng đủ", "Siết mông, đẩy hông cao hơn!",
            Landmarks.LEFT_HIP, Landmarks.RIGHT_HIP),

    // Chống ghế
    DIP_DEPTH("dip_depth", "chưa hạ đủ sâu", "Gập khuỷu tay sâu hơn!",
            Landmarks.LEFT_ELBOW, Landmarks.RIGHT_ELBOW),

    // Bật nhảy
    ARMS_LOW("arms_low", "tay chưa đưa lên hết", "Vung tay qua đầu!",
            Landmarks.LEFT_WRIST, Landmarks.RIGHT_WRIST),
    LEGS_CLOSED("legs_closed", "chân chưa dang rộng", "Bật dang chân rộng hơn!",
            Landmarks.LEFT_ANKLE, Landmarks.RIGHT_ANKLE),

    // Nâng gối
    KNEE_LOW("knee_low", "gối nâng quá thấp", "Nâng gối cao ngang hông!",
            Landmarks.LEFT_KNEE, Landmarks.RIGHT_KNEE),

    // Chạm chân
    SHORT_REACH("toe_reach", "chưa với tới mũi chân", "Gập sâu hơn, với tay tới mũi chân!",
            Landmarks.LEFT_WRIST, Landmarks.RIGHT_WRIST);

    public final String key;
    public final String label;
    public final String voice;
    public final int[] joints;

    /** Tìm lỗi theo key server trả về (formErrors); null nếu key lạ. */
    public static FormIssue fromKey(String key) {
        for (FormIssue i : values()) {
            if (i.key.equals(key)) return i;
        }
        return null;
    }

    FormIssue(String key, String label, String voice, int... joints) {
        this.key = key;
        this.label = label;
        this.voice = voice;
        this.joints = joints;
    }
}
