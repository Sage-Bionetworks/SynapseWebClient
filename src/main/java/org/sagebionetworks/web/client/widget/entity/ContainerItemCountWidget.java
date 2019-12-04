package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityType.entityview;
import static org.sagebionetworks.repo.model.EntityType.file;
import static org.sagebionetworks.repo.model.EntityType.folder;
import static org.sagebionetworks.repo.model.EntityType.link;
import static org.sagebionetworks.repo.model.EntityType.table;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ContainerItemCountWidget implements IsWidget {
	private ContainerItemCountWidgetView view;
	private SynapseJavascriptClient jsClient;
	private SynapseJSNIUtils utils;
	List<EntityType> entityTypes = new ArrayList<>();

	@Inject
	public ContainerItemCountWidget(ContainerItemCountWidgetView view, SynapseJavascriptClient jsClient, SynapseJSNIUtils utils) {
		this.view = view;
		this.jsClient = jsClient;
		this.utils = utils;
		entityTypes.add(file);
		entityTypes.add(folder);
		entityTypes.add(link);
		// include Tables, even though it's not currently possible to move them to a subfolder.
		entityTypes.add(table);
		entityTypes.add(entityview);
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
