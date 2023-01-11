package com.lunch2meet.utils;

import java.util.Comparator;
import java.util.Date;

import com.lunch2meet.dto.Message;

public class MessageComparator implements Comparator<Message> {

  @Override
  public int compare(Message o1, Message o2) {
    Date date1 = o1.datetime;
    Date date2 = o2.datetime;
    return (int) (date1.getTime() - date2.getTime());
  }

}
