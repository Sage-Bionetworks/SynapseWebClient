package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRenderer implements Cell {

	EntityIdCellRendererView view;
	EntityHeaderAsyncHandler entityHeaderAsyncHandler;
	SynapseJSNIUtils jsniUtils;
	String entityId, entityName;
	ClickHandler customClickHandler;
	boolean hideIfLoadError;

	@Inject
	public EntityIdCellRenderer(EntityIdCellRendererView view, EntityHeaderAsyncHandler entityHeaderAsyncHandler, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.entityHeaderAsyncHandler = entityHeaderAsyncHandler;
		this.jsniUtils = jsniUtils;
	}

	public void loadData() {
		if (entityName == null && entityId != null) {
			view.showLoadingIcon();
			String requestEntityId = entityId.toLowerCase().startsWith("syn") ? entityId : "syn" + entityId;
			view.setEntityId(requestEntityId);
			if (customClickHandler != null) {
				view.setClickHandler(customClickHandler);
			}

			entityHeaderAsyncHandler.getEntityHeader(requestEntityId, new AsyncCallback<EntityHeader>() {
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
			});
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
}
