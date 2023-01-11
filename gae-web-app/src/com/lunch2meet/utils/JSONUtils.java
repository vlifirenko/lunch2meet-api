package com.lunch2meet.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.lunch2meet.daos.MessageDao;
import com.lunch2meet.daos.UserDao;
import com.lunch2meet.dto.Message;
import com.lunch2meet.dto.User;

public class JSONUtils {

  private static final Logger log = Logger.getLogger(JSONUtils.class.getName());
  private UserDao userDao;
  private MessageDao messageDao;

  public JSONUtils(UserDao userDao, MessageDao messageDao) {
    this.userDao = userDao;
    this.messageDao = messageDao;
  }

  public JSONArray serializeMessages(List<Message> messages) {
    JSONArray jsonMessages = new JSONArray();
    for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
      Message message = (Message) iterator.next();
      JSONObject jsonMessage = new JSONObject();
      try {
        jsonMessage.put("user",    serializeUser(userDao.getById(message.senderId)));
        jsonMessage.put("message", serializeMessage(message));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      jsonMessages.put(jsonMessage);
    }
    return jsonMessages;
  }

  private JSONObject serializeMessage(Message message) throws JSONException {
    JSONObject jsonMessage = new JSONObject();
    jsonMessage.put("text",     message.text);
    jsonMessage.put("datetime", message.datetime.getTime());
    jsonMessage.put("distance", message.distance);
    return jsonMessage;
  }

  public JSONObject serializeUser(User user) throws JSONException {
    JSONObject jsonUser = new JSONObject();
    jsonUser.put("id",         user.id);
    jsonUser.put("name",       user.name);
    jsonUser.put("status",     user.status);
    jsonUser.put("pictureURL", user.pictureURL.toString());
    jsonUser.put("profileURL", user.profileURL.toString());
    return jsonUser;
  }

}
