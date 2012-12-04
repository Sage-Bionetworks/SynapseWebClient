package org.sagebionetworks.web.client.events;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.event.shared.GwtEvent;

public class WidgetDescriptorUpdatedEvent extends GwtEvent<WidgetDescriptorUpdatedHandler> {

	private static final Type<WidgetDescriptorUpdatedHandler> TYPE = new Type<WidgetDescriptorUpdatedHandler>();
	private WidgetDescriptor widgetDescriptor;
	private String name;
	private EntityWrapper entityWrapper;
	
	public WidgetDescriptorUpdatedEvent() {
		
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

	public WidgetDescriptor getWidgetDescriptor() {
		return widgetDescriptor;
	}

	public void setWidgetDescriptor(WidgetDescriptor widgetDescriptor) {
		this.widgetDescriptor = widgetDescriptor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public EntityWrapper getEntityWrapper() {
		return entityWrapper;
	}
	public void setEntityWrapper(EntityWrapper entityWrapper) {
		this.entityWrapper = entityWrapper;
	}
}
