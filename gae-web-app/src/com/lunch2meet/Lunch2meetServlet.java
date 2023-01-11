package com.lunch2meet;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.lunch2meet.daos.MessageDao;
import com.lunch2meet.daos.UserDao;
import com.lunch2meet.daos.impl.MessageDaoGAE;
import com.lunch2meet.daos.impl.UserDaoGAE;
import com.lunch2meet.dto.Message;
import com.lunch2meet.dto.User;
import com.lunch2meet.utils.JSONUtils;

@SuppressWarnings("serial")
public class Lunch2meetServlet extends HttpServlet {

  private UserDao userDao;
  private MessageDao messageDao;
  private JSONUtils jsonUtils;

  public Lunch2meetServlet() {
    userDao = new UserDaoGAE();
    messageDao = new MessageDaoGAE();
    jsonUtils = new JSONUtils(userDao, messageDao);
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String action = req.getParameter("action");
    String id = (String) req.getSession().getAttribute("id");
    String jsonString = "{}";
    switch (action) {
    case "set_status":
      userDao.setStatus(id, req.getParameter("status"));
      break;

    case "get_messages":
      jsonString = getMessagesJson(
          Long.parseLong(req.getParameter("since")),
          Float.parseFloat(req.getParameter("latitude")),
          Float.parseFloat(req.getParameter("longitude")));
      break;

    case "get_profile":
      String profileId = req.getParameter("id");
      jsonString = getProfileJson(profileId);
      break;

    case "send_message":
      saveMessage( id,
                   req.getParameter("reciever"),
                   req.getParameter("text"),
                   Float.parseFloat(req.getParameter("latitude")),
                   Float.parseFloat(req.getParameter("longitude")));
      break;

    case "get_dialog":
      jsonString = getDialogJson( id,
                                  req.getParameter("reciever"),
                                  Long.parseLong(req.getParameter("since")),
                                  Float.parseFloat(req.getParameter("latitude")),
                                  Float.parseFloat(req.getParameter("longitude")));
      break;

    case "get_dialogs_count":
      jsonString = getDialogsCountJson(id);
      break;

    case "get_dialogs":
      jsonString = getDialogsJson(id);
      break;
    }

    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().println(jsonString);
  }

  private String getDialogsJson(String id) {
    String json = "[]";
    try {
      User user = userDao.getById(id);
      json = jsonUtils.serializeMessages(messageDao.getDialogs(user)).toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return json;
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

  private void saveMessage(String senderId, String recieverId, String text, float latitude, float longitude) {
    User reciever = (recieverId != null && !recieverId.equals("")) ? userDao.getById(recieverId) : null;
    User sender = userDao.getById(senderId);
    messageDao.saveMessage(sender, reciever, text, latitude, longitude);
  }

  private String getMessagesJson(long sinceTimestamp, float latitude, float longitude) {
    String json = "[]";
    // try {
    Date sinceDate = new Date(sinceTimestamp);
    List<Message> messages = messageDao.getMessages(sinceDate, latitude, longitude);
    json = jsonUtils.serializeMessages(messages).toString();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    return json;
  }

  private String getDialogJson(String id, String recieverId, long sinceTimestamp, float latitude, float longitude) {
    String json = "[]";
    // try {
    User sender = userDao.getById(id);
    User reciever = userDao.getById(recieverId);
    Date sinceDate = new Date(sinceTimestamp);
    List<Message> messages = messageDao.getDialog(sender, reciever, sinceDate, latitude, longitude);
    json = jsonUtils.serializeMessages(messages).toString();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    return json;
  }

}
