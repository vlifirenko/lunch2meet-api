package com.lunch2meet.daos.impl;

import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Query;
import com.lunch2meet.daos.UserDao;
import com.lunch2meet.dto.User;

public class UserDaoGAE implements UserDao {

  private static final Logger log = Logger.getLogger(UserDaoGAE.class.getName());
  private DatastoreService datastore;

  public UserDaoGAE() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.UserDao#getByEmail(java.lang.String)
   */
  @Override
  public User getByEmail(String email) {
    Query q = new Query("User").addFilter("email", Query.FilterOperator.EQUAL, new Email(email));
    List<Entity> users = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(1));
    return users.isEmpty() ? null : entity2user(users.get(0));
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.UserDao#createUser(com.lunch2meet.dto.User)
   */
  @Override
  public User createUser(User user) {
    Key userKey = KeyFactory.createKey("User", user.email);
    Entity entity = new Entity("User", userKey);
    entity.setProperty("email",      new Email(user.email));
    entity.setProperty("name",       user.name);
    entity.setProperty("status",     user.status);
    entity.setProperty("profileURL", new Link(user.profileURL.toString()));
    String pictureUrl = (user.pictureURL != null) ? ((String) user.pictureURL.toString()) : "http://oauth-python-test.appspot.com/img/no-picture.jpg";
    entity.setProperty("pictureURL", new Link(pictureUrl));
    datastore.put(entity);
    user.id = KeyFactory.keyToString(entity.getKey());
    return user;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.UserDao#setStatus(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void setStatus(String id, String status) {
    try {
      Entity user = datastore.get(KeyFactory.stringToKey(id));
      user.setProperty("status", status);
      datastore.put(user);
    } catch (EntityNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.UserDao#getById(java.lang.String)
   */
  @Override
  public User getById(String id) {
    Entity userEntity = null;
    User user = null;
    try {
      userEntity = datastore.get(KeyFactory.stringToKey(id));
      user = entity2user(userEntity);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return user;
  }

  private User entity2user(Entity entity) {
    User user = new User();
    user.id     = KeyFactory.keyToString(entity.getKey());
    user.email  = ((Email) entity.getProperty("email")).getEmail();
    user.name   = (String) entity.getProperty("name");
    user.status = (String) entity.getProperty("status");
    try {
      user.profileURL = new URL(((Link) entity.getProperty("profileURL")).getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      user.pictureURL = new URL(((Link) entity.getProperty("pictureURL")).getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return user;
  }

}
