package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeParticipantsWidget implements ChallengeParticipantsView.Presenter, WidgetRendererPresenter, PageChangeListener {
	
	private ChallengeParticipantsView view;
	private Map<String,String> descriptor;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private String challengeId;
	private boolean isInTeam;
	private Callback widgetRefreshRequired;
	private DetailedPaginationWidget paginationWidget;
	public static final Long DEFAULT_PARTICIPANT_LIMIT = 50L;
	public static final Long DEFAULT_OFFSET = 0L;
	
	@Inject
	public ChallengeParticipantsWidget(ChallengeParticipantsView view, 
			DetailedPaginationWidget paginationWidget, 
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.paginationWidget = paginationWidget;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPaginationWidget(paginationWidget.asWidget());
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		this.widgetRefreshRequired = widgetRefreshRequired;
		challengeId = descriptor.get(WidgetConstants.CHALLENGE_ID_KEY);
		isInTeam = false;
		String isInTeamString = descriptor.get(WidgetConstants.IS_IN_CHALLENGE_TEAM_KEY);
		if (isInTeamString != null) {
			isInTeam = Boolean.parseBoolean(isInTeamString);
		}
		descriptor = widgetDescriptor;
		if (challengeId == null) {
			view.showErrorMessage(WidgetConstants.CHALLENGE_ID_KEY + " is required.");
		} else {
			//get the challenge team summaries
			onPageChange(DEFAULT_OFFSET);
		}
	}
	
	@Override
	public void onPageChange(final Long newOffset) {
		view.hideErrors();
		view.showLoading();
		view.clearParticipants();
		synapseClient.getChallengeParticipants(isInTeam, challengeId, DEFAULT_PARTICIPANT_LIMIT, newOffset, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					view.hideLoading();
					PaginatedResults<UserGroupHeader> users = nodeModelCreator.createPaginatedResults(result, UserGroupHeader.class);
					if (users.getTotalNumberOfResults() > 0) {
						//configure the pager, and the participant list
						paginationWidget.configure(DEFAULT_PARTICIPANT_LIMIT, newOffset, users.getTotalNumberOfResults(), ChallengeParticipantsWidget.this);
						for (UserGroupHeader header : users.getResults()) {
							view.addParticipant(header.getOwnerId());
						}
					} 
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}

				
			}
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
