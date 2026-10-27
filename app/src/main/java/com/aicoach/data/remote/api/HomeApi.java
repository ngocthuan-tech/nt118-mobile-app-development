package com.aicoach.data.remote.api;

import com.aicoach.data.model.HomeData;
import com.aicoach.data.model.HomeStreak;
import com.aicoach.data.model.TodayWorkout;
import com.aicoach.data.remote.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/** Trang chủ (Figma 21). Giữa kỳ dùng 2 API riêng: Chuỗi 7 ngày và Bài tập hôm nay. */
public interface HomeApi {

    /** Thẻ "Chuỗi 7 ngày": số ngày liên tiếp + 7 chấm T2–CN. */
    @GET("home/streak")
    Call<ApiResponse<HomeStreak>> getStreak();

    /** Dải "Bài tập hôm nay": buổi tiếp theo của giáo án, hoặc 3 bài gợi ý. */
    @GET("home/today-workout")
    Call<ApiResponse<TodayWorkout>> getTodayWorkout();

    /** Gộp toàn bộ Trang chủ trong một lần gọi (không bắt buộc dùng). */
    @GET("home")
    Call<ApiResponse<HomeData>> getHome();
}
