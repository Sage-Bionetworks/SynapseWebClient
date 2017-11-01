package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidget implements OpenUserInvitationsWidgetView.Presenter {
	public static final Integer INVITATION_BATCH_LIMIT = 10;

	private OpenUserInvitationsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
	private String teamId;
	private Callback teamRefreshCallback;
	private Integer currentOffset;
	private SynapseAlert synAlert;
	private GWTWrapper gwt;
	private DateTimeUtils dateTimeUtils;
	private PortalGinInjector ginInjector;

	@Inject
	public OpenUserInvitationsWidget(OpenUserInvitationsWidgetView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState,
			SynapseAlert synAlert,
			GWTWrapper gwt,
			DateTimeUtils dateTimeUtils,
			SynapseJavascriptClient jsClient,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.synAlert = synAlert;
		this.gwt = gwt;
		this.dateTimeUtils = dateTimeUtils;
		view.setPresenter(this);
		view.setSynAlert(synAlert);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.jsClient = jsClient;
		this.ginInjector = ginInjector;
	}
	
	public void clear() {
		view.clear();
	}

	@Override
	public void removeInvitation(String invitationId) {
		gwt.saveWindowPosition();
		synAlert.clear();
		jsClient.deleteMembershipInvitation(invitationId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				teamRefreshCallback.invoke();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public void configure(String teamId, Callback teamRefreshCallback) {
		this.teamId = teamId;
		this.teamRefreshCallback = teamRefreshCallback;
		currentOffset = 0;
		getNextBatch();
	}

	@Override
	public void getNextBatch() {
		view.hideMoreButton();
		// Show up to INVITATION_BATCH_LIMIT invitations extended by teamId
		synapseClient.getOpenTeamInvitations(teamId, INVITATION_BATCH_LIMIT, currentOffset, new AsyncCallback<ArrayList<OpenTeamInvitationBundle>>() {
			@Override
			public void onSuccess(ArrayList<OpenTeamInvitationBundle> result) {
				currentOffset += result.size();
				addInvitations(result);
				updateMoreButton(result.size());
				gwt.restoreWindowPosition();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					view.hideMoreButton();
				} else {
					synAlert.handleException(caught);
				} 
			}
		});
	}

	private void addInvitations(List<OpenTeamInvitationBundle> bundles) {
		// Add the invitations to the view
		for (OpenTeamInvitationBundle b : bundles) {
			MembershipInvtnSubmission mis = b.getMembershipInvtnSubmission();
			String createdOn = dateTimeUtils.convertDateToSmallString(b.getMembershipInvtnSubmission().getCreatedOn());
			if (b.getUserProfile() != null) {
				// Invitee is an existing user
				UserBadge userBadge = ginInjector.getUserBadgeWidget();
				userBadge.configure(b.getUserProfile());
				view.addInvitation(userBadge, mis.getInviteeEmail(), mis.getId(), mis.getMessage(), createdOn);
			} else if (mis.getInviteeEmail() != null) {
				// Invitee is an email address
				EmailInvitationBadge emailInvitationBadge = ginInjector.getEmailInvitationBadgeWidget();
				emailInvitationBadge.configure(mis.getInviteeEmail());
				view.addInvitation(emailInvitationBadge, mis.getId(), mis.getMessage(), createdOn);
			} else {
				synAlert.showError("Membership invitation with ID " + mis.getId() + " is not in a valid state.");
			}
		}
	}

	private void updateMoreButton(int resultSize) {
		if (resultSize == INVITATION_BATCH_LIMIT) {
			view.showMoreButton();
		} else if (resultSize < INVITATION_BATCH_LIMIT){
			view.hideMoreButton();
		} else {
			synAlert.showError("Result size can't be greater than " + INVITATION_BATCH_LIMIT);
		}
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setVisible(boolean visible) {
		view.asWidget().setVisible(visible);
	}
}
