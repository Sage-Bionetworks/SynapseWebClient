package org.sagebionetworks.web.client.widget.breadcrumb;

import com.google.gwt.place.shared.Place;
import org.sagebionetworks.repo.model.EntityType;

public class LinkData {

  private String text;
  private EntityType entityType;
  private Place place;

  public LinkData(String text, Place place) {
    this(text, null, place);
  }

  public LinkData(String text, EntityType entityType, Place place) {
    this.text = text;
    this.entityType = entityType;
    this.place = place;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(Place place) {
    this.place = place;
  }
}
