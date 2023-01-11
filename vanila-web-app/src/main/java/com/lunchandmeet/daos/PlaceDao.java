package com.lunchandmeet.daos;

import java.util.List;

import com.lunchandmeet.dto.Place;

public interface PlaceDao {

  public abstract void setFavorite(String id, boolean favorite);

  public abstract Place getById(String id);

  public abstract List<Place> getAllPlaces();

  //public abstract List<Place> getNearUsers(String myId);
}
