package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class EntitySelectedEvent extends GwtEvent<EntitySelectedHandler> {

	private static final Type TYPE = new Type<EntitySelectedHandler>();
	
	public EntitySelectedEvent() {
		
	}
	
	public static Type getType() {
		return TYPE;
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
