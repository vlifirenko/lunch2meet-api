package com.lunch2meet.daos.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.labs.repackaged.com.google.common.collect.Iterables;
import com.lunch2meet.daos.MessageDao;
import com.lunch2meet.dto.Message;
import com.lunch2meet.dto.User;
import com.lunch2meet.utils.MessageComparator;

public class MessageDaoGAE implements MessageDao {

  private static final Logger log = Logger.getLogger(MessageDaoGAE.class.getName());
  private DatastoreService datastore;

  public MessageDaoGAE() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.MessageDao#saveMessage(com.lunch2meet.dto.User,
   * com.lunch2meet.dto.User, java.lang.String, float, float)
   */
  @Override
  public void saveMessage(User sender, User reciever, String text, float latitude, float longitude) {
    Entity message = new Entity("Message");
    message.setProperty("sender", KeyFactory.stringToKey(sender.id));
    Key recieverKey = (reciever == null) ? null : KeyFactory.stringToKey(reciever.id);
    message.setProperty("reciever", recieverKey);
    message.setProperty("text", escapeString(text));
    message.setProperty("location", new GeoPt(latitude, longitude));
    message.setProperty("datetime", new Date());
    message.setProperty("status", 1);
    datastore.put(message);
  }

  private String escapeString(String text) {
    // text = text.replaceAll("<", "&lt;");
    // text = text.replaceAll(">", "&gt;");
    // text = text.replaceAll("&", "&amp;");
    // text = text.replaceAll("\"", "&quot;");
    return text;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.MessageDao#getMessages(java.util.Date, float,
   * float)
   */
  @Override
  public List<Message> getMessages(Date sinceDate, float latitude, float longitude) {
    archiveOldMessages();

    float longDelta = 0.01f;
    float latDelta = 0.01f;
    Filter locationMinFilter = new FilterPredicate("location", FilterOperator.GREATER_THAN, new GeoPt(latitude - latDelta, longitude - longDelta));
    Filter locationMaxFilter = new FilterPredicate("location", FilterOperator.LESS_THAN, new GeoPt(latitude + latDelta, longitude + longDelta));
    Filter recieverFilter    = new FilterPredicate("reciever", FilterOperator.EQUAL, null);
    Filter statusFilter      = new FilterPredicate("status", FilterOperator.EQUAL, 1);

    Filter messagesFilter = CompositeFilterOperator.and(locationMinFilter, locationMaxFilter, recieverFilter, statusFilter);

    Query q = new Query("Message").setFilter(messagesFilter);
    PreparedQuery pq = datastore.prepare(q);
    return filterLocateAndSort(sinceDate, latitude, longitude, pq);
  }

  private List<Message> filterLocateAndSort(Date sinceDate, float latitude, float longitude, PreparedQuery pq) {
    GeoPt messagePoint;
    List<Message> a = new ArrayList<Message>();
    for (Entity message : pq.asIterable()) {
      // List<Entity> b =
      // datastore.prepare(q).asList(FetchOptions.Builder.withLimit(20));
      // for (Iterator<Entity> iterator = b.iterator();
      // iterator.hasNext();) {
      // Entity message = (Entity) iterator.next();
      if (((Date) message.getProperty("datetime")).after(sinceDate)) {
        messagePoint = (GeoPt) message.getProperty("location");
        message.setProperty("distance", getDistanceBetweenPoints( messagePoint.getLatitude(),
                                                                  messagePoint.getLongitude(),
                                                                  latitude,
                                                                  longitude));
        a.add(entity2message(message));
      }
    }
    Collections.sort(a, new MessageComparator());
    return a;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.MessageDao#getDialog(com.lunch2meet.dto.User,
   * com.lunch2meet.dto.User, java.util.Date, float, float)
   */
  @Override
  public List<Message> getDialog(User sender, User reciever, Date sinceDate, float latitude, float longitude) {
    Key senderKey = KeyFactory.stringToKey(sender.id);
    Key recieverKey = KeyFactory.stringToKey(reciever.id);

    Filter senderFilter1 = new FilterPredicate("sender", FilterOperator.EQUAL, senderKey);
    Filter recieverFilter1 = new FilterPredicate("reciever", FilterOperator.EQUAL, recieverKey);
    Filter sr1 = CompositeFilterOperator.and(senderFilter1, recieverFilter1);

    Filter senderFilter2 = new FilterPredicate("sender", FilterOperator.EQUAL, recieverKey);
    Filter recieverFilter2 = new FilterPredicate("reciever", FilterOperator.EQUAL, senderKey);
    Filter sr2 = CompositeFilterOperator.and(senderFilter2, recieverFilter2);

    Filter sr = CompositeFilterOperator.or(sr1, sr2);

    Filter dateFilter = new FilterPredicate("datetime", FilterOperator.GREATER_THAN, sinceDate);
    Filter statusFilter = new FilterPredicate("status", FilterOperator.EQUAL, 1);

    Filter messagesFilter = CompositeFilterOperator.and(sr, dateFilter, statusFilter);

    Query q = new Query("Message").setFilter(messagesFilter);
    PreparedQuery pq = datastore.prepare(q);
    GeoPt messagePoint;
    List<Message> a = new ArrayList<Message>();
    for (Entity message : pq.asIterable()) {
      messagePoint = (GeoPt) message.getProperty("location");
      message.setProperty("distance", getDistanceBetweenPoints( messagePoint.getLatitude(),
                                                                messagePoint.getLongitude(),
                                                                latitude,
                                                                longitude));
      a.add(entity2message(message));
    }
    Collections.sort(a, new MessageComparator());
    return a;
  }

  private Message entity2message(Entity entity) {
    Message message = new Message();
    message.text = (String) entity.getProperty("text");
    GeoPt pt = (GeoPt) entity.getProperty("location");
    message.location = new float[]{pt.getLatitude(), pt.getLongitude()};
    message.distance = (float) ((double) entity.getProperty("distance"));
    message.senderId = KeyFactory.keyToString((Key) entity.getProperty("sender"));
    Key recieverKey = (Key) entity.getProperty("reciever");
    if (recieverKey != null)
      message.recieverId = KeyFactory.keyToString(recieverKey);
    message.datetime = (Date) entity.getProperty("datetime");
    return message;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.lunch2meet.daos.MessageDao#getDialogsCount(com.lunch2meet.dto.User)
   */
  @Override
  public int getDialogsCount(User user) {
    return getDialogs(user).size();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.lunch2meet.daos.MessageDao#getDialogs(com.lunch2meet.dto.User)
   */
  @Override
  public List<Message> getDialogs(User user) {
    float[] pos = getPosition(user);
    GeoPt userPos = new GeoPt(pos[0], pos[1]);

    Filter recieverFilter = new FilterPredicate("reciever", FilterOperator.EQUAL, KeyFactory.stringToKey(user.id));
    Filter statusFilter = new FilterPredicate("status", FilterOperator.EQUAL, 1);
    Filter messagesFilter = CompositeFilterOperator.and(recieverFilter, statusFilter);
    Query q = new Query("Message").setFilter(messagesFilter);
    List<Entity> list = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(1000));
    Map<String, Message> messagesMap = new HashMap<String, Message>();
    String keyString;
    GeoPt messagePoint;
    MessageComparator mc = new MessageComparator();
    for (int i = list.size() - 1; i >= 0; i--) {
      Entity message = list.get(i);
      keyString = ((Key) message.getProperty("sender")).toString();
      if (!messagesMap.containsKey(keyString) || mc.compare(messagesMap.get(keyString), entity2message(message)) < 0) {
        messagePoint = (GeoPt) message.getProperty("location");
        message.setProperty("distance", getDistanceBetweenPoints( messagePoint.getLatitude(),
                                                                  messagePoint.getLongitude(),
                                                                  userPos.getLatitude(),
                                                                  userPos.getLongitude()));
        messagesMap.put(keyString, entity2message(message));
      }
    }
    List<Message> a = new ArrayList<Message>(messagesMap.values());
    Collections.sort(a, new MessageComparator());
    return a;
  }

  private float[] getPosition(User user) {
    GeoPt pt = new GeoPt(0, 0);
    Filter senderFilter = new FilterPredicate("sender", FilterOperator.EQUAL, KeyFactory.stringToKey(user.id));
    Query q = new Query("Message").setFilter(senderFilter).addSort("datetime", Query.SortDirection.DESCENDING);
    List<Entity> list = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(1));
    if (list.size() > 0 && list.get(0).getProperty("location") != null) {
      pt = (GeoPt) list.get(0).getProperty("location");
    }
    return new float[] { pt.getLatitude(), pt.getLongitude() };
  }

  private void archiveOldMessages() {
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date());
    cal.add(Calendar.HOUR, -3);
    Date threeHoursAgo = cal.getTime();

    Query uq = new Query("Message").addFilter("datetime", FilterOperator.LESS_THAN, threeHoursAgo);
    PreparedQuery upq = datastore.prepare(uq);
    List<Entity> toUpdate = new ArrayList<Entity>();
    for (Entity message : upq.asIterable()) {
      message.setProperty("status", 0);
      toUpdate.add(message);
    }
    datastore.put(toUpdate);
  }

  private double getDistanceBetweenPoints(float latitude1, float longitude1, float latitude2, float longitude2) {
    double theta = longitude1 - longitude2;
    double distance = (Math.sin(deg2rad(latitude1)) * Math.sin(deg2rad(latitude2)))
                      + (Math.cos(deg2rad(latitude1)) * Math.cos(deg2rad(latitude2)) * Math.cos(deg2rad(theta)));
    distance = Math.acos(distance);
    distance = rad2deg(distance);
    distance = distance * 60 * 1.1515f;
    distance = distance * 1.609344f;
    return distance;
  }

  private double deg2rad(double deg) {
    return (deg * Math.PI / 180.0);
  }

  private double rad2deg(double rad) {
    return (rad * 180.0 / Math.PI);
  }

}
