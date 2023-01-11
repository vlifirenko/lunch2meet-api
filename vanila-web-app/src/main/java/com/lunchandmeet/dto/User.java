package com.lunchandmeet.dto;

public class User {

  public String id;
  public String email;
  public String name;
  public String profileURL;
  public String pictureURL;
  public String status;
  public String token;
  public double[] location;
  public boolean active;

  public User(String id, String email, String name, String profileURL, String pictureURL, String status, String token) {
    super();
    this.id         = id;
    this.email      = email;
    this.name       = name;
    this.profileURL = profileURL;
    this.pictureURL = pictureURL;
    this.status     = status;
    this.token      = token;
  }

  public User() {
    // TODO Auto-generated constructor stub
  }

}
