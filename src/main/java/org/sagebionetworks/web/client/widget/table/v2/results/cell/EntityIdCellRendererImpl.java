package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRendererImpl implements EntityIdCellRenderer{

	EntityIdCellRendererView view;
	LazyLoadHelper lazyLoadHelper;
	EntityHeaderAsyncHandler entityHeaderAsyncHandler;
	String entityId, entityName;
	@Inject
	public EntityIdCellRendererImpl(EntityIdCellRendererView view, 
			LazyLoadHelper lazyLoadHelper,
			EntityHeaderAsyncHandler entityHeaderAsyncHandler) {
		this.view = view;
		this.lazyLoadHelper = lazyLoadHelper;
		this.entityHeaderAsyncHandler = entityHeaderAsyncHandler;
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				loadData();
			}
		};
		lazyLoadHelper.configure(loadDataCallback, view);
	}

	public void loadData() {
		if (entityName == null && entityId != null) {
			view.showLoadingIcon();
			String requestEntityId = entityId.toLowerCase().startsWith("syn") ? entityId : "syn"+entityId;
			view.setLinkHref(Synapse.getHrefForDotVersion(requestEntityId));
			entityHeaderAsyncHandler.getEntityHeader(requestEntityId, new AsyncCallback<EntityHeader>() {
				@Override
				public void onSuccess(EntityHeader entity) {
					entityName = entity.getName();
					view.setIcon(EntityTypeUtils.getIconTypeForEntityClassName(entity.getType()));
					view.setLinkText(entityName);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorIcon(caught.getMessage());
					view.setLinkText(entityId);
				}
			});
		}
	}
	
	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}
	
	@Override
	public void setValue(String value) {
		view.hideAllIcons();
		this.entityId = value;
		entityName = null;
		lazyLoadHelper.setIsConfigured();
	}

	@Override
	public String getValue() {
		return entityId;
	}

}
