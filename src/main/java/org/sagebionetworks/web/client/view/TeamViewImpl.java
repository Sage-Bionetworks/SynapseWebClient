package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.Linkify;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.InviteWidget;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamViewImpl extends Composite implements TeamView {

	public interface TeamViewImplUiBinder extends UiBinder<Widget, TeamViewImpl> {}
	@UiField
	HTMLPanel mainContainer;
	@UiField
	Column commandsContainer;
	@UiField
	Div teamBadgeContainer;
	@UiField
	SimplePanel inviteMemberPanel;
	@UiField
	SimplePanel teamEditPanel;
	@UiField
	SimplePanel teamLeavePanel;
	@UiField
	SimplePanel teamDeletePanel;
	@UiField
	SimplePanel synAlertPanel;
	@UiField
	SimplePanel joinTeamPanel;
	@UiField
	SimplePanel openMembershipRequestsPanel;
	@UiField
	SimplePanel openUserInvitationsPanel;
	@UiField
	SimplePanel memberListPanel;
	@UiField
	Div memberCountContainer;
	@UiField
	Span publicJoinField;
	@UiField
	AnchorListItem leaveTeamItem;
	@UiField
	AnchorListItem editTeamItem;
	@UiField
	AnchorListItem deleteTeamItem;
	@UiField
	AnchorListItem inviteMemberItem;
	@UiField
	AnchorListItem manageAccessItem;
	@UiField
	TextBoxWithCopyToClipboardWidget synapseEmailField;
	@UiField
	Div mapPanel;
	@UiField
	Modal mapModal;
	@UiField
	Anchor showMapLink;
	@UiField
	org.gwtbootstrap3.client.ui.TextBox memberSearchTextBox;
	@UiField
	Button memberSearchButton;
	private Presenter presenter;
	private Header headerWidget;
	private GWTWrapper gwt;
	private BigTeamBadge bigTeamBadge;
	
	@Inject
	public TeamViewImpl(TeamViewImplUiBinder binder, 
			InviteWidget inviteWidget, 
			Header headerWidget, 
			GWTWrapper gwt,
			BigTeamBadge bigTeamBadge) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.gwt = gwt;
		this.bigTeamBadge = bigTeamBadge;
		setDropdownHandlers();
		headerWidget.configure();
		teamBadgeContainer.clear();
		teamBadgeContainer.add(bigTeamBadge.asWidget());
		showMapLink.addClickHandler(event -> {
			presenter.onShowMap();
		});
		memberSearchButton.addClickHandler(event -> {
			presenter.onMemberSearch(memberSearchTextBox.getValue());
		});
		memberSearchTextBox.addKeyDownHandler(event -> {
			if(KeyCodes.KEY_ENTER == event.getNativeKeyCode()){
				memberSearchButton.click();
			}
		});
	}
	
	@Override
	public void showMapModal() {
		mapModal.show();
	}
	
	private void setDropdownHandlers() {
		inviteMemberItem.addClickHandler(event ->  {
			gwt.scheduleDeferred(() -> {
				presenter.showInviteModal();		
			});
		});
		editTeamItem.addClickHandler(event -> {
			gwt.scheduleDeferred(() -> {
				presenter.showEditModal();		
			});
		});
		deleteTeamItem.addClickHandler(event -> {
			gwt.scheduleDeferred(() -> {
				presenter.showDeleteModal();		
			});
		});
		leaveTeamItem.addClickHandler(event -> {
			gwt.scheduleDeferred(() -> {
				presenter.showLeaveModal();		
			});
		});
		manageAccessItem.addClickHandler(event -> {
			gwt.scheduleDeferred(() -> {
				presenter.onManageAccess();		
			});
		});
	}
	
	@Override
	public void clear() {
		commandsContainer.setVisible(false);
		inviteMemberItem.setVisible(false);
		editTeamItem.setVisible(false);
		deleteTeamItem.setVisible(false);
		leaveTeamItem.setVisible(false);
		publicJoinField.setVisible(false);
		synapseEmailField.setText("");
		memberSearchTextBox.setValue("");
	}
	
	@Override
	public void showLoading() {
		clear();
		mainContainer.add(DisplayUtils.getLoadingWidget());
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void showMemberMenuItems() {
		leaveTeamItem.setVisible(true);
	}
	
	@Override
	public void showAdminMenuItems() {
		deleteTeamItem.setVisible(true);
		editTeamItem.setVisible(true);
		inviteMemberItem.setVisible(true);
	}
	@Override
	public void setCommandsVisible(boolean visible) {
		commandsContainer.setVisible(visible);	
	}
	
	@Override
	public void setMediaObjectPanel(Team team) {
		bigTeamBadge.configure(team, team.getDescription());
		mapModal.setTitle(team.getName());
	}	

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		this.synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setLeaveTeamWidget(Widget leaveWidget) {
		this.teamLeavePanel.setWidget(leaveWidget);
	}

	@Override
	public void setDeleteTeamWidget(Widget deleteWidget) {
		this.teamDeletePanel.setWidget(deleteWidget);
	}

	@Override
	public void setEditTeamWidget(Widget editWidget) {
		this.teamEditPanel.setWidget(editWidget);
	}
	
	@Override
	public void setInviteMemberWidget(Widget inviteWidget) {
		this.inviteMemberPanel.setWidget(inviteWidget);
	}
	
	@Override
	public void setJoinTeamWidget(Widget joinWidget) {
		this.joinTeamPanel.setWidget(joinWidget);
	}
	
	@Override
	public void setOpenMembershipRequestWidget(Widget openMembershipRequestWidget) {
		this.openMembershipRequestsPanel.setWidget(openMembershipRequestWidget);
	}
	
	@Override
	public void setOpenUserInvitationsWidget(Widget openUserInvitationsWidget) {
		this.openUserInvitationsPanel.setWidget(openUserInvitationsWidget);
	}
	
	@Override
	public void setMemberListWidget(Widget memberListWidget) {
		this.memberListPanel.setWidget(memberListWidget);
	}

	@Override
	public void setPublicJoinVisible(Boolean canPublicJoin) {
		publicJoinField.setVisible(canPublicJoin);
	}

	@Override
	public void setMemberCountWidget(IsWidget widget) {
		memberCountContainer.clear();
		memberCountContainer.add(widget);
	}

	@Override
	public void setTeamEmailAddress(String teamEmail) {
		synapseEmailField.setText(teamEmail);
	}
	@Override
	public void setMap(Widget w) {
		mapPanel.clear();
		mapPanel.add(w);
	}
	
	@Override
	public void setShowMapVisible(boolean visible) {
		showMapLink.setVisible(visible);
	}
	
	@Override
	public int getClientHeight() {
		return Window.getClientHeight();
	};
	@Override
	public void setManageAccessVisible(boolean visible) {
		manageAccessItem.setVisible(visible);
	}
}
