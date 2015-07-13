package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracleImpl;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracleImpl.UserGroupSuggestion;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class InviteWidget implements InviteWidgetView.Presenter {
	private InviteWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private Team team;
	private Callback teamUpdatedCallback;
	private GWTWrapper gwt;
	private SynapseAlert synAlert;
	private UserGroupSuggestBox peopleSuggestWidget;
	private SynapseJSNIUtils synapseJSNIUtils;
	private UserGroupSuggestOracleImpl oracle;
	
	@Inject
	public InviteWidget(InviteWidgetView view, 
			SynapseClientAsync synapseClient, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			GWTWrapper gwt, SynapseAlert synAlert,
			UserGroupSuggestBox peopleSuggestBox,
			SynapseJSNIUtils synapseJSNIUtils, UserGroupSuggestOracleImpl oracle) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.gwt = gwt;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.oracle = oracle;
		peopleSuggestWidget.setOracle(oracle);
		view.setSuggestWidget(peopleSuggestBox.asWidget());
		view.setSynAlertWidget(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	@Override
	public void configure(Team team) {
		clear();
		this.team = team;
		peopleSuggestWidget.configureURLs(synapseJSNIUtils.getBaseFileHandleUrl(), synapseJSNIUtils.getBaseProfileAttachmentUrl());
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
	public void sendInvite(String invitationMessage) {
		UserGroupSuggestion suggestion = peopleSuggestWidget.getSelectedSuggestion();
		if(suggestion != null) {
			UserGroupHeader header = suggestion.getHeader();
			String principalId = header.getOwnerId();
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
		else {
			synAlert.showError("Please select a user to send an invite to.");
		}
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
