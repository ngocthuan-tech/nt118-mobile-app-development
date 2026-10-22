package com.aicoach.data.remote;

/** Envelope chung của MỌI API backend: { "success": true, "message": "...", "data": ... }. */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
