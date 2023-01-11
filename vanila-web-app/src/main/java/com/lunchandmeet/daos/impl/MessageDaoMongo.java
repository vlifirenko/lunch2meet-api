package com.lunchandmeet.daos.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;

import com.lunchandmeet.utils.MessageComparator;
import com.lunchandmeet.daos.MessageDao;
import com.lunchandmeet.dto.Message;
import com.lunchandmeet.dto.User;
import com.lunchandmeet.utils.DBPool;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MessageDaoMongo implements MessageDao {

  private static final String COLLECTION      = "messages";
  private static final String FIELD_LOCATION  = "location";
  private static final String FIELD_SENDER    = "senderId";
  private static final String FIELD_RECIEVER  = "recieverId";
  private static final String FIELD_TEXT      = "text";
  private static final String FIELD_TIMESTAMP = "timestamp";
  private DBCollection messageColl;
  private DB db;

  public MessageDaoMongo() throws UnknownHostException {
    db = DBPool.getDb();
    messageColl = db.getCollection(COLLECTION);
    messageColl.ensureIndex(new BasicDBObject(FIELD_LOCATION, "2d"), "locationIdx");
    messageColl.ensureIndex(FIELD_TIMESTAMP);
  }

  @Override
  public void saveMessage(User sender, User reciever, String text, float latitude, float longitude) {
    String sId = sender.id;
    String rId = (reciever != null) ? reciever.id : null;
    BasicDBObject message = new BasicDBObject(FIELD_SENDER,    sId)
                                      .append(FIELD_RECIEVER,  rId)
                                      .append(FIELD_TEXT,      escapeString(text.substring(0, Math.min(140, text.length()))))
                                      .append(FIELD_TIMESTAMP, new Date().getTime())
                                      .append(FIELD_LOCATION,  new double[]{latitude, longitude});
    messageColl.insert(message);
  }

  @Override
  public List<Message> getMessages(long sinceTimestamp, float latitude, float longitude) {
    Date fourHoursAgo = new Date(System.currentTimeMillis() - (6 * 60 * 60 * 1000));
    BasicDBObject document = new BasicDBObject();
    document.put(FIELD_TIMESTAMP, new BasicDBObject("$lt", fourHoursAgo.getTime()));
    messageColl.remove(document);


    // http://docs.mongodb.org/manual/reference/operator/near/

    // http://stackoverflow.com/questions/8774875/mongodb-and-ruby-runcommand-geonear-and-sorting-by-date
    // { loc :  { $nearSphere : [80.21223299999997, 13.034892],$maxDistance:20/6378  } }

    // http://chuckjohnson.wordpress.com/2012/04/02/geospatial-location-based-searches-in-mongodb-part-2-simple-searching/
    // BasicDBObject(�loc�, JSON.parse(�{$near : [ " + dLng + "," + dLat + " ] , $maxDistance : � + sDistance + �}�))).limit(10);

    //DBObject query0 = new BasicDBObject(FIELD_LOCATION, new BasicDBObject("$near", new double[] {longitude, latitude}));
    //DBCursor cursor = messageColl.find(query).sort(new BasicDBObject(FIELD_TIMESTAMP, -1));

    DBObject query1 = new BasicDBObject(FIELD_TIMESTAMP, new BasicDBObject("$gt", sinceTimestamp));
    DBObject query2 = new BasicDBObject(FIELD_RECIEVER, null);

    ArrayList<DBObject> andList = new ArrayList<DBObject>();
    andList.add(query1);
    andList.add(query2);
    BasicDBObject query = new BasicDBObject("$and", andList);

    List<Message> messages = new ArrayList<Message>();

    CommandResult result = db.command(getNearQuery(latitude, longitude, query));
    BasicDBList dbMessages = (BasicDBList) result.get("results");
    for (Iterator<Object> iterator = dbMessages.iterator(); iterator.hasNext();) {
      DBObject object = (DBObject) iterator.next();
      messages.add(dbObject2Message((DBObject) object.get("obj"), (Double) object.get("dis")));
    }
    Collections.sort(messages, new MessageComparator());
    return messages;
  }

  @Override
  public List<Message> getDialog(User sender, User reciever, long sinceTimestamp) {
    DBObject query0 = new BasicDBObject(FIELD_TIMESTAMP, new BasicDBObject("$gt", sinceTimestamp));


    DBObject query1 = new BasicDBObject(FIELD_SENDER,    sender.id);
    DBObject query2 = new BasicDBObject(FIELD_RECIEVER,  reciever.id);
    ArrayList<DBObject> andList0 = new ArrayList<DBObject>();
    andList0.add(query1);
    andList0.add(query2);
    BasicDBObject andQuery1 = new BasicDBObject("$and", andList0);

    DBObject query3 = new BasicDBObject(FIELD_SENDER,    reciever.id);
    DBObject query4 = new BasicDBObject(FIELD_RECIEVER,  sender.id);
    ArrayList<DBObject> andList1 = new ArrayList<DBObject>();
    andList1.add(query3);
    andList1.add(query4);
    BasicDBObject andQuery2 = new BasicDBObject("$and", andList1);


    ArrayList<DBObject> orList = new ArrayList<DBObject>();
    orList.add(andQuery1);
    orList.add(andQuery2);
    BasicDBObject orQuery = new BasicDBObject("$or", orList);


    ArrayList<DBObject> andList = new ArrayList<DBObject>();
    andList.add(query0);
    andList.add(orQuery);
    BasicDBObject query = new BasicDBObject("$and", andList);

    List<Message> messages = new ArrayList<Message>();
    DBCursor cursor = messageColl.find(query).sort(new BasicDBObject(FIELD_TIMESTAMP, 1));
    while (cursor.hasNext()) {
        messages.add(dbObject2Message(cursor.next(), 0d));
    }
    return messages;
  }

  @Override
  public int getDialogsCount(User user) {
    return getDialogs(user).size();
  }

    @Override
  public List<Message> getDialogs(User user) {
//	  DBObject query0 = new BasicDBObject(FIELD_TIMESTAMP, new BasicDBObject("$gt", sinceTimestamp));
    DBObject query1 = new BasicDBObject(FIELD_RECIEVER, user.id);
//	  ArrayList<DBObject> andList = new ArrayList<DBObject>();
//	  andList.add(query0);
//	  andList.add(query1);
//	  BasicDBObject query = new BasicDBObject("$and", andList);

    BasicDBObject cmd = new BasicDBObject();
    cmd.append("distinct", "messages");
    cmd.append("key",      FIELD_SENDER);
    cmd.append("query",    query1);

    List<Message> messages = new ArrayList<Message>();
    CommandResult result = db.command(cmd);
    BasicDBList messagesIds = (BasicDBList) result.get("values");
    for (Iterator<Object> iterator = messagesIds.iterator(); iterator.hasNext();) {
      String senderId = (String) iterator.next();
      messages.add(dbObject2Message(getLastMessage(senderId, user.id), 0d));
    }
    Collections.sort(messages, new MessageComparator());
    return messages;
  }

  private DBObject getLastMessage(String senderId, String recieverId) {
    DBObject query0 = new BasicDBObject(FIELD_SENDER, senderId);
    DBObject query1 = new BasicDBObject(FIELD_RECIEVER, recieverId);
    ArrayList<DBObject> andList = new ArrayList<DBObject>();
    andList.add(query0);
    andList.add(query1);
    BasicDBObject query = new BasicDBObject("$and", andList);
    DBCursor cursor = messageColl.find(query).sort(new BasicDBObject(FIELD_TIMESTAMP, -1)).limit(1);
    return (cursor.hasNext()) ? cursor.next() : null;
  }

  private DBObject getById(String id) {
    if (id == null)
      return null;

    try {
      DBObject searchById = new BasicDBObject("_id", new ObjectId(id));
      return messageColl.findOne(searchById);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private Message dbObject2Message(DBObject obj, Double distance) {
    if (obj == null)
      return null;

    return new Message( (String)   obj.get(FIELD_SENDER),
                        (String)   obj.get(FIELD_RECIEVER),
                        (long)     obj.get(FIELD_TIMESTAMP),
                        (String)   obj.get(FIELD_TEXT),
                        distance.doubleValue(),
                        toCoords((BasicDBList) obj.get(FIELD_LOCATION)) );
  }

  private DBObject getNearQuery(float latitude, float longitude, DBObject addQuery) {
    BasicDBObject query = new BasicDBObject();
    query.append("geoNear",            COLLECTION);
    query.append("near",               new double[] {latitude, longitude});
    query.append("spherical",          true);
    //query.append("maxDistance",        (double) 50 / 3959 );
    query.append("maxDistance",        (double) 1 / 6378);
    query.append("distanceMultiplier", 6378);
    query.append("query",              addQuery);
    return query;
  }

  private double[] toCoords(BasicDBList list) {
    return new double[] {(double) list.get(0), (double) list.get(1)};
  }

  private String escapeString(String text) {
    text = text.replaceAll("<",  "&lt;");
    text = text.replaceAll(">",  "&gt;");
    text = text.replaceAll("&",  "&amp;");
    text = text.replaceAll("\"", "&quot;");
    return text;
  }

}
