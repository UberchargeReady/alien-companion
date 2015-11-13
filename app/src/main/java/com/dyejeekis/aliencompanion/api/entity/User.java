package com.dyejeekis.aliencompanion.api.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.dyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.retrieval.Subreddits;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditOAuth;

/**
 * This class represents a user connected to Reddit.
 *
 * Implement: gildSelf, giveGold, 
 *
 * @author Omer Elnour
 * @author Karan Goel
 * @author Raul Rene Lepsa
 * @author Benjamin Jakobus
 * @author Evin Ugur
 * @author Andrei Sfat
 * @author Simon Kassing
 * @author Marc Leef
 */
public class User implements Serializable {

    private String username;
    private final HttpClient httpClient;
    private String modhash, cookie, password;
    private OAuthToken tokenObject;

    /**
     * Create a user.
     * @param httpClient HTTP Client handle
     * @param username User name
     * @param password Password
     */
    public User(HttpClient httpClient, String username, String password) {
        this.httpClient = httpClient;
        this.username = username;
        this.password = password;
    }

    public User(HttpClient httpClient, String username, String modhash, String cookie) {
        this.httpClient = httpClient;
        this.username = username;
        this.modhash = modhash;
        this.cookie = cookie;
    }

    public User(HttpClient httpClient, OAuthToken tokenObject) {
        this.httpClient = httpClient;
        this.tokenObject = tokenObject;
    }

    public OAuthToken getTokenObject() {
        return tokenObject;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the user name of the user.
     * @return User name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the password of the user.
     * @param password Password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieve the modulo hash of the cookie.
     * @return Modulo hash
     */
    public String getModhash() {
        return modhash;
    }

    /**
     * Retrieve the cookie of the user containing all session information.
     * @return Cookie
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * Call this function to connect the user. <br /> By "connect" I mean
     * effectively sending a POST request to reddit and getting the modhash and
     * cookie, which are required for most reddit API functions.
     *
     * @throws IOException If connection fails.
     * @throws ParseException If parsing JSON fails.
     */
    public void connect() throws IOException, ParseException {
        if(RedditOAuth.useOAuth2) {

        }
        else {
            ArrayList<String> hashCookiePair = hashCookiePair(ConvertUtils.URLEncodeString(username), ConvertUtils.URLEncodeString(password));
            this.modhash = hashCookiePair.get(0);
            this.cookie = hashCookiePair.get(1);
        }
    }

    /**
     * This function logs in to reddit and returns an ArrayList containing a
     * modhash and cookie.
     *
     * @param username The username
     * @param password The password
     * @return An array containing a modhash and cookie
     * @throws IOException    If connection fails
     * @throws ParseException If parsing JSON fails
     */
    private ArrayList<String> hashCookiePair(String username, String password) throws IOException, ParseException {
        ArrayList<String> values = new ArrayList<String>();
        JSONObject jsonObject = (JSONObject) httpClient.post("api_type=json&user=" + username + "&passwd=" + password, String.format(ApiEndpointUtils.USER_LOGIN, username), getCookie()).getResponseObject();
        JSONObject valuePair = (JSONObject) ((JSONObject) jsonObject.get("json")).get("data");

        values.add(valuePair.get("modhash").toString());
        values.add(valuePair.get("cookie").toString());

        return values;
    }


    /**
     * This function returns all the subreddits the user is subscribed to.
     * @param limit leave 0 for max number
     * @return A list of subreddit objects
     * @throws RetrievalFailedException    If retrieval of subreddits fails
     * @throws RedditError
     */
    public List<Subreddit> getSubscribed(int limit) throws RetrievalFailedException, RedditError {
        if (this.getCookie() == null || this.getModhash() == null) {
            System.err.printf("Please invoke the connect method in order to login the user");
            return null;
        }
        Subreddits sub = new Subreddits(httpClient, this);

        return sub.parse(ApiEndpointUtils.USER_GET_SUBSCRIBED + (limit == 0 ? "?&limit=100" : "?&limit=" + limit));
    }

    /**
     * This function returns all the subreddits the user is an approved contributor to.
     * @param limit leave 0 for max number
     * @return A list of subreddit objects
     * @throws RetrievalFailedException    If retrieval of subreddits fails
     * @throws RedditError
     */
    public List<Subreddit> getContributedTo(int limit) throws RetrievalFailedException, RedditError {
        if (this.getCookie() == null || this.getModhash() == null) {
            System.err.printf("Please invoke the connect method in order to login the user");
            return null;
        }
        Subreddits sub = new Subreddits(httpClient, this);

        return sub.parse(ApiEndpointUtils.USER_GET_CONTRIBUTED_TO + (limit == 0 ? "?&limit=100" : "?&limit=" + limit));
    }

    /**
     * This function returns all the subreddits the user is a moderator of.
     * @param limit leave 0 for max number
     * @return A list of subreddit objects
     * @throws RetrievalFailedException    If retrieval of subreddits fails
     * @throws RedditError
     */
    public List<Subreddit> getModeratorOf(int limit) throws RetrievalFailedException, RedditError {
        if (this.getCookie() == null || this.getModhash() == null) {
            System.err.printf("Please invoke the connect method in order to login the user");
            return null;
        }
        Subreddits sub = new Subreddits(httpClient, this);

        return sub.parse(ApiEndpointUtils.USER_GET_MODERATOR_OF + (limit == 0 ? "?&limit=100" : "?&limit=" + limit));
    }

}