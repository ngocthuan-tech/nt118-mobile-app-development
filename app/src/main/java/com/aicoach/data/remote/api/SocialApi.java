package com.aicoach.data.remote.api;

import com.aicoach.data.model.Challenge;
import com.aicoach.data.model.Social;
import com.aicoach.data.remote.ApiResponse;
import com.aicoach.data.remote.dto.SocialDtos.CreateChallengeRequest;
import com.aicoach.data.remote.dto.SocialDtos.InviteRequest;
import com.aicoach.data.remote.dto.SocialDtos.JoinRequest;
import com.aicoach.data.remote.dto.SocialDtos.RespondRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Cộng đồng (Thành viên B): Thử thách 40–42, Bảng xếp hạng, Thông báo 22. Cần đăng nhập. */
public interface SocialApi {

    // ---------------------------------------------------------------- Thử thách

    /** Thử thách mình tham gia: đang diễn ra trước, tối đa 20. */
    @GET("challenges")
    Call<ApiResponse<List<Challenge>>> getChallenges();

    @GET("challenges/{id}")
    Call<ApiResponse<Challenge>> getChallenge(@Path("id") String id);

    @POST("challenges")
    Call<ApiResponse<Challenge>> createChallenge(@Body CreateChallengeRequest body);

    @POST("challenges/join")
    Call<ApiResponse<Challenge>> join(@Body JoinRequest body);

    /** Bạn bè lọc theo tên (không dấu), hoặc q là email đúng của một người dùng. */
    @GET("challenges/{id}/candidates")
    Call<ApiResponse<List<Social.Candidate>>> getCandidates(@Path("id") String id, @Query("q") String q);

    @POST("challenges/{id}/invites")
    Call<ApiResponse<Social.InviteResult>> invite(@Path("id") String id, @Body InviteRequest body);

    // ---------------------------------------------------------------- Bạn bè, xếp hạng

    @GET("social/friends")
    Call<ApiResponse<List<Social.Friend>>> getFriends();

    /** scope = friends | national */
    @GET("social/leaderboard")
    Call<ApiResponse<Social.Leaderboard>> getLeaderboard(@Query("scope") String scope);

    // ---------------------------------------------------------------- Thông báo

    /** type = all | challenge | system */
    @GET("notifications")
    Call<ApiResponse<Social.NoticePage>> getNotifications(@Query("type") String type);

    @POST("notifications/read-all")
    Call<ApiResponse<Social.ReadAllResult>> readAll();

    @POST("notifications/{id}/read")
    Call<ApiResponse<Social.Notice>> markRead(@Path("id") String id);

    @POST("notifications/{id}/respond")
    Call<ApiResponse<Social.Notice>> respond(@Path("id") String id, @Body RespondRequest body);
}
