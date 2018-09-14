package org.sagebionetworks.web.client.widget.team;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.ValidationUtils.isValidEmail;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;

import com.google.gwt.event.dom.client.KeyCodes;
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
	private List<String> inviteEmails, inviteUsers;
	private String currentlyProcessingEmail, currentlyProcessingUser, invitationMessage;
	private AsyncCallback<Void> inviteCallback;
	@Inject
	public InviteWidget(InviteWidgetView view,
						SynapseClientAsync synapseClient,
						GWTWrapper gwt, SynapseAlert synAlert,
						SynapseSuggestBox peopleSuggestBox,
						UserGroupSuggestionProvider provider) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.gwt = gwt;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		peopleSuggestWidget.setSuggestionProvider(provider);
		peopleSuggestWidget.setTypeFilter(TypeFilter.USERS_ONLY);
		view.setSuggestWidget(peopleSuggestBox.asWidget());
		view.setSynAlertWidget(synAlert.asWidget());
		peopleSuggestWidget.addKeyDownHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				addSuggestion();
			}
		});
		peopleSuggestWidget.addItemSelectedHandler(suggestion -> {
			if (suggestion != null) {
				addSuggestion();
			}
		});
		inviteCallback = new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				if (currentlyProcessingEmail != null) {
					inviteEmails.remove(currentlyProcessingEmail);
				} else if (currentlyProcessingUser != null) {
					inviteUsers.remove(currentlyProcessingUser);
				}
				doSendInvites();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				refreshInvitees();
			}
		};
		view.setPresenter(this);
	}
	
	public void addSuggestion() {
		String input = peopleSuggestWidget.getText();
		if (isValidEmail(input)) {
			inviteEmails.add(input);
			view.addEmailToInvite(input);
			peopleSuggestWidget.clear();
		} else if (peopleSuggestWidget.getSelectedSuggestion() != null) {
			String userId = peopleSuggestWidget.getSelectedSuggestion().getId();
			inviteUsers.add(userId);
			view.addUserToInvite(userId);
			peopleSuggestWidget.clear();
		}
	}
	
	@Override
	public void configure(Team team) {
		clear();
		inviteEmails = new ArrayList<String>();
		inviteUsers = new ArrayList<String>();
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
	public void doSendInvites(String invitationMessage) {
		// if anything is in the invitation field, then pick it up before processing
		addSuggestion();
		view.setLoading(true);
		synAlert.clear();
		this.invitationMessage = invitationMessage;
		doSendInvites();
	}
	
	/**
	 * Recursively process the user invitations (emails, then Synapse users).
	 */
	public void doSendInvites() {
		currentlyProcessingUser = currentlyProcessingEmail = null;
		if (!inviteEmails.isEmpty()) {
			// kick off the next email invite
			currentlyProcessingEmail = inviteEmails.get(0);
			synapseClient.inviteNewMember(currentlyProcessingEmail, team.getId(), invitationMessage, gwt.getHostPageBaseURL(), inviteCallback);
		} else if (!inviteUsers.isEmpty()) {
			// kick off the next user invite
			currentlyProcessingUser = inviteUsers.get(0);
			synapseClient.inviteMember(currentlyProcessingUser, team.getId(), invitationMessage, gwt.getHostPageBaseURL(), inviteCallback);
		} else {
			// done!
			view.hide();
			view.showInfo("Invitation(s) Sent");
			teamUpdatedCallback.invoke();
			view.setLoading(false);
		}
	}
	
	@Override
	public void show() {
		clear();
		view.show();
	}
	
	@Override
	public void hide() {
		view.hide();
	}

	@Override
	public void removeEmailToInvite(String email) {
		inviteEmails.remove(email);
	}
	
	@Override
	public void removeUserToInvite(String userId) {
		inviteUsers.remove(userId);
	}
	
	public void refreshInvitees() {
		view.clear();
		view.setLoading(false);
		for (String email : inviteEmails) {
			view.addEmailToInvite(email);
		}
		for (String userId : inviteUsers) {
			view.addUserToInvite(userId);
		}
	}
}
