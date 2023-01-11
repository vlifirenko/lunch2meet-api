package com.lunchandmeet.dto;

public class Place {

  public String id;
  public String name;
  public String businessLunch;
  public String businessLunchTime;
  public String averageCheck;
  public boolean favorite;
  public String distance;
  public String specialHeader;
  public String specialText;
  public String avatar;
  public String address;
  public String link;
  public double[] location;

  public Place(String id, String name, String businessLunch, String businessLunchTime, String averageCheck, String specialHeader, String specialText, String avatar, String address, String link, double[] location) {
    super();
    this.id                 = id;
    this.name               = name;
    this.businessLunch      = businessLunch;
    this.businessLunchTime  = businessLunchTime;
    this.averageCheck       = averageCheck;
    this.specialHeader      = specialHeader;
    this.specialText        = specialText;
    this.avatar             = avatar;
    this.address            = address;
    this.link               = link;
    this.location           = location;
  }

  public Place() {
    // TODO Auto-generated constructor stub
  }

}
