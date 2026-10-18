package com.example.restreports.api;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.restreports.R;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public final class ApiErrors {

    private ApiErrors() {
    }


    @NonNull
    public static String fromHttpCode(@NonNull Context c, int code) {
        if (code == 400) return c.getString(R.string.error_bad_request);
        if (code == 401 || code == 403) return c.getString(R.string.error_unauthorized);
        if (code == 404) return c.getString(R.string.error_not_found);
        if (code == 408) return c.getString(R.string.error_timeout);
        if (code == 429) return c.getString(R.string.error_too_many);
        if (code >= 500) return c.getString(R.string.error_server);
        return c.getString(R.string.error_http_generic);
    }

    @NonNull
    public static String fromThrowable(@NonNull Context c, @NonNull Throwable t) {
        if (t instanceof UnknownHostException || t instanceof ConnectException) {
            return c.getString(R.string.error_no_connection);
        }
        if (t instanceof SocketTimeoutException) {
            return c.getString(R.string.error_timeout);
        }
        if (t instanceof IOException) {
            return c.getString(R.string.error_network);
        }
        return c.getString(R.string.error_unexpected);
    }
}
