package com.lunch2meet.daos;

import java.util.Date;
import java.util.List;

import com.lunch2meet.dto.Message;
import com.lunch2meet.dto.User;

public interface MessageDao {

	public abstract void saveMessage(User sender, User reciever, String text,
			float latitude, float longitude);

	public abstract List<Message> getMessages(Date sinceDate, float latitude,
			float longitude);

	public abstract List<Message> getDialog(User sender, User reciever,
			Date sinceDate, float latitude, float longitude);

	public abstract int getDialogsCount(User user);

	public abstract List<Message> getDialogs(User user);

}