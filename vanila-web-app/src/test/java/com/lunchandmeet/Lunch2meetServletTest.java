package com.lunchandmeet;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.lunchandmeet.daos.impl.MessageDaoMongo;
import com.lunchandmeet.daos.impl.UserDaoMongo;
import com.lunchandmeet.dto.Message;
import com.lunchandmeet.dto.User;

public class Lunch2meetServletTest {

  @Test
  public void testGetMessagesJson() throws UnknownHostException {
    MessageDaoMongo messageDao = new MessageDaoMongo();
    List<Message> messages = messageDao.getMessages(0, 0, 0);

    Map<String, User> a = getUsers4Messages(messages);
  }

  private Map<String, User> getUsers4Messages(List<Message> messages) throws UnknownHostException {
      Map<String, User> users = new HashMap<String, User>();

      if (messages == null) {
        return users;
      }

      String id;
      Message message;
      UserDaoMongo userDao = new UserDaoMongo();
      for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
        message = (Message) iterator.next();
        id = message.senderId;
        if (!users.containsKey(id)) {
          users.put(id, userDao.getById(id));
        }
      }
      return users;
    }

}
