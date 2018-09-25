package org.sagebionetworks.web.client.widget.team;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget.DELETED_INVITATION_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
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
	public static final String RESENT_INVITATION = "Invitation resent";
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
	private PopupUtilsView popupUtils;

	@Inject
	public OpenUserInvitationsWidget(OpenUserInvitationsWidgetView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState,
			SynapseAlert synAlert,
			GWTWrapper gwt,
			DateTimeUtils dateTimeUtils,
			SynapseJavascriptClient jsClient,
			PopupUtilsView popupUtils,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.synAlert = synAlert;
		this.gwt = gwt;
		this.dateTimeUtils = dateTimeUtils;
		view.setPresenter(this);
		view.setSynAlert(synAlert);
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalApplicationState = globalApplicationState;
		this.jsClient = jsClient;
		this.popupUtils = popupUtils;
		this.ginInjector = ginInjector;
	}
	
	public void clear() {
		view.clear();
	}

	public void refresh() {
		configure(teamId, teamRefreshCallback);
	}
	
	@Override
	public void removeInvitation(String invitationId) {
		gwt.saveWindowPosition();
		synAlert.clear();
		jsClient.deleteMembershipInvitation(invitationId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				popupUtils.showInfo(DELETED_INVITATION_MESSAGE);
				refresh();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				refresh();
			}
		});
	}
	
	public void configure(String teamId, Callback teamRefreshCallback) {
		this.teamId = teamId;
		this.teamRefreshCallback = teamRefreshCallback;
		currentOffset = 0;
		view.clear();
		getNextBatch();
	}

	@Override
	public void getNextBatch() {
		view.hideMoreButton();
		// Show up to INVITATION_BATCH_LIMIT invitations extended by teamId
		synapseClient.getOpenTeamInvitations(teamId, INVITATION_BATCH_LIMIT, currentOffset, new AsyncCallback<ArrayList<OpenTeamInvitationBundle>>() {
			@Override
			public void onSuccess(ArrayList<OpenTeamInvitationBundle> result) {
				boolean isEmptyResultset = currentOffset == 0 && result.isEmpty();
				view.setVisible(!isEmptyResultset);
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
			MembershipInvitation mis = b.getMembershipInvitation();
			String createdOn = dateTimeUtils.getDateTimeString(b.getMembershipInvitation().getCreatedOn());
			if (b.getUserProfile() != null) {
				// Invitee is an existing user
				UserBadge userBadge = ginInjector.getUserBadgeWidget();
				userBadge.configure(b.getUserProfile());
				view.addInvitation(userBadge, mis.getInviteeEmail(), mis.getId(), mis.getMessage(), createdOn);
			} else if (mis.getInviteeEmail() != null) {
				// Invitee is an email address
				EmailInvitationBadge emailInvitationBadge = ginInjector.getEmailInvitationBadgeWidget();
				emailInvitationBadge.configure(mis.getInviteeEmail());
				view.addInvitation(emailInvitationBadge, null, mis.getId(), mis.getMessage(), createdOn);
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
	
	@Override
	public void resendInvitation(String membershipInvitationId) {
		gwt.saveWindowPosition();
		synapseClient.resendTeamInvitation(membershipInvitationId, gwt.getHostPageBaseURL(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				popupUtils.showInfo(RESENT_INVITATION);
				refresh();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				refresh();
			}
		});
	}

}
