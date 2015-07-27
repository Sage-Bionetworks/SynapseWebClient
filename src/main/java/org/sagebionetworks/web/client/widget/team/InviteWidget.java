package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class InviteWidget implements InviteWidgetView.Presenter {
	private InviteWidgetView view;
	private SynapseClientAsync synapseClient;
	private Team team;
	private Callback teamUpdatedCallback;
	private GWTWrapper gwt;
	private SynapseAlert synAlert;
	private SynapseSuggestBox peopleSuggestWidget;
	
	@Inject
	public InviteWidget(InviteWidgetView view, 
			SynapseClientAsync synapseClient, 
			GWTWrapper gwt, SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		peopleSuggestWidget.setSuggestionProvider(provider);
		view.setSuggestWidget(peopleSuggestBox.asWidget());
		view.setSynAlertWidget(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	@Override
	public void configure(Team team) {
		clear();
		this.team = team;
		peopleSuggestWidget.setPlaceholderText("Enter a user name...");
	}
	
	public void clear() {
		view.clear();	
		peopleSuggestWidget.clear();
		synAlert.clear();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setRefreshCallback(Callback teamUpdatedCallback) {
		this.teamUpdatedCallback = teamUpdatedCallback;
	}
	
	@Override
	public void validateAndSendInvite(final String invitationMessage) {
		UserGroupSuggestion suggestion = (UserGroupSuggestion)peopleSuggestWidget.getSelectedSuggestion();
		if(suggestion != null) {
			UserGroupHeader header = suggestion.getHeader();
			synapseClient.isTeamMember(header.getOwnerId(), Long.valueOf(team.getId()), new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
				@Override
				public void onSuccess(Boolean result) {
					if (!result) {
						doSendInvite(invitationMessage);
					} else {
						synAlert.showError("This user is already a member.");
					}
				}
			});
		} else {
			synAlert.showError("Please select a user to send an invite to.");
			
		}
	}
	
	public void doSendInvite(String invitationMessage) {
		UserGroupHeader header = ((UserGroupSuggestion)peopleSuggestWidget.getSelectedSuggestion()).getHeader();
		final String principalId = header.getOwnerId();
		final String firstName = header.getFirstName();
		final String lastName = header.getLastName();
		final String userName = header.getUserName();
		synapseClient.inviteMember(principalId, team.getId(), invitationMessage, gwt.getHostPageBaseURL(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.hide();
				view.showInfo("Invitation Sent", "An invitation has been sent to " + DisplayUtils.getDisplayName(firstName, lastName, userName));
				teamUpdatedCallback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public void show() {
		clear();
		view.clear();
		view.show();
	}
	
	@Override
	public void hide() {
		view.hide();
	}

}
