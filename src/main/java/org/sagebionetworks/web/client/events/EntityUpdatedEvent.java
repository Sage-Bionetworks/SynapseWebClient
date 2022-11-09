package org.sagebionetworks.web.client.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * When fired, refreshes the EntityPresenter and clears the react-query cache for the specified entity, or all entities
 * if not specified.
 */
public class EntityUpdatedEvent extends GenericEvent {

  private String entityId = null;

  public EntityUpdatedEvent() {
    super();
  }

  public EntityUpdatedEvent(String entityId) {
    super();
    this.entityId = entityId;
  }

  /**
   * Gets the ID of the updated entity. May be null.
   * @return
   */
  public String getEntityId() {
    return entityId;
  }
}
