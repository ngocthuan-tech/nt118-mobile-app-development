package com.aicoach.common;

/**
 * Bọc mọi kết quả trả về từ Repository: đang tải / thành công / lỗi.
 * Fragment chỉ cần quan sát status để hiện vòng xoay, dữ liệu hoặc thông báo lỗi.
 */
public class Resource<T> {

    public enum Status { LOADING, SUCCESS, ERROR }

    public final Status status;
    public final T data;
    public final String message;

    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String message) {
        return new Resource<>(Status.ERROR, null, message);
    }
}
