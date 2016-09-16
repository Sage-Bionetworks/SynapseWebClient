package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRendererImpl implements EntityIdCellRenderer{

	EntityIdCellRendererView view;
	LazyLoadHelper lazyLoadHelper;
	SynapseClientAsync synapseClient;
	
	String entityId, entityName;
	@Inject
	public EntityIdCellRendererImpl(EntityIdCellRendererView view, 
			SynapseClientAsync synapseClient, 
			LazyLoadHelper lazyLoadHelper) {
		this.view = view;
		this.lazyLoadHelper = lazyLoadHelper;
		this.synapseClient = synapseClient;
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
			synapseClient.getEntity(entityId, new AsyncCallback<Entity>() {
				@Override
				public void onSuccess(Entity entity) {
					entityName = entity.getName();
					view.setIcon(EntityTypeUtils.getIconTypeForEntity(entity));
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
		this.entityId = value;
		entityName = null;
		lazyLoadHelper.setIsConfigured();
		view.setLinkHref(Synapse.getHrefForDotVersion(entityId));
	}

	@Override
	public String getValue() {
		return entityId;
	}

}
