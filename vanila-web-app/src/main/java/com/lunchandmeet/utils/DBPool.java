package com.lunchandmeet.utils;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class DBPool {

  private static DB db = null;

  public static DB getDb() throws UnknownHostException {
    if (db == null) {
      MongoClient mongoClient = new MongoClient();
      db = mongoClient.getDB("lunchandmeet");
    }
    return db;
  }

}
