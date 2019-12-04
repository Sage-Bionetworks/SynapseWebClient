package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiHistoryWidget implements WikiHistoryWidgetView.Presenter, SynapseWidgetPresenter, IsWidget {
	public static final String NO_HISTORY_IS_FOUND_FOR_A_WIKI = "No history is found for a wiki";
	private GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	private WikiHistoryWidgetView view;
	private SynapseClientAsync synapseClient;
	private WikiPageKey wikiKey;
	private Map<String, String> mapIdToName;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synAlert;

	public interface ActionHandler {
		public void previewClicked(Long versionToPreview, Long currentVersion);

		public void restoreClicked(Long versionToRestore);
	}

	@Inject
	public WikiHistoryWidget(GlobalApplicationState globalApplicationState, WikiHistoryWidgetView view, SynapseClientAsync synapseClient, AuthenticationController authenticationController, SynapseJavascriptClient jsClient, SynapseAlert synAlert) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
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

	public void clear() {
		view.clear();
	}

	@Override
	public void configureNextPage(final Long offset, final Long limit) {
		synAlert.clear();
		synapseClient.getV2WikiHistory(wikiKey, limit, offset, new AsyncCallback<PaginatedResults<V2WikiHistorySnapshot>>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught.getMessage() != null && caught.getMessage().contains(NO_HISTORY_IS_FOUND_FOR_A_WIKI)) {
					// exception should be something more reasonable (like a 404).
					view.hideLoadMoreButton();
				} else {
					synAlert.handleException(caught);
				}
			}

			@Override
			public void onSuccess(PaginatedResults<V2WikiHistorySnapshot> result) {
				PaginatedResults<V2WikiHistorySnapshot> paginatedHistory = result;
				// paginatedHistory.getTotalNumberOfResults() should return total!
				List<V2WikiHistorySnapshot> historyAsListOfHeaders = paginatedHistory.getResults();
				if (historyAsListOfHeaders == null || historyAsListOfHeaders.isEmpty()) {
					view.hideLoadMoreButton();
				} else {
					// Update/append to history data structure
					view.updateHistoryList(historyAsListOfHeaders);
					// Prepare ids that are not mapped to a display name in the map
					final ArrayList<String> idsToSearch = new ArrayList<String>();
					for (int i = 0; i < historyAsListOfHeaders.size(); i++) {
						String modifiedById = historyAsListOfHeaders.get(i).getModifiedBy();
						// Only add unique ids to the list being built
						if (mapIdToName != null && !idsToSearch.contains(modifiedById) && !modifiedById.trim().isEmpty()) {
							idsToSearch.add(modifiedById);
						}
					}
					// Call to get user headers from the list of needed ids
					synAlert.clear();
					jsClient.getUserGroupHeadersById(idsToSearch, new AsyncCallback<UserGroupHeaderResponsePage>() {

						@Override
						public void onFailure(Throwable caught) {
							synAlert.handleException(caught);
						}

						@Override
						public void onSuccess(UserGroupHeaderResponsePage response) {
							// Store display names along with the associated id in the map
							List<UserGroupHeader> headers = response.getChildren();
							for (int i = 0; i < headers.size(); i++) {
								mapIdToName.put(idsToSearch.get(i), DisplayUtils.getDisplayName(headers.get(i)));
							}
							// Now we're ready to build the history widget
							view.buildHistoryWidget();
						}

					});
				}
			}
		});
	}

	@Override
	public String getNameForUserId(String userId) {
		return mapIdToName.get(userId);
	}

	public void hideHistoryWidget() {
		view.hideHistoryWidget();
	}

	public void showHistoryWidget() {
		view.showHistoryWidget();
	}
}
