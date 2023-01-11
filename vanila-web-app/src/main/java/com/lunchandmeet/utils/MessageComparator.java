package com.lunchandmeet.utils;

import java.util.Comparator;

import com.lunchandmeet.dto.Message;

public class MessageComparator implements Comparator<Message> {

  @Override
  public int compare(Message o1, Message o2) {
    return (int) (o1.timestamp - o2.timestamp);
  }

}
