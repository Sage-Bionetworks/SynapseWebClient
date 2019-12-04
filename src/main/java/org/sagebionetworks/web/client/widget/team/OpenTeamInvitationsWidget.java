package org.sagebionetworks.web.client.widget.team;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenTeamInvitationsWidget implements OpenTeamInvitationsWidgetView.Presenter, IsWidget {
	public static final String DELETED_INVITATION_MESSAGE = "Invitation removed";
	public static final String RECEIVED = "received ";
	private OpenTeamInvitationsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private PortalGinInjector ginInjector;
	private Callback teamUpdatedCallback, refreshCallback;
	private SynapseAlert synAlert;
	DateTimeUtils dateTimeUtils;
	private SynapseJavascriptClient jsClient;
	private PopupUtilsView popupUtils;

	@Inject
	public OpenTeamInvitationsWidget(OpenTeamInvitationsWidgetView view, SynapseClientAsync synapseClient, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, PortalGinInjector ginInjector, SynapseAlert synAlert, DateTimeUtils dateTimeUtils, PopupUtilsView popupUtils, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setSynAlert(synAlert);
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.ginInjector = ginInjector;
		this.dateTimeUtils = dateTimeUtils;
		this.popupUtils = popupUtils;
		this.jsClient = jsClient;
		this.refreshCallback = new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		};
	}

	public void configure(final Callback teamUpdatedCallback, final CallbackP<List<OpenUserInvitationBundle>> openTeamInvitationsCallback) {
		this.teamUpdatedCallback = teamUpdatedCallback;
		view.clear();
		synAlert.clear();
		// using the current user, ask for all of the open invitations extended to this user.
		if (authenticationController.isLoggedIn()) {
			// get the open invitations
			synapseClient.getOpenInvitations(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<ArrayList<OpenUserInvitationBundle>>() {
				@Override
				public void onSuccess(ArrayList<OpenUserInvitationBundle> result) {
					if (openTeamInvitationsCallback != null)
						openTeamInvitationsCallback.invoke(result);
					showTeamInvites(result);
				}

				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		}
	};

	/**
	 * Update the view to display the given team invitations, with a Join button for each team.
	 * 
	 * @param invites
	 */
	public void showTeamInvites(List<OpenUserInvitationBundle> invites) {
		// create the associated object list, and pass to the view to render
		view.clear();
		for (OpenUserInvitationBundle b : invites) {
			String invitationMessage = "";
			MembershipInvitation invite = b.getMembershipInvitation();
			if (invite.getMessage() != null) {
				invitationMessage = invite.getMessage();
			}
			String createdOnString = "";
			if (invite.getCreatedOn() != null) {
				createdOnString = RECEIVED + dateTimeUtils.getRelativeTime(invite.getCreatedOn());
			}
			String invitationId = invite.getId();
			JoinTeamWidget joinButton = ginInjector.getJoinTeamWidget();
			joinButton.setButtonSize(ButtonSize.DEFAULT);
			joinButton.configure(b.getTeam().getId(), refreshCallback);
			view.addTeamInvite(b.getTeam(), invitationMessage, createdOnString, invitationId, joinButton.asWidget());
		}
	}

	@Override
	public void deleteInvitation(String inviteId) {
		synAlert.clear();
		jsClient.deleteMembershipInvitation(inviteId).addCallback(new FutureCallback<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				popupUtils.showInfo(DELETED_INVITATION_MESSAGE);
				refresh();
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		}, directExecutor());
	}

	public void refresh() {
		if (teamUpdatedCallback != null) {
			teamUpdatedCallback.invoke();
		}
		configure(teamUpdatedCallback, (CallbackP<List<OpenUserInvitationBundle>>) null);
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	public void clear() {
		view.clear();
	}

	public Widget asWidget() {
		return view.asWidget();
	}



}
