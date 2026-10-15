package com.aicoach.common;

/** Tên collection Firestore dùng chung. Không ai gõ tay chuỗi "users", "sessions"... ở chỗ khác. */
public final class FirestorePaths {

    private FirestorePaths() {
    }

    public static final String USERS = "users";
    public static final String EXERCISES = "exercises";
    public static final String PLANS = "plans";
    public static final String FRIEND_REQUESTS = "friendRequests";
    public static final String CHALLENGES = "challenges";

    // Sub-collection
    public static final String SESSIONS = "sessions";   // users/{uid}/sessions
    public static final String BADGES = "badges";       // users/{uid}/badges
    public static final String FRIENDS = "friends";     // users/{uid}/friends
    public static final String SCORES = "scores";       // challenges/{id}/scores
}
