package com.lunch2meet.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.lunch2meet.dto.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * A helper class for Google's OAuth2 authentication API.
 * @version 20130224
 * @author Matyas Danter
 */
public final class GoogleAuthHelper {

  /**
   * Please provide a value for the CLIENT_ID constant before proceeding, set this up at https://code.google.com/apis/console/
   */
  private static final String CLIENT_ID = "78510187078-tv180rae0vss9mgm26cl00puhn3lrhsb.apps.googleusercontent.com";
  /**
   * Please provide a value for the CLIENT_SECRET constant before proceeding, set this up at https://code.google.com/apis/console/
   */
  private static final String CLIENT_SECRET = "WosgqhvLVf8LIaV0MsqIS5Qv";

  /**
   * Callback URI that google will redirect to after successful authentication
   */
  private static final String CALLBACK_URI = "http://localhost:8888/index.jsp";

  // start google authentication constants
  private static final Iterable<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile;https://www.googleapis.com/auth/userinfo.email;https://www.googleapis.com/auth/plus.me".split(";"));
  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  // end google authentication constants


  private final GoogleAuthorizationCodeFlow flow;

  /**
   * Constructor initializes the Google Authorization Code Flow with CLIENT ID, SECRET, and SCOPE
   */
  public GoogleAuthHelper() {
    flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
        JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPE).build();
  }

  /**
   * Builds a login URL based on client ID, secret, callback URI, and scope
   */
  public String buildLoginUrl() {

    final GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();

    return url.setRedirectUri(CALLBACK_URI).setState("google").build();
  }

  /**
   * Expects an Authentication Code, and makes an authenticated request for the user's profile information
   * @return JSON formatted user profile information
   * @param authCode authentication code provided by google
   * @throws JSONException
   */
  public JSONObject getUserInfoJson(final String authCode) throws IOException, JSONException {

    final GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(CALLBACK_URI).execute();
    final Credential credential = flow.createAndStoreCredential(response, null);
    final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
    // Make an authenticated request
    final GenericUrl url = new GenericUrl(USER_INFO_URL);
    final HttpRequest request = requestFactory.buildGetRequest(url);
    request.getHeaders().setContentType("application/json");

    final String jsonIdentity = request.execute().parseAsString();

    return new JSONObject(jsonIdentity);

  }

  public User deserializeUser(JSONObject userJson) throws JSONException, MalformedURLException {
    System.out.println(userJson.toString());
    User user = new User();
    if (userJson.has("email")) user.email      = (String) userJson.get("email");
    if (userJson.has("name"))  user.name       = (String) userJson.get("name");
    if (userJson.has("link"))  user.profileURL = new URL((String) userJson.get("link"));
    String pictureUrl = (userJson.has("picture") && userJson.get("picture") != null) ? ((String) userJson.get("picture")) + "?sz=100" : "http://localhost:8888/img/no-picture.jpg";
    user.pictureURL = new URL(pictureUrl);
    return user;
  }

}
