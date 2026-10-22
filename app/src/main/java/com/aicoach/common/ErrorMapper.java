package com.aicoach.common;

import android.util.Log;

import androidx.annotation.NonNull;

import com.aicoach.data.remote.ApiResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Đổi lỗi mạng / lỗi HTTP sang câu tiếng Việt cho người dùng.
 * Server đã trả sẵn "message" tiếng Việt trong envelope — ưu tiên dùng câu đó.
 * Không bao giờ hiện nguyên văn exception lên màn hình.
 */
public final class ErrorMapper {

    private static final String TAG = "ErrorMapper";
    private static final Gson GSON = new Gson();

    private ErrorMapper() {
    }

    /** Request không tới được server (mất mạng, hết thời gian chờ...). */
    @NonNull
    public static String fromThrowable(@NonNull Throwable t) {
        Log.w(TAG, "Network error", t);
        if (t instanceof SocketTimeoutException) {
            return "Máy chủ phản hồi chậm. Vui lòng thử lại.";
        }
        if (t instanceof IOException) {
            return "Không có kết nối mạng. Vui lòng thử lại.";
        }
        return "Đã có lỗi xảy ra. Vui lòng thử lại.";
    }

    /** Server trả lỗi (4xx/5xx) hoặc success = false. */
    @NonNull
    public static String fromResponse(@NonNull Response<?> response) {
        Object body = response.body();
        if (body instanceof ApiResponse && ((ApiResponse<?>) body).getMessage() != null) {
            return ((ApiResponse<?>) body).getMessage();
        }
        String serverMessage = readErrorMessage(response.errorBody());
        if (serverMessage != null) {
            return serverMessage;
        }
        switch (response.code()) {
            case 400: return "Dữ liệu không hợp lệ.";
            case 401: return "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.";
            case 403: return "Bạn không có quyền thực hiện thao tác này.";
            case 404: return "Không tìm thấy dữ liệu.";
            case 409: return "Dữ liệu đã tồn tại.";
            default:  return "Hệ thống đang bận, vui lòng thử lại sau.";
        }
    }

    private static String readErrorMessage(ResponseBody errorBody) {
        if (errorBody == null) return null;
        try {
            ApiResponse<?> parsed = GSON.fromJson(errorBody.charStream(), ApiResponse.class);
            return parsed != null ? parsed.getMessage() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
