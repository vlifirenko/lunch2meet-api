package com.lunchandmeet.dto;

public class Message {

  public String senderId;
  public String recieverId;
  public String text;
  public long timestamp;
  public double distance;
  public double[] location;

  public Message(String senderId, String recieverId, long timestamp, String text, double distance, double[] location) {
    super();
    this.senderId   = senderId;
    this.recieverId = recieverId;
    this.text       = text;
    this.timestamp  = timestamp;
    this.distance   = distance;
    this.location   = location;
  }

}
