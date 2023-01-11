package com.lunchandmeet.daos;

import java.util.List;

import com.lunchandmeet.dto.Message;
import com.lunchandmeet.dto.User;

public interface MessageDao {

  public abstract void saveMessage(User sender, User reciever, String text, float latitude, float longitude);

  public abstract List<Message> getMessages(long sinceTimestamp, float latitude, float longitude);

  public abstract List<Message> getDialog(User sender, User reciever, long sinceTimestamp);

  public abstract int getDialogsCount(User user);

  public abstract List<Message> getDialogs(User user);

}
