package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.DropdownButton;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamViewImpl extends Composite implements TeamView {

	private static final String PUBLIC_JOIN_TEXT = "People can join this team without team manager authorization";


	public interface TeamViewImplUiBinder extends UiBinder<Widget, TeamViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	HTMLPanel mainContainer;
	@UiField
	Column commandsContainer;
	@UiField
	Column mediaObjectContainer;
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
	Span totalMemberCountField;
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
	
	private Team team;
	private DropdownButton toolsButton;
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private MemberListWidget memberListWidget;
	private OpenMembershipRequestsWidget openMembershipRequestsWidget;
	private OpenUserInvitationsWidget openUserInvitationsWidget;
	private InviteWidget inviteWidget;
	private JoinTeamWidget joinTeamWidget;
	private Header headerWidget;
	private Footer footerWidget;
	private SynapseJSNIUtils synapseJSNIUtils;
	private UploadDialogWidget uploader;
	
	@Inject
	public TeamViewImpl(TeamViewImplUiBinder binder, 
			SageImageBundle sageImageBundle, 
			MemberListWidget memberListWidget, 
			OpenMembershipRequestsWidget openMembershipRequestsWidget,
			OpenUserInvitationsWidget openUserInvitationsWidget,
			InviteWidget inviteWidget, 
			JoinTeamWidget joinTeamWidget, 
			Header headerWidget, 
			Footer footerWidget, 
			SynapseJSNIUtils synapseJSNIUtils,
			UploadDialogWidget uploader			
			) {
		initWidget(binder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.memberListWidget = memberListWidget;
		this.openMembershipRequestsWidget = openMembershipRequestsWidget;
		this.openUserInvitationsWidget = openUserInvitationsWidget;
		this.inviteWidget = inviteWidget;
		this.joinTeamWidget = joinTeamWidget;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.uploader = uploader;
		uploader.disableMultipleFileUploads();
		setDropdownHandlers();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}
	
	private void setDropdownHandlers() {
		inviteMemberItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.showInviteModal();
			}
		});
		editTeamItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.showEditModal();
			}
		});
		deleteTeamItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.showDeleteModal();
			}
		});
		leaveTeamItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.showLeaveModal();
			}
		});
	}
	
	@Override
	public void clear() {
		mediaObjectContainer.clear();
		commandsContainer.setVisible(false);
		leaveTeamItem.setVisible(true);
	}
	
	@Override
	public void showLoading() {
		clear();
		mainContainer.add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void configure(final Team team, boolean isAdmin, TeamMembershipStatus teamMembershipStatus, Long totalMemberCount) {
		clear();
		
		this.team = team;
		String pictureUrl = null;
		if (team.getIcon() != null) {
			pictureUrl = DisplayUtils.createTeamIconUrl(synapseJSNIUtils.getBaseFileHandleUrl(), team.getId()) + "&imageId=" + team.getIcon();
		}
		
		FlowPanel mediaObjectPanel = DisplayUtils.getMediaObject(team.getName(), team.getDescription(), null,  pictureUrl, false, 2);
		mediaObjectContainer.add(mediaObjectPanel);
		totalMemberCountField.setText(totalMemberCount.toString());
		publicJoinField.setVisible(team.getCanPublicJoin());
		if (isAdmin) {
			Callback refreshCallback = getRefreshCallback(team.getId());
			openMembershipRequestsWidget.configure(team.getId(), refreshCallback);
			mainContainer.add(openMembershipRequestsWidget.asWidget());
			openUserInvitationsWidget.configure(team.getId(), refreshCallback);
			mainContainer.add(openUserInvitationsWidget.asWidget());
			deleteTeamItem.setVisible(true);
			editTeamItem.setVisible(true);
			inviteMemberItem.setVisible(true);
		}
		
		if (teamMembershipStatus != null) {
			if (!teamMembershipStatus.getIsMember()) {
				//not a member, add Join widget
				joinTeamWidget.configure(team.getId(), false, teamMembershipStatus, getRefreshCallback(team.getId()), null, null, null, null, false);
				Widget joinTeamView = joinTeamWidget.asWidget();
				joinTeamView.addStyleName("margin-top-15");	
				mainContainer.add(joinTeamView);
			}
			else {
				leaveTeamItem.setVisible(true);
				commandsContainer.setVisible(true);
			}
		}
		memberListWidget.configure(team.getId(), isAdmin, getRefreshCallback(team.getId()));
		Widget memberListView = memberListWidget.asWidget();
		memberListView.addStyleName("margin-top-15");
		mainContainer.add(memberListView);
	}	
	
	private Callback getRefreshCallback(final String teamId) {
		return new Callback() {
			@Override
			public void invoke() {
				presenter.refresh(teamId);
			}
		};
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

}
