package com.lunchandmeet.utils;

import com.lunchandmeet.daos.impl.UserDaoMongo;
import com.lunchandmeet.dto.User;
import com.lunchandmeet.utils.odnoklassniki.OdnoklassnikiApi;
import org.json.JSONException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.GoogleApi;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by Admin on 03.06.14.
 */
public class AuthHelper {

    private static final String HOST = "http://lunch2meet.loc:8080";

    public static void facebook(HttpServletResponse resp) throws IOException {
        String callback = HOST + "/api?action=callback&provider=facebook";
        OAuthService service = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(ApiKeys.FACEBOOK_API_KEY)
                .apiSecret(ApiKeys.FACEBOOK_API_SECRET)
                .callback(callback)
                .build();
        resp.sendRedirect(service.getAuthorizationUrl(null));
    }

    public static User facebookCallback(HttpServletRequest req) throws UnknownHostException {
        String callback = HOST + "/api?action=callback&provider=facebook";
        OAuthService service = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(ApiKeys.FACEBOOK_API_KEY)
                .apiSecret(ApiKeys.FACEBOOK_API_SECRET)
                .callback(callback)
                .build();
        String verifier = req.getParameter(OAuthConstants.CODE);
        Token accessToken = service.getAccessToken(null, new Verifier(verifier));
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
        service.signRequest(accessToken, request);
        Response response = request.send();
        JSONUtils jsonUtils = new JSONUtils();
        UserDaoMongo userDao = new UserDaoMongo();
        User user = new User();
        try {
            user = jsonUtils.deserializeUserFacebook(response.getBody());
            user = userDao.createUser(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    private static String GOOGLE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";

    public static void google(HttpServletResponse resp) throws IOException {
        String callback = HOST + "/api?action=callback&provider=google";
        OAuthService service = new ServiceBuilder()
                .provider(GoogleApi.class)
                .apiKey(ApiKeys.GOOGLE_API_KEY)
                .apiSecret(ApiKeys.GOOGLE_API_SECRET)
                .callback(callback)
                .scope(GOOGLE_SCOPE)
                .build();
        Token requestToken = service.getRequestToken();
        saveToken = requestToken;
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.sendRedirect(service.getAuthorizationUrl(requestToken));
    }

    private static Token saveToken;

    public static User googleCallback(HttpServletRequest req) throws UnknownHostException {
        OAuthService service = new ServiceBuilder()
                .provider(GoogleApi.class)
                .apiKey(ApiKeys.GOOGLE_API_KEY)
                .apiSecret(ApiKeys.GOOGLE_API_SECRET)
                .build();

        Token requestToken = saveToken;
        String verifier = req.getParameter(OAuthConstants.VERIFIER);
        Token accessToken = service.getAccessToken(requestToken, new Verifier(verifier));

        OAuthRequest request = new OAuthRequest(Verb.GET,
                "https://www.googleapis.com/oauth2/v1/userinfo");
        service.signRequest(accessToken, request);
        request.addHeader("GData-Version", "3.0");
        Response response = request.send();
        JSONUtils jsonUtils = new JSONUtils();
        UserDaoMongo userDao = new UserDaoMongo();
        User user = new User();
        try {
            user = jsonUtils.deserializeUserGoogle(response.getBody());
            user = userDao.createUser(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void vkontakte(HttpServletResponse resp) throws IOException {
        String callback = HOST + "/api?action=callback&provider=vkontakte";
        OAuthService service = new ServiceBuilder()
                .provider(VkontakteApi.class)
                .apiKey(ApiKeys.VK_API_KEY)
                .apiSecret(ApiKeys.VK_API_SECRET)
                .callback(callback)
                .scope("email")
                .build();
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.sendRedirect(service.getAuthorizationUrl(null));
    }


    public static User vkontakteCallback(HttpServletRequest req) throws UnknownHostException {
        String callback = HOST + "/api?action=callback&provider=vkontakte";
        OAuthService service = new ServiceBuilder()
                .provider(VkontakteApi.class)
                .apiKey(ApiKeys.VK_API_KEY)
                .apiSecret(ApiKeys.VK_API_SECRET)
                .callback(callback)
                .build();

        String verifier = req.getParameter(OAuthConstants.CODE);
        Token accessToken = service.getAccessToken(null, new Verifier(verifier));
        OAuthRequest request = new OAuthRequest(Verb.GET,
                "https://api.vkontakte.ru/method/users.get?fields=photo_200,email");
        service.signRequest(accessToken, request);
        Response response = request.send();
        JSONUtils jsonUtils = new JSONUtils();
        UserDaoMongo userDao = new UserDaoMongo();
        User user = new User();
        try {
            user = jsonUtils.deserializeUserVkontakte(response.getBody());
            user = userDao.createUser(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void odnoklassniki(HttpServletResponse resp) throws IOException {
        String callback = HOST + "/api?action=callback&provider=odnoklassniki";
        OAuthService service = new ServiceBuilder()
                .provider(OdnoklassnikiApi.class)
                .apiKey(ApiKeys.ODNOKLASSNIKI_API_KEY)
                .apiSecret(ApiKeys.ODNOKLASSNIKI_API_SECRET)
                .scope("VALUABLE_ACCESS")
                .callback(callback)
                .build();
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.sendRedirect(service.getAuthorizationUrl(null));
    }


    public static User odnoklassnikiCallback(HttpServletRequest req) throws UnknownHostException {
        String callback = HOST + "/api?action=callback&provider=odnoklassniki";
        OAuthService service = new ServiceBuilder()
                .provider(OdnoklassnikiApi.class)
                .apiKey(ApiKeys.ODNOKLASSNIKI_API_KEY)
                .apiSecret(ApiKeys.ODNOKLASSNIKI_API_SECRET)
                .callback(callback)
                .build();

        String verifier = req.getParameter(OAuthConstants.CODE);
        Token accessToken = service.getAccessToken(null, new Verifier(verifier));
        OAuthRequest request = new OAuthRequest(Verb.POST,
                "http://api.odnoklassniki.ru/fb.do");
        service.signRequest(accessToken, request);
        Response response = request.send();
        JSONUtils jsonUtils = new JSONUtils();
        UserDaoMongo userDao = new UserDaoMongo();
        User user = new User();
        try {
            user = jsonUtils.deserializeUserOdnoklassniki(response.getBody());
            user = userDao.createUser(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }
}