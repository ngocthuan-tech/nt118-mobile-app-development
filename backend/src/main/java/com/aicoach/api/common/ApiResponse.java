package com.aicoach.api.common;

/**
 * Envelope chung cho MỌI response: { "success": true/false, "message": "...", "data": ... }.
 * App Android chỉ cần một lớp ApiResponse&lt;T&gt; để đọc mọi API.
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
