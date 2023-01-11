package com.lunchandmeet.daos;

import java.util.List;

import com.lunchandmeet.dto.User;

public interface UserDao {

  public abstract User getByEmail(String email);

  public abstract User getByProfileUrl(String profileUrl);

  public abstract User createUser(User user);

  public abstract void setStatus(String id, String status);

  public abstract void setLocation(String id, double[] location);

  public abstract void setToken(String id, String token);

  public abstract User getById(String id);

  public abstract User getByToken(String token);

  public abstract List<User> getAllUsers(String myId);

  public abstract List<User> getNearUsers(String myId);
}
