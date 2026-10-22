package com.aicoach.data.remote;

import android.content.Context;

import com.aicoach.BuildConfig;
import com.aicoach.data.local.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Nơi DUY NHẤT tạo Retrofit (singleton). Repository lấy service bằng:
 * <pre>AuthApi api = ApiClient.create(context, AuthApi.class);</pre>
 * Activity/Fragment/ViewModel KHÔNG được gọi lớp này.
 */
public final class ApiClient {

    private static volatile Retrofit retrofit;

    private ApiClient() {
    }

    public static <S> S create(Context context, Class<S> service) {
        return get(context).create(service);
    }

    private static Retrofit get(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);
                    logging.redactHeader("Authorization");

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor(
                                    TokenManager.getInstance(context.getApplicationContext())))
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}
