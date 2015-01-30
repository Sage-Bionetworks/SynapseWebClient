package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialog;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeTeamsWidget implements ChallengeTeamsView.Presenter, WidgetRendererPresenter, PageChangeListener {
	
	private ChallengeTeamsView view;
	private Map<String,String> descriptor;
	private EditRegisteredTeamDialog dialog;
	private AuthenticationController authController;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private String challengeId;
	private Callback widgetRefreshRequired;
	private DetailedPaginationWidget paginationWidget;
	public static final Long DEFAULT_TEAM_LIMIT = 50L;
	public static final Long DEFAULT_OFFSET = 0L;
	
	@Inject
	public ChallengeTeamsWidget(ChallengeTeamsView view, 
			EditRegisteredTeamDialog dialog, 
			DetailedPaginationWidget paginationWidget, 
			AuthenticationController authController,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.dialog = dialog;
		this.paginationWidget = paginationWidget;
		this.authController = authController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPaginationWidget(paginationWidget.asWidget());
		view.setEditRegisteredTeamDialog(dialog.asWidget());
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		this.widgetRefreshRequired = widgetRefreshRequired;
		challengeId = descriptor.get(WidgetConstants.CHALLENGE_ID_KEY);
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
		view.clearTeams();
		synapseClient.getChallengeTeamSummaries(challengeId, authController.getCurrentUserPrincipalId(),DEFAULT_TEAM_LIMIT, newOffset, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					view.hideLoading();
					PaginatedResults<ChallengeTeamSummary> challenges = nodeModelCreator.createPaginatedResults(result, ChallengeTeamSummary.class);
					if (challenges.getTotalNumberOfResults() > 0) {
						//configure the pager, and the challenge list
						paginationWidget.configure(DEFAULT_TEAM_LIMIT, newOffset, challenges.getTotalNumberOfResults(), ChallengeTeamsWidget.this);
						for (ChallengeTeamSummary challenge : challenges.getResults()) {
							view.addChallengeTeam(challenge.getTeamId(), 
								DisplayUtils.replaceWithEmptyStringIfNull(challenge.getMessage()), 
								challenge.getCanUserEdit());
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
	public void onEdit(String teamId, String message) {
		dialog.showChallengeTeamEditor(challengeId, message, teamId, widgetRefreshRequired);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
