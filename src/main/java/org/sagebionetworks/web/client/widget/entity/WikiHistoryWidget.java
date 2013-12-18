package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiHistoryWidget implements WikiHistoryWidgetView.Presenter,
	SynapseWidgetPresenter{
	private GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	private WikiHistoryWidgetView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private WikiPageKey wikiKey;

	public interface ActionHandler{
		public void previewClicked(Long versionToPreview, Long currentVersion);
		public void restoreClicked(Long versionToRestore);
	}
	
	@Inject
	public WikiHistoryWidget(GlobalApplicationState globalApplicationState, WikiHistoryWidgetView view, 
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, AuthenticationController authenticationController) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {	
		return view.asWidget();
	}
	
	@Override
	public void configure(final WikiPageKey key, final boolean canEdit, final ActionHandler actionHandler) {
		this.wikiKey = key;
		synapseClient.getV2WikiHistory(wikiKey, new Long(100), new Long(0), new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_HISTORY_WIDGET_FAILED+caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults<JSONEntity> paginatedHistory = nodeModelCreator.createPaginatedResults(result, V2WikiHistorySnapshot.class);
					view.configure(canEdit, paginatedHistory.getResults(), actionHandler);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
				
			}
		});
	}
	
	@Override
	public void removeHistoryWidget() {
		view.removeHistoryWidget();
	}
}
