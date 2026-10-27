package com.aicoach.ui.auth;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;

import com.aicoach.R;

/** Điều hướng sau khi đăng nhập / đăng ký thành công (dùng chung cho 06 và 12). */
final class AuthNavigation {

    private AuthNavigation() {
    }

    /**
     * Hồ sơ đã hoàn tất → Trang chủ (global action đã xoá luồng đăng nhập khỏi back stack).
     * Chưa hoàn tất → Khảo sát 13, bỏ các màn đăng nhập phía trước (đã có token, Back không quay lại).
     */
    static void afterSignIn(@NonNull NavController nav, boolean profileCompleted) {
        if (profileCompleted) {
            nav.navigate(R.id.action_global_to_main);
        } else {
            nav.navigate(R.id.surveyBodyFragment, null, new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_auth, false)
                    .build());
        }
    }
}
