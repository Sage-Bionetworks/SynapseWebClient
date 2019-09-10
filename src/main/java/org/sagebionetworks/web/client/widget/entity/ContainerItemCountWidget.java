package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ContainerItemCountWidget implements IsWidget {
	private ContainerItemCountWidgetView view;
	private SynapseJavascriptClient jsClient;
	private SynapseJSNIUtilsImpl utils;
	List<EntityType> entityTypes = new ArrayList<>();

	@Inject
	public ContainerItemCountWidget(ContainerItemCountWidgetView view, SynapseJavascriptClient jsClient, SynapseJSNIUtilsImpl utils) {
		this.view = view;
		this.jsClient = jsClient;
		entityTypes.add(EntityType.file);
		entityTypes.add(EntityType.folder);
		entityTypes.add(EntityType.link);
		// TODO: uncomment if tables are in the hierarchy
//		entityTypes.add(EntityType.table);
//		entityTypes.add(EntityType.entityview);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void configure(String entityId) {
		clear();
		EntityChildrenRequest request = new EntityChildrenRequest();
		request.setParentId(entityId);
		request.setIncludeSumFileSizes(false);
		request.setIncludeTotalChildCount(true);
		request.setIncludeTypes(entityTypes);
		jsClient.getEntityChildren(request, new AsyncCallback<EntityChildrenResponse>() {
			
			@Override
			public void onSuccess(EntityChildrenResponse result) {
				Long childCount = result.getTotalChildCount();
				if (childCount > 0) {
					view.showCount(childCount);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				utils.consoleError(caught);
			}
		});
	}

	public void clear() {
		view.hide();
		view.clear();
	}
}
