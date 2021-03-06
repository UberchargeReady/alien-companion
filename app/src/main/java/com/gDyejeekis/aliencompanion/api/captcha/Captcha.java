package com.gDyejeekis.aliencompanion.api.captcha;

import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by George on 8/11/2015.
 */
public class Captcha {

    private final HttpClient httpClient;

    /**
     * Constructor.
     * @param httpClient HttpClient
     */
    public Captcha(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Generates and saves a new reddit captcha in the working directory
     *
     * @param user user to get captcha for
     * @return the iden of the generated captcha as a String
     */
    public String newCaptcha(User user) {
        JSONObject obj = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, null, ApiEndpointUtils.CAPTCHA_NEW, user.getCookie()).getResponseObject();
        String iden = (String) ((JSONArray) ((JSONArray) ((JSONArray) obj.get("jquery")).get(11)).get(3)).get(0);
        return iden;
    }

    /**
     * Check whether user needs CAPTCHAs for API methods that define the "captcha" and "iden" parameters.
     *
     * @param user user to do the check for
     * @return true if CAPTCHAs are needed, false otherwise
     */
    public boolean needsCaptcha(User user) {
        return httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, ApiEndpointUtils.CAPTCHA_NEEDS, user.getCookie()).getResponseText().equals("true");
    }

}
