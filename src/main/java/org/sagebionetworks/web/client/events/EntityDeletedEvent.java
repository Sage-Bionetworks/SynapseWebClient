package org.sagebionetworks.web.client.events;

import org.sagebionetworks.repo.model.Entity;

import com.google.gwt.event.shared.GwtEvent;

public class EntityDeletedEvent extends GwtEvent<EntityDeletedHandler> {

	private static final Type<EntityDeletedHandler> TYPE = new Type<EntityDeletedHandler>();
	private String deletedId;
	private Class<? extends Entity> clazz;
	
	public EntityDeletedEvent(String deletedId, Class<? extends Entity> clazz) {
		this.deletedId = deletedId;
		this.clazz = clazz;
	}
	
	public String getDeletedId() {
		return deletedId;
	}
	
	public Class<? extends Entity> getClazz() {
		return clazz;
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
