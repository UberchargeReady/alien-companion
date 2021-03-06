package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

/**
 * Created by George on 1/16/2017.
 */

public class GiphyTask extends AsyncTask<String, Void, String> {

    private Context context;

    public GiphyTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            final String url = params[0];
            return getGiphyDirectUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }

    public static String getGiphyDirectUrl(String originalUrl) throws IOException, ParseException {
        final String url = "http://api.giphy.com/v1/gifs/" + LinkUtils.getGiphyId(originalUrl) + "?api_key=" + LinkHandler.GIPHY_API_KEY;
        Log.d("Giphy", "GET request to " + url);
        Request request = new Request.Builder()
                .cacheControl(new CacheControl.Builder().maxStale(24, TimeUnit.HOURS).build())
                .url(url).build();
        Response response = MyApplication.okHttpClient.newCall(request).execute();
        String content = response.body().string();
        response.close();

        Object responseObject = new JSONParser().parse(content);
        JSONObject giphyData = (JSONObject) ((JSONObject) responseObject).get("data");
        JSONObject images = (JSONObject) giphyData.get("images");
        JSONObject original = (JSONObject) images.get("original");
        return safeJsonToString(original.get("mp4"));
    }

    // simple modify url method for GIPHY
    public static String getGiphyDirectUrlSimple(String originalUrl) {
        return "http://media.giphy.com/media/" + LinkUtils.getGiphyId(originalUrl) + "/giphy.mp4";
    }
}
