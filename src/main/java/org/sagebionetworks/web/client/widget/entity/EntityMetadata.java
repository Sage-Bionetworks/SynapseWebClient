package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityTypeProvider entityTypeProvider;
	private JiraURLHelper jiraURLHelper;
	private EventBus bus;

	@Inject
	public EntityMetadata(EntityMetadataView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,
			EntityTypeProvider entityTypeProvider, JiraURLHelper jiraURLHelper,
			EventBus bus) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityTypeProvider = entityTypeProvider;
		this.jiraURLHelper = jiraURLHelper;
		this.bus = bus;
	}

	@Override
	public void loadVersions(String id, int offset, int limit,
			final AsyncCallback<PaginatedResults<VersionInfo>> asyncCallback) {
		// TODO: If we ever change the offset api to actually take 0 as a valid
		// offset, then we need to remove "+1"
		synapseClient.getEntityVersions(id, offset + 1, limit,
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						PaginatedResults<VersionInfo> paginatedResults = nodeModelCreator
								.createPaginatedResults(result,
										VersionInfo.class);
						asyncCallback.onSuccess(paginatedResults);
					}

					@Override
					public void onFailure(Throwable caught) {
						asyncCallback.onFailure(caught);
					}
				});
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityBundle(EntityBundle bundle, boolean readOnly) {
		view.setEntityBundle(bundle);
		view.setReadOnly(bundle.getPermissions().getCanEdit() && readOnly);
	}

}
