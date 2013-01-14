package org.sagebionetworks.web.client.events;

import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.event.shared.GwtEvent;

public class WidgetDescriptorUpdatedEvent extends GwtEvent<WidgetDescriptorUpdatedHandler> {

	private static final Type<WidgetDescriptorUpdatedHandler> TYPE = new Type<WidgetDescriptorUpdatedHandler>();
	private String name, oldName;
	private EntityWrapper entityWrapper;
	private boolean isDeleted;
	
	//some entities might want to simply insert some constant text into the description instead of updating the attachments (external image will do this)
	private String insertValue;
	
	public WidgetDescriptorUpdatedEvent() {
		isDeleted = false;
	}
	
	public static Type<WidgetDescriptorUpdatedHandler> getType() {
		return TYPE;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<WidgetDescriptorUpdatedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(WidgetDescriptorUpdatedHandler handler) {
		handler.onUpdate(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * This holds what the attachment name used to be (support the case when the attachment has been renamed)
	 * @param oldName
	 */
	public void setOldName(String oldName) {
		this.oldName = oldName;
	}
	public String getOldName() {
		return oldName;
	}
	
	
	public EntityWrapper getEntityWrapper() {
		return entityWrapper;
	}
	public void setEntityWrapper(EntityWrapper entityWrapper) {
		this.entityWrapper = entityWrapper;
	}
	public String getInsertValue() {
		return insertValue;
	}
	public void setInsertValue(String insertConstant) {
		this.insertValue = insertConstant;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
