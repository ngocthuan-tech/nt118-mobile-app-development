package com.aicoach.common;

import android.util.Log;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestoreException;

/** Đổi lỗi Firebase sang câu tiếng Việt dễ hiểu. Không bao giờ hiện nguyên văn exception cho người dùng. */
public final class ErrorMapper {

    private static final String TAG = "ErrorMapper";

    private ErrorMapper() {
    }

    public static String toVietnamese(Exception e) {
        Log.w(TAG, "Firebase error", e);

        if (e instanceof FirebaseNetworkException) {
            return "Không có kết nối mạng. Vui lòng thử lại.";
        }
        // WeakPassword là lớp con của InvalidCredentials nên phải kiểm tra trước
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Mật khẩu quá yếu, cần tối thiểu 6 ký tự.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email hoặc mật khẩu không đúng.";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "Tài khoản không tồn tại hoặc đã bị khoá.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Email này đã được đăng ký.";
        }
        if (e instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) e).getCode()) {
                case PERMISSION_DENIED:
                    return "Bạn không có quyền thực hiện thao tác này.";
                case UNAVAILABLE:
                    return "Máy chủ tạm thời không phản hồi. Vui lòng thử lại.";
                case NOT_FOUND:
                    return "Không tìm thấy dữ liệu.";
                default:
                    break;
            }
        }
        return "Đã có lỗi xảy ra. Vui lòng thử lại.";
    }
}
