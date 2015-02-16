package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialog;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.shared.ChallengeTeamBundle;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeTeamsWidget implements ChallengeTeamsView.Presenter, WidgetRendererPresenter, PageChangeListener {
	
	private ChallengeTeamsView view;
	private Map<String,String> descriptor;
	private EditRegisteredTeamDialog dialog;
	private ChallengeClientAsync challengeClient;
	private String challengeId;
	private Callback widgetRefreshRequired;
	private DetailedPaginationWidget paginationWidget;
	public static final Long DEFAULT_TEAM_LIMIT = 50L;
	public static final Long DEFAULT_OFFSET = 0L;
	private AuthenticationController authController;
	@Inject
	public ChallengeTeamsWidget(ChallengeTeamsView view, 
			EditRegisteredTeamDialog dialog, 
			DetailedPaginationWidget paginationWidget, 
			ChallengeClientAsync challengeClient,
			AuthenticationController authController) {
		this.view = view;
		this.dialog = dialog;
		this.paginationWidget = paginationWidget;
		this.challengeClient = challengeClient;
		this.authController = authController;
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
		challengeClient.getChallengeTeams(authController.getCurrentUserPrincipalId(), challengeId,DEFAULT_TEAM_LIMIT.intValue(), newOffset.intValue(), new AsyncCallback<ChallengeTeamPagedResults>() {
			@Override
			public void onSuccess(ChallengeTeamPagedResults results) {
				view.hideLoading();
				if (results.getTotalNumberOfResults() > 0) {
					//configure the pager, and the challenge list
					paginationWidget.configure(DEFAULT_TEAM_LIMIT, newOffset, results.getTotalNumberOfResults(), ChallengeTeamsWidget.this);
					for (ChallengeTeamBundle challenge : results.getResults()) {
						view.addChallengeTeam(challenge.getChallengeTeam().getTeamId(), 
							DisplayUtils.replaceWithEmptyStringIfNull(challenge.getChallengeTeam().getMessage()), 
							challenge.isAdmin());
					}
				} else {
					view.showNoTeams();
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
