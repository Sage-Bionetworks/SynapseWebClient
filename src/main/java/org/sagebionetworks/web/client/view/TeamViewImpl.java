package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamViewImpl extends Composite implements TeamView {

	public interface TeamViewImplUiBinder extends UiBinder<Widget, TeamViewImpl> {
	}

	@UiField
	Anchor toolsMenuLink;
	@UiField
	Heading teamNameHeading;
	@UiField
	HTMLPanel mainContainer;
	@UiField
	Div commandsContainer;
	@UiField
	Div teamBadgeContainer;
	@UiField
	SimplePanel inviteMemberPanel;
	@UiField
	SimplePanel synAlertPanel;
	@UiField
	SimplePanel joinTeamPanel;
	@UiField
	SimplePanel openMembershipRequestsPanel;
	@UiField
	SimplePanel openUserInvitationsPanel;
	@UiField
	SimplePanel managerListPanel;
	@UiField
	SimplePanel memberListPanel;
	@UiField
	Div publicJoinField;
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
	AnchorListItem teamProjectsItem;
	@UiField
	Div mapPanel;
	@UiField
	Modal mapModal;
	@UiField
	Anchor showMapLink;
	@UiField
	org.gwtbootstrap3.client.ui.TextBox memberSearchTextBox;
	@UiField
	Icon memberSearchButton;
	@UiField
	Div widgetsContainer;
	private Presenter presenter;
	private Header headerWidget;
	private GWTWrapper gwt;
	private BigTeamBadge bigTeamBadge;
	private CookieProvider cookieProvider;

	@Inject
	public TeamViewImpl(TeamViewImplUiBinder binder, InviteWidget inviteWidget, Header headerWidget, GWTWrapper gwt, BigTeamBadge bigTeamBadge, CookieProvider cookieProvider) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.gwt = gwt;
		this.bigTeamBadge = bigTeamBadge;
		this.cookieProvider = cookieProvider;
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
			if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
				presenter.onMemberSearch(memberSearchTextBox.getValue());
			}
		});
		Icon icon = new Icon(IconType.ELLIPSIS_V);
		icon.addStyleName("SRC-primary-background-color-hover");
		icon.setPaddingTop(9);
		icon.setPaddingBottom(9);
		icon.setPaddingLeft(4);
		icon.setPaddingRight(4);
		toolsMenuLink.add(icon);
	}

	@Override
	public void showMapModal() {
		mapModal.show();
	}

	private void setDropdownHandlers() {
		inviteMemberItem.addClickHandler(event -> {
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
		teamProjectsItem.addClickHandler(event -> {
			gwt.scheduleDeferred(() -> {
				presenter.showTeamProjectsModal();
			});
		});
	}

	@Override
	public void clear() {
		teamNameHeading.setText("");
		commandsContainer.setVisible(false);
		inviteMemberItem.setVisible(false);
		editTeamItem.setVisible(false);
		deleteTeamItem.setVisible(false);
		leaveTeamItem.setVisible(false);
		publicJoinField.setVisible(false);
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
	public void setTeam(Team team, TeamMembershipStatus status) {
		teamNameHeading.setText(team.getName());
		bigTeamBadge.configure(team, team.getDescription(), status);
		mapModal.setTitle(team.getName());

		// TODO: remove next line to take out of alpha mode
		teamProjectsItem.setVisible(DisplayUtils.isInTestWebsite(cookieProvider));
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		this.synAlertPanel.setWidget(synAlert);
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
	public void addWidgets(Widget... widgets) {
		for (Widget widget : widgets) {
			widgetsContainer.add(widget);
		}
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
	public void setManagerListWidget(Widget managerListWidget) {
		this.managerListPanel.setWidget(managerListWidget);
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
