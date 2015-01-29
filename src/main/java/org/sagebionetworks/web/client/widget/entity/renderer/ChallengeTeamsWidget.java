package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialog;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeTeamsWidget implements ChallengeTeamsView.Presenter, WidgetRendererPresenter {
	
	private ChallengeTeamsView view;
	private Map<String,String> descriptor;
	private EditRegisteredTeamDialog dialog;
	private AuthenticationController authController;
	private SynapseClientAsync synapseClient;
	private String challengeId;
	private Callback widgetRefreshRequired;
	private DetailedPaginationWidget paginationWidget;
	
	@Inject
	public ChallengeTeamsWidget(ChallengeTeamsView view, 
			EditRegisteredTeamDialog dialog, 
			DetailedPaginationWidget paginationWidget, 
			AuthenticationController authController,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.dialog = dialog;
		this.paginationWidget = paginationWidget;
		this.authController = authController;
		this.synapseClient = synapseClient;
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
			getChallengeTeamSummaries();
		}
	}
	
	public void getChallengeTeamSummaries() {
		view.hideErrors();
		view.showLoading();
		synapseClient.get
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
