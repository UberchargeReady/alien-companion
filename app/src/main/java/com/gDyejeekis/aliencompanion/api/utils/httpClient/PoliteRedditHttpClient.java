package com.gDyejeekis.aliencompanion.api.utils.httpClient;

import android.util.Log;

import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;

import okhttp3.RequestBody;

/**
 * Created by sound on 11/10/2015.
 */
public class PoliteRedditHttpClient extends RedditHttpClient {

    private static final long WAIT_TIME_DEFAULT = 2000L;

    private static final long WAIT_TIME_OAUTH = 1000L;

    /**
     * Waiting time in milliseconds.
     */
    private static final long WAIT_TIME = (RedditOAuth.useOAuth2) ? WAIT_TIME_OAUTH : WAIT_TIME_DEFAULT; //2000L for 30 requests/min, 1000L for 60 requests/min

    /**
     * Last time a request was made.
     */
    private static long lastReqTime = 0;

    public PoliteRedditHttpClient() {
        super();
    }

    public PoliteRedditHttpClient(User user) {
        super(user);
    }

    //public PoliteHttpRestClient(HttpClient httpClient, ResponseHandler<Response> responseHandler) {
    //    super(httpClient, responseHandler);
    //}

    public Response get(String baseUrl, String urlPath, String cookie) {
        waitIfNeeded();
        Response resp = super.get(baseUrl, urlPath, cookie);
        noteTime();
        return resp;
    }

    public Response post(String baseUrl, RequestBody apiParams, String urlPath, String cookie) {
        waitIfNeeded();
        Response resp = super.post(baseUrl, apiParams, urlPath, cookie);
        noteTime();
        return resp;
    }

    public Response put(String baseUrl, RequestBody apiParams, String urlPath, String cookie) {
        waitIfNeeded();
        Response resp = super.put(baseUrl, apiParams, urlPath, cookie);
        noteTime();
        return resp;
    }

    public Response delete(String baseUrl, RequestBody apiParams, String urlPath, String cookie) {
        waitIfNeeded();
        Response resp = super.delete(baseUrl, apiParams, urlPath, cookie);
        noteTime();
        return resp;
    }

    private void noteTime() {
        lastReqTime = System.currentTimeMillis();
        //Log.d("geotest", String.valueOf(lastReqTime));
    }

    private void waitIfNeeded() {
        if (lastReqTime == 0)
            return;

        long elapsed = System.currentTimeMillis() - lastReqTime;

        if (elapsed >= WAIT_TIME)
            return;

        long toWait = WAIT_TIME - elapsed;
        try {
            //Log.d("Polite http requests", "waiting for " + toWait);
            Thread.sleep(toWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
