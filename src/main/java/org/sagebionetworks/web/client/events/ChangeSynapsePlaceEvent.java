package org.sagebionetworks.web.client.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;
import org.sagebionetworks.web.client.place.Synapse;

public class ChangeSynapsePlaceEvent extends GenericEvent {

  private final Synapse place;

  public ChangeSynapsePlaceEvent(Synapse place) {
    this.place = place;
  }

  public Synapse getPlace() {
    return place;
  }
}
