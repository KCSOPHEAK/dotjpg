/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.alashow.dotjpg.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import tm.alashow.dotjpg.App;
import tm.alashow.dotjpg.Config;

public class ApiClient {
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static int DEFAULT_TIMEOUT = 40 * 1000; //40 seconds

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        get(url, params, responseHandler, DEFAULT_TIMEOUT);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, int timeoutInMilliSeconds) {
        client.setTimeout(timeoutInMilliSeconds);
        client.get(App.applicationContext, getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        post(url, params, responseHandler, DEFAULT_TIMEOUT);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, int timeoutInMilliSeconds) {
        client.setTimeout(timeoutInMilliSeconds);
        client.post(App.applicationContext, getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return Config.SERVER + relativeUrl;
    }

    /**
     * Cancel all requests
     */
    public static void cancelAll() {
        try {
            client.cancelRequests(App.applicationContext, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}