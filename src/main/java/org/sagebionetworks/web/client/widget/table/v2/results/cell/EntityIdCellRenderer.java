package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.VersionedEntityHeaderAsyncHandler;
import org.sagebionetworks.web.shared.table.CellAddress;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRenderer implements Cell, TakesAddressCell {

	EntityIdCellRendererView view;
	EntityHeaderAsyncHandler entityHeaderAsyncHandler;
	VersionedEntityHeaderAsyncHandler versionedHeaderAsyncHandler;
	SynapseJSNIUtils jsniUtils;
	String entityId, entityName;
	Long entityVersion = null;
	ClickHandler customClickHandler;
	CellAddress address;
	boolean hideIfLoadError;

	@Inject
	public EntityIdCellRenderer(EntityIdCellRendererView view, 
			EntityHeaderAsyncHandler entityHeaderAsyncHandler,
			VersionedEntityHeaderAsyncHandler versionedHeaderAsyncHandler,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.entityHeaderAsyncHandler = entityHeaderAsyncHandler;
		this.versionedHeaderAsyncHandler = versionedHeaderAsyncHandler;
		this.jsniUtils = jsniUtils;
	}

	public void loadData() {
		if (entityName == null && entityId != null) {
			view.showLoadingIcon();
			String requestEntityId = entityId.toLowerCase().startsWith("syn") ? entityId : "syn" + entityId;
			if (address != null && address.getColumn().getName().equals("id")) {
				// This Entity type column is named "id". 
				// if the row matches the synapse ID, set the version based on the cell address row_version 
				if (requestEntityId.equals("syn" + address.getRowId())) {
					entityVersion = address.getRowVersion();	
				}
			}
			String dotVersionEntityId = entityVersion != null ? requestEntityId + "." + entityVersion : requestEntityId;
			view.setEntityId(dotVersionEntityId);
			if (customClickHandler != null) {
				view.setClickHandler(customClickHandler);
			}
			AsyncCallback<EntityHeader> callback = new AsyncCallback<EntityHeader>() {
				@Override
				public void onSuccess(EntityHeader entity) {
					entityName = entity.getName();
					view.setIcon(EntityTypeUtils.getIconTypeForEntityClassName(entity.getType()));
					view.setLinkText(entityName);
				}

				@Override
				public void onFailure(Throwable caught) {
					if (hideIfLoadError) {
						view.setVisible(false);
						jsniUtils.consoleError(caught.getMessage());
					} else {
						view.setLinkText(entityId);
						view.showErrorIcon(caught.getMessage());
					}
				}
			};
			if (entityVersion != null) {
				versionedHeaderAsyncHandler.getEntityHeader(requestEntityId, entityVersion, callback);	
			} else {
				entityHeaderAsyncHandler.getEntityHeader(requestEntityId, callback);	
			}
		}
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	public void setValue(String value, boolean hideIfLoadError) {
		setValue(value, null, hideIfLoadError);
	}

	@Override
	public void setValue(String value) {
		setValue(value, null, false);
	}

	public void setValue(String value, ClickHandler customClickHandler, boolean hideIfLoadError) {
		view.hideAllIcons();
		this.entityId = value;
		this.hideIfLoadError = hideIfLoadError;
		entityName = null;
		this.customClickHandler = customClickHandler;
		loadData();
	}

	@Override
	public String getValue() {
		return entityId;
	}


	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
	
	@Override
	public void setCellAddresss(CellAddress address) {
		this.address = address;
	}

}
