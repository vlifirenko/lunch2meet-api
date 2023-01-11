package com.lunchandmeet;

import com.lunchandmeet.daos.MessageDao;
import com.lunchandmeet.daos.PlaceDao;
import com.lunchandmeet.daos.UserDao;
import com.lunchandmeet.daos.impl.MessageDaoMongo;
import com.lunchandmeet.daos.impl.PlaceDaoMongo;
import com.lunchandmeet.daos.impl.UserDaoMongo;
import com.lunchandmeet.dto.Message;
import com.lunchandmeet.dto.Place;
import com.lunchandmeet.dto.User;
import com.lunchandmeet.utils.AuthHelper;
import com.lunchandmeet.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class Lunch2meetServlet extends HttpServlet {

    enum Provider {GOOGLE, FACEBOOK, VKONTAKTE, ODNOKLASSNIKI}

    private UserDao userDao;
    private MessageDao messageDao;
    private PlaceDao placeDao;
    private JSONUtils jsonUtils;

    public Lunch2meetServlet() throws UnknownHostException {
        userDao = new UserDaoMongo();
        messageDao = new MessageDaoMongo();
        placeDao = new PlaceDaoMongo();
        jsonUtils = new JSONUtils();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        String token = req.getParameter("token");
        User user = userDao.getByToken(token);

        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        String jsonString = "{}";
        switch (action) {
            case "set_status":
                userDao.setStatus(user.id, req.getParameter("status"));
                break;

            case "set_token":
                userDao.setToken(user.id, req.getParameter("token"));
                break;

            case "get_messages":
                jsonString = getMessagesJson(req.getParameter("since"),
                        req.getParameter("latitude"),
                        req.getParameter("longitude"));
                break;

            case "get_profile":
                String profileId = req.getParameter("id");
                jsonString = getProfileJson(profileId);
                break;

            case "get_my_profile":
                jsonString = getProfileJson(user.id);
                break;

            case "send_message":
                if (user.id.equals(req.getParameter("reciever")))
                    break;

                saveMessage(user.id,
                        req.getParameter("reciever"),
                        req.getParameter("text"),
                        req.getParameter("latitude"),
                        req.getParameter("longitude"));
                break;

            case "get_dialog":
                jsonString = getDialogJson(user.id,
                        req.getParameter("reciever"),
                        req.getParameter("since"));
                break;

            case "get_dialogs_count":
                jsonString = getDialogsCountJson(user.id);
                break;

            case "get_dialogs":
                jsonString = getDialogsJson(user.id);
                break;

            case "get_users":
                jsonString = getUsersJson(user.id);
                break;

            case "get_places":
                jsonString = getPlacesJson();
                break;

            case "get_place":
                jsonString = getPlaceJson(req.getParameter("place"));
                break;

            case "auth":
                if (req.getParameter("provider").equals("google"))
                    auth(resp, Provider.GOOGLE);
                else if (req.getParameter("provider").equals("facebook"))
                    auth(resp, Provider.FACEBOOK);
                else if (req.getParameter("provider").equals("vkontakte"))
                    auth(resp, Provider.VKONTAKTE);
                else if (req.getParameter("provider").equals("odnoklassniki"))
                    auth(resp, Provider.ODNOKLASSNIKI);
                break;

            case "callback":
                if (req.getParameter("provider").equals("google"))
                    jsonString = callback(req, resp, Provider.GOOGLE);
                else if (req.getParameter("provider").equals("facebook"))
                    jsonString = callback(req, resp, Provider.FACEBOOK);
                else if (req.getParameter("provider").equals("vkontakte"))
                    jsonString = callback(req, resp, Provider.VKONTAKTE);
                else if (req.getParameter("provider").equals("odnoklassniki"))
                    jsonString = callback(req, resp, Provider.ODNOKLASSNIKI);
                break;

            case "login":
                jsonString = login(req.getParameter("token"));
                break;
        }

        resp.getWriter().println(jsonString);
    }


    private String getDialogsJson(String id) {
        String json = "[]";
        try {
            User user = userDao.getById(id);
            List<Message> messages = messageDao.getDialogs(user);
            json = jsonUtils.serializeMessages(messages, getUsers4Messages(messages)).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private Map<String, User> getUsers4Messages(List<Message> messages) {
        Map<String, User> users = new HashMap<String, User>();

        if (messages == null) {
            return users;
        }

        String id;
        Message message;
        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext(); ) {
            message = (Message) iterator.next();
            id = message.senderId;
            if (!users.containsKey(id)) {
                users.put(id, userDao.getById(id));
            }
        }
        return users;
    }

    private String getDialogsCountJson(String id) {
        JSONObject obj = new JSONObject();
        try {
            User user = userDao.getById(id);
            obj.put("count", messageDao.getDialogsCount(user));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    private String getProfileJson(String id) {
        String json = "{}";
        try {
            User user = userDao.getById(id);
            json = jsonUtils.serializeUser(user).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private String getPlaceJson(String placeId) {
        String json = "{}";
        try {
            Place place = placeDao.getById(placeId);
            json = jsonUtils.serializePlace(place).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private void saveMessage(String senderId, String recieverId, String text, String latitude, String longitude) {
        User reciever = userDao.getById(recieverId);
        User sender = userDao.getById(senderId);
        messageDao.saveMessage(sender, reciever, text, Float.parseFloat(latitude), Float.parseFloat(longitude));
        userDao.setLocation(senderId, new double[]{Float.parseFloat(latitude), Float.parseFloat(longitude)});
    }

    private String getMessagesJson(String sinceTimestampStr, String latitudeStr, String longitudeStr) {
        String json = "[]";
        try {
            List<Message> messages = messageDao.getMessages(Long.parseLong(sinceTimestampStr), Float.parseFloat(latitudeStr), Float.parseFloat(longitudeStr));
            json = jsonUtils.serializeMessages(messages, getUsers4Messages(messages)).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private String getUsersJson(String id) {
        String json = "[]";
        try {
            List<User> users = userDao.getNearUsers(id);
            json = jsonUtils.serializeUsers(users).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private String getPlacesJson() {
        String json = "[]";
        try {
            List<Place> places = placeDao.getAllPlaces();
            json = jsonUtils.serializePlaces(places).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private String getDialogJson(String id, String recieverId, String sinceTimestamp) {
        String json = "[]";
        try {
            User sender = userDao.getById(id);
            User reciever = userDao.getById(recieverId);
            List<Message> messages = messageDao.getDialog(sender, reciever, Long.parseLong(sinceTimestamp));
            json = jsonUtils.serializeMessages(messages, getUsers4Messages(messages)).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private void auth(HttpServletResponse resp, Provider provider) {
        try {
            switch (provider) {
                case GOOGLE:
                    AuthHelper.google(resp);
                    break;
                case FACEBOOK:
                    AuthHelper.facebook(resp);
                    break;
                case VKONTAKTE:
                    AuthHelper.vkontakte(resp);
                    break;
                case ODNOKLASSNIKI:
                    AuthHelper.odnoklassniki(resp);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String REDIRECT_LOGIN_URL = "http://lunch2meet.loc/#/auth-callback/";

    private String callback(HttpServletRequest req, HttpServletResponse resp, Provider provider) {
        String json = "[]";
        try {
            User user = null;
            switch (provider) {
                case GOOGLE:
                    user = AuthHelper.googleCallback(req);
                    break;
                case FACEBOOK:
                    user = AuthHelper.facebookCallback(req);
                    break;
                case VKONTAKTE:
                    user = AuthHelper.vkontakteCallback(req);
                    break;
                case ODNOKLASSNIKI:
                    user = AuthHelper.odnoklassnikiCallback(req);
                    break;
            }
            if (user != null)
                resp.sendRedirect(REDIRECT_LOGIN_URL + user.token);
            else
                return json;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private String login(String token) {
        User user = userDao.getByToken(token);
        if (user != null)
            try {
                return jsonUtils.serializeUser(user).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return null;
    }

}
