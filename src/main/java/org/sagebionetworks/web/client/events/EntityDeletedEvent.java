package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class EntityDeletedEvent extends GwtEvent<EntityDeletedHandler> {

	private static final Type<EntityDeletedHandler> TYPE = new Type<EntityDeletedHandler>();
	private String deletedId;
	
	public EntityDeletedEvent(String deletedId) {
		this.deletedId = deletedId;
	}
	
	public String getDeletedId() {
		return deletedId;
	}
	
	public static Type<EntityDeletedHandler> getType() {
		return TYPE;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<EntityDeletedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(EntityDeletedHandler handler) {
		handler.onDeleteSuccess(this);
	}

}
