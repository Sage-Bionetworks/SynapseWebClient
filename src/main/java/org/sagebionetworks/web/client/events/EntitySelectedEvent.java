package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class EntitySelectedEvent extends GwtEvent<EntitySelectedHandler> {

	private static final Type TYPE = new Type<EntitySelectedHandler>();
	private String selectedEntityId;

	public EntitySelectedEvent(String selectedEntityId) {
		this.selectedEntityId = selectedEntityId;
	}

	public String getSelectedEntityId() {
		return selectedEntityId;
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<EntitySelectedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(EntitySelectedHandler handler) {
		handler.onSelection(this);
	}

}
