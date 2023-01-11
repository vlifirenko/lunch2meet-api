package com.lunch2meet.daos;

import java.net.MalformedURLException;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.lunch2meet.dto.User;

public interface UserDao {

  public abstract User getByEmail(String email);

  public abstract User createUser(User user);

  public abstract void setStatus(String id, String status);

  public abstract User getById(String id);

}
