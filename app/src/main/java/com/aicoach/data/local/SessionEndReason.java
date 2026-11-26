package com.aicoach.data.local;

public enum SessionEndReason {
    EXPIRED, PASSWORD_CHANGED, PASSWORD_UPDATED_HERE,
    /** Quản trị viên khoá tài khoản trên web admin (server trả X-Session-Reason: ACCOUNT_LOCKED). */
    ACCOUNT_LOCKED
}
