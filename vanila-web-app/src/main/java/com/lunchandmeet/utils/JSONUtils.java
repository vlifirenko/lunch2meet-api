package com.lunchandmeet.utils;

import com.lunchandmeet.dto.Message;
import com.lunchandmeet.dto.Place;
import com.lunchandmeet.dto.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JSONUtils {

    private static final Logger log = Logger.getLogger(JSONUtils.class.getName());

    public JSONArray serializeMessages(List<Message> messages, Map<String, User> users) {
        JSONArray jsonMessages = new JSONArray();
        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext(); ) {
            Message message = (Message) iterator.next();
            JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("user", serializeUser(users.get(message.senderId)));
                jsonMessage.put("message", serializeMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonMessages.put(jsonMessage);
        }
        return jsonMessages;
    }

    public JSONArray serializeUsers(List<User> users) {
        JSONArray jsonUsers = new JSONArray();
        for (Iterator<User> iterator = users.iterator(); iterator.hasNext(); ) {
            User user = (User) iterator.next();
            JSONObject jsonMessage = new JSONObject();
            try {
                jsonMessage.put("user", serializeUser(user));
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonUsers.put(jsonMessage);
        }
        return jsonUsers;
    }

    public JSONArray serializePlaces(List<Place> places) {
        JSONArray jsonPlaces = new JSONArray();
        for (Iterator<Place> iterator = places.iterator(); iterator.hasNext(); ) {
            Place place = (Place) iterator.next();
            JSONObject jsonUser = new JSONObject();
            try {
                jsonUser.put("place", serializePlace(place));
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonPlaces.put(jsonUser);
        }
        return jsonPlaces;
    }

    private JSONObject serializeMessage(Message message) throws JSONException {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("text", message.text);
        jsonMessage.put("timestamp", message.timestamp);
        jsonMessage.put("distance", message.distance);
        return jsonMessage;
    }

    public JSONObject serializeUser(User user) throws JSONException {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("id", user.id);
        jsonUser.put("name", user.name);
        jsonUser.put("status", user.status);
        jsonUser.put("pictureURL", user.pictureURL);
        jsonUser.put("profileURL", user.profileURL);
        jsonUser.put("token", user.token);
        return jsonUser;
    }

    public JSONObject serializePlace(Place place) throws JSONException {
        JSONObject jsonPlace = new JSONObject();
        jsonPlace.put("id", place.id);
        jsonPlace.put("name", place.name);
        jsonPlace.put("businessLunch", place.businessLunch);
        jsonPlace.put("businessLunchTime", place.businessLunchTime);
        jsonPlace.put("averageCheck", place.averageCheck);
        jsonPlace.put("favorite", place.favorite);
        jsonPlace.put("distance", place.distance);
        jsonPlace.put("specialHeader", place.specialHeader);
        jsonPlace.put("specialText", place.specialText);
        jsonPlace.put("avatar", place.avatar);
        jsonPlace.put("address", place.address);
        jsonPlace.put("link", place.link);
        jsonPlace.put("location", place.location);
        return jsonPlace;
    }

    public User deserializeUserFacebook(String jsonString) throws JSONException {
        User user = new User();
        JSONObject jsonObject = new JSONObject(jsonString);
        user.name = jsonObject.getString("name");
        user.profileURL = jsonObject.getString("link");
        user.pictureURL = String.format("https://graph.facebook.com/%s/picture?type=large",
                jsonObject.getString("id"));
        return user;
    }

    public User deserializeUserGoogle(String jsonString) throws JSONException {
        User user = new User();
        JSONObject jsonObject = new JSONObject(jsonString);
        user.email = jsonObject.getString("email");
        user.name = jsonObject.getString("name");
        user.profileURL = jsonObject.getString("link");
        user.pictureURL = jsonObject.getString("picture");
        return user;
    }

    public User deserializeUserVkontakte(String jsonString) throws JSONException {
        User user = new User();
        JSONObject jsonObject = (JSONObject) new JSONObject(jsonString).getJSONArray("response").get(0);
        user.name = String.format("%s %s", jsonObject.getString("first_name"), jsonObject.getString("last_name"));
        user.profileURL = String.format("https://vk.com/%s", jsonObject.getString("uid"));
        user.pictureURL = jsonObject.getString("photo_200");
        return user;
    }

    public User deserializeUserOdnoklassniki(String jsonString) throws JSONException {
        User user = new User();
        JSONObject jsonObject = new JSONObject(jsonString);
        user.name = jsonObject.getString("name");
        user.profileURL = String.format("http://www.odnoklassniki.ru/profile/%s", jsonObject.getString("uid"));
        user.pictureURL = jsonObject.getString("pic_1");
        return user;
    }
}
