package com.lunchandmeet.daos.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.net.UnknownHostException;

import org.bson.types.ObjectId;

import com.lunchandmeet.daos.PlaceDao;
import com.lunchandmeet.dto.Place;
import com.lunchandmeet.utils.DBPool;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.CommandResult;

public class PlaceDaoMongo implements PlaceDao {

  private DBCollection placeColl;
  private DB db;

  public PlaceDaoMongo() throws UnknownHostException {
    db = DBPool.getDb();
    placeColl = db.getCollection("places");
    placeColl.ensureIndex("name");
  }

  @Override
  public Place getById(String id) {
    if (id == null)
      return null;

    try {
      DBObject searchById = new BasicDBObject("_id", new ObjectId(id));
      return dbObject2Place(placeColl.findOne(searchById));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void setFavorite(String id, boolean favorite) {
    DBObject place = placeColl.findOne(new BasicDBObject( "_id", new ObjectId(id)));
    placeColl.update(place, new BasicDBObject("$set", new BasicDBObject("favorite", favorite)));
  }  

  private Place dbObject2Place(DBObject obj) {
    if (obj == null) {
        return null;
    }

    return new Place( obj.get("_id").toString(),
                     (String) obj.get("name"),
                     (String) obj.get("businessLunch"),
                     (String) obj.get("businessLunchTime"),
                     (String) obj.get("averageCheck"),
                     (String) obj.get("specialHeader"),
                     (String) obj.get("specialText"),
                     (String) obj.get("avatar"),
                     (String) obj.get("address"),
                     (String) obj.get("link"),
                     toCoords((BasicDBList) obj.get("location"))
                     );
  }

  private double[] toCoords(BasicDBList list) {
    if (list != null)
      return new double[] {(double) list.get(0), (double) list.get(1)};
    else
      return null;
  }

  @Override
  public List<Place> getAllPlaces() {
    List<Place> places = new ArrayList<Place>();
    DBCursor cursor = placeColl.find();
    while (cursor.hasNext()) {
        Place place = dbObject2Place(cursor.next());
        places.add(place);
    }
    return places;
  }

  /*@Override
  public List<User> getNearUsers(String myId) {

    DBObject searchById = new BasicDBObject("_id", new ObjectId(myId));
    User user =  dbObject2User(userColl.findOne(searchById));

    BasicDBObject query = new BasicDBObject();

    CommandResult result = db.command(getNearQuery((float)user.location[0], (float)user.location[1], query));
    BasicDBList dbMessages = (BasicDBList) result.get("results");

    List<User> users = new ArrayList<User>();

    for (Iterator<Object> iterator = dbMessages.iterator(); iterator.hasNext();) {
      DBObject object = (DBObject) iterator.next();
      users.add(dbObject2User((DBObject) object.get("obj")));
    }
    return users;
  }*/

  private DBObject getNearQuery(float latitude, float longitude, DBObject addQuery) {
    BasicDBObject query = new BasicDBObject();
    query.append("geoNear",            "users");
    query.append("near",               new double[] {latitude, longitude});
    query.append("spherical",          true);
    query.append("maxDistance",        (double) 1 / 6378);
    query.append("distanceMultiplier", 6378);
    query.append("query",              addQuery);
    return query;
  }

}
