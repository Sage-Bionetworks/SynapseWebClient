package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.ArrayList;
import java.util.Collections;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

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
			synapseClient.getEntityHeaderBatch(Collections.singletonList(entityId), new AsyncCallback<ArrayList<EntityHeader>>() {
				@Override
				public void onSuccess(ArrayList<EntityHeader> results) {
					if (results.size() == 1) {
						EntityHeader entity = results.get(0);
						entityName = entity.getName();
						view.setIcon(EntityTypeUtils.getIconTypeForEntityClassName(entity.getType()));
						view.setLinkText(entityName);
					} else {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_LOADING));
					}
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
