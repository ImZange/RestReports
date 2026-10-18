package com.example.restreports.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton: one Retrofit/OkHttp instance is reused during the whole app lifecycle.
 */
public final class RetrofitClient {

    // -------------------------------------------------------------------------------
    // CONFIGURE HERE (see README.md):
    //  A) mockapi.io (recommended): create a project and a resource named "reports";
    //     copy the project URL (the resource name is added by ApiService):
    //     "https://<your-project-id>.mockapi.io/api/v1/"
    //  B) Local json-server (emulator only): "http://10.0.2.2:3000/"
    //     (plain http also needs android:usesCleartextTraffic="true" in the manifest)
    // The URL MUST end with "/".
    // -------------------------------------------------------------------------------
    private static final String BASE_URL =
            "https://6ad436fb59dd78397bbc5207.mockapi.io/api/ok/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    private RetrofitClient() {
    }

    /** False while BASE_URL still contains the YOUR_* placeholders. */
    public static boolean isConfigured() {
        return !BASE_URL.contains("YOUR_");
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            // Short timeouts so that network problems are reported quickly to the user
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
