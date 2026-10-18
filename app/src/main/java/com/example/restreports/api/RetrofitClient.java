package com.example.restreports.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public final class RetrofitClient {


    private static final String BASE_URL =
            "https://6ad436fb59dd78397bbc5207.mockapi.io/api/ok/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    private RetrofitClient() {
    }


    public static boolean isConfigured() {
        return !BASE_URL.contains("YOUR_");
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
