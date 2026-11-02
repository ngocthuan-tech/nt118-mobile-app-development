package com.aicoach.api.auth.reset;

/** Gửi mã xác thực tới người dùng. Tách thành interface để test bắt được mã mà không gửi email thật. */
public interface ResetCodeSender {

    void send(String email, String displayName, String code, int validMinutes);
}
