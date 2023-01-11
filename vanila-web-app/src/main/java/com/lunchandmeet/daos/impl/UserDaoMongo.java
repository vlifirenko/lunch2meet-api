package com.lunchandmeet.daos.impl;

import com.lunchandmeet.daos.UserDao;
import com.lunchandmeet.dto.User;
import com.lunchandmeet.utils.DBPool;
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserDaoMongo implements UserDao {

    private DBCollection userColl;
    private DB db;

    public UserDaoMongo() throws UnknownHostException {
        db = DBPool.getDb();
        userColl = db.getCollection("users");
        userColl.ensureIndex("email");
    }

    @Override
    public User getByEmail(String email) {
        DBObject query = new BasicDBObject("email", email);
        return dbObject2User(userColl.findOne(query));
    }

    @Override
    public User getByProfileUrl(String profileUrl) {
        DBObject query = new BasicDBObject("profileURL", profileUrl);
        return dbObject2User(userColl.findOne(query));
    }

    @Override
    public User getByToken(String token) {
        DBObject query = new BasicDBObject("token", token);
        return dbObject2User(userColl.findOne(query));
    }

    private static final String SALT = "sahgowyvnxz2gqiasjzxcg";

    @Override
    public User createUser(User user) {
        User searchUser;
        if (user.email != null)
            searchUser = getByEmail(user.email);
        else
            searchUser = getByProfileUrl(user.profileURL);
        if (searchUser == null) {
            user.token = md5Java(System.currentTimeMillis() + SALT);
            BasicDBObject userObj = new BasicDBObject("email", user.email)
                    .append("name", user.name)
                    .append("profileURL", user.profileURL)
                    .append("pictureURL", user.pictureURL)
                    .append("status", user.status)
                    .append("token", user.token)
                    .append("location", user.location);
            userColl.insert(userObj);
            user.id = userObj.get("_id").toString();
            return user;
        }
        System.out.println(searchUser.token);
        return searchUser;
    }

    @Override
    public void setStatus(String id, String status) {
        DBObject user = userColl.findOne(new BasicDBObject("_id", new ObjectId(id)));
        userColl.update(user, new BasicDBObject("$set", new BasicDBObject("status", status)));
    }

    @Override
    public void setLocation(String id, double[] location) {
        DBObject user = userColl.findOne(new BasicDBObject("_id", new ObjectId(id)));
        userColl.update(user, new BasicDBObject("$set", new BasicDBObject("location", location)));
    }

    @Override
    public void setToken(String id, String token) {
        DBObject user = userColl.findOne(new BasicDBObject("_id", new ObjectId(id)));
        userColl.update(user, new BasicDBObject("$set", new BasicDBObject("token", token)));
    }

    @Override
    public User getById(String id) {
        if (id == null)
            return null;

        try {
            DBObject searchById = new BasicDBObject("_id", new ObjectId(id));
            return dbObject2User(userColl.findOne(searchById));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private User dbObject2User(DBObject obj) {
        if (obj == null) {
            return null;
        }

        return new User(obj.get("_id").toString(),
                (String) obj.get("email"),
                (String) obj.get("name"),
                (String) obj.get("profileURL"),
                (String) obj.get("pictureURL"),
                (String) obj.get("status"),
                (String) obj.get("token"));
        //toCoords((BasicDBList) obj.get("location")));
    }

    private double[] toCoords(BasicDBList list) {
        if (list != null)
            return new double[]{(double) list.get(0), (double) list.get(1)};
        else
            return null;
    }

    @Override
    public List<User> getAllUsers(String myId) {
        List<User> users = new ArrayList<User>();
        DBCursor cursor = userColl.find();
        while (cursor.hasNext()) {
            User user = dbObject2User(cursor.next());
            //if (user != null && !myId.equals(user.id))
            users.add(user);
        }
        return users;
    }

    @Override
    public List<User> getNearUsers(String myId) {

        DBObject searchById = new BasicDBObject("_id", new ObjectId(myId));
        User user = dbObject2User(userColl.findOne(searchById));

        BasicDBObject query = new BasicDBObject();

        CommandResult result = db.command(getNearQuery((float) user.location[0], (float) user.location[1], query));
        BasicDBList dbMessages = (BasicDBList) result.get("results");

        List<User> users = new ArrayList<User>();

        for (Iterator<Object> iterator = dbMessages.iterator(); iterator.hasNext(); ) {
            DBObject object = (DBObject) iterator.next();
            users.add(dbObject2User((DBObject) object.get("obj")));
        }
        return users;
    }

    private DBObject getNearQuery(float latitude, float longitude, DBObject addQuery) {
        BasicDBObject query = new BasicDBObject();
        query.append("geoNear", "users");
        query.append("near", new double[]{latitude, longitude});
        query.append("spherical", true);
        query.append("maxDistance", (double) 1 / 6378);
        query.append("distanceMultiplier", 6378);
        query.append("query", addQuery);
        return query;
    }

    public static String md5Java(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest;
    }

}
