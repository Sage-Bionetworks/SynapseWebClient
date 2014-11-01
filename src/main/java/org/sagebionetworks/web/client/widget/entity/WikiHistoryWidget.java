package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;

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
	private Map<String, String> mapIdToName;

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
		this.mapIdToName = new HashMap<String, String>();
		view.configure(canEdit, actionHandler);
	}
	
	@Override
	public void configureNextPage(Long offset, Long limit) {
		synapseClient.getV2WikiHistory(wikiKey, limit, offset, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_HISTORY_WIDGET_FAILED+caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults<JSONEntity> paginatedHistory = nodeModelCreator.createPaginatedResults(result, V2WikiHistorySnapshot.class);
					List<JSONEntity> historyAsList = paginatedHistory.getResults();
					// Create paginated history into list of V2 History Snapshots
					List<V2WikiHistorySnapshot> historyAsListOfHeaders = new ArrayList<V2WikiHistorySnapshot>();
					for(int i = 0; i < historyAsList.size(); i++) {
						V2WikiHistorySnapshot snapshot = (V2WikiHistorySnapshot) historyAsList.get(i);
						historyAsListOfHeaders.add(snapshot);
					}
					// Update/append to history data structure
					view.updateHistoryList(historyAsListOfHeaders);
					
					// Prepare ids that are not mapped to a display name in the map
					final ArrayList<String> idsToSearch = new ArrayList<String>();
					for(int i = 0; i < historyAsListOfHeaders.size(); i++) {
						String modifiedById = historyAsListOfHeaders.get(i).getModifiedBy();
						// Only add unique ids to the list being built
						if(mapIdToName != null && !mapIdToName.containsKey(modifiedById) && !idsToSearch.contains(modifiedById)) {
							idsToSearch.add(modifiedById);
						}
					}
					// Call to get user headers from the list of needed ids
					synapseClient.getUserGroupHeadersById(idsToSearch, new AsyncCallback<UserGroupHeaderResponsePage>() {

						@Override
						public void onFailure(Throwable caught) {
							view.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_HISTORY_WIDGET_FAILED+caught.getMessage());
						}
						@Override
						public void onSuccess(UserGroupHeaderResponsePage response) {
							// Store display names along with the associated id in the map
							List<UserGroupHeader> headers = response.getChildren();
							for(int i = 0; i < headers.size(); i++) {
								mapIdToName.put(idsToSearch.get(i), DisplayUtils.getDisplayName(headers.get(i)));
							}
							// Now we're ready to build the history widget
							view.buildHistoryWidget();
						}
						
					});
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
				
			}
		});
	}
	
	@Override
	public String getNameForUserId(String userId) {
		return mapIdToName.get(userId);
	}
	
	@Override
	public void hideHistoryWidget() {
		view.hideHistoryWidget();
	}
	
	@Override
	public void showHistoryWidget() {
		view.showHistoryWidget();
	}
}
