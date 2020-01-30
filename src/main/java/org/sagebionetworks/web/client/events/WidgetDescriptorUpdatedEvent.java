package org.sagebionetworks.web.client.events;

import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import com.google.gwt.event.shared.GwtEvent;

public class WidgetDescriptorUpdatedEvent extends GwtEvent<WidgetDescriptorUpdatedHandler> {

	private static final Type<WidgetDescriptorUpdatedHandler> TYPE = new Type<WidgetDescriptorUpdatedHandler>();
	private String name, oldName;
	private Entity entityWrapper;
	private boolean isDeleted;
	private List<String> newFileHandleIds, deletedFileHandleIds;

	// some entities might want to simply insert some constant text into the description instead of
	// updating the attachments (external image will do this)
	private String insertValue;

	public WidgetDescriptorUpdatedEvent() {
		isDeleted = false;
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
	 * This holds what the attachment name used to be (support the case when the attachment has been
	 * renamed)
	 * 
	 * @param oldName
	 */
	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getOldName() {
		return oldName;
	}


	public Entity getEntityWrapper() {
		return entityWrapper;
	}

	public void setEntityWrapper(Entity entityWrapper) {
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

	public List<String> getNewFileHandleIds() {
		return newFileHandleIds;
	}

	public void setNewFileHandleIds(List<String> newFileHandleIds) {
		this.newFileHandleIds = newFileHandleIds;
	}

	public List<String> getDeletedFileHandleIds() {
		return deletedFileHandleIds;
	}

	public void setDeletedFileHandleIds(List<String> deletedFileHandleIds) {
		this.deletedFileHandleIds = deletedFileHandleIds;
	}
}
