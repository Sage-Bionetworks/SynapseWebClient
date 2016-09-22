package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.InviteWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamViewImpl extends Composite implements TeamView {

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
	SimplePanel mediaObjectContainer;
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
	@UiField
	TextBox synapseEmailField;
	@UiField
	Div mapPanel;
	@UiField
	Modal mapModal;
	@UiField
	Anchor showMapLink;
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private Header headerWidget;
	private Footer footerWidget;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public TeamViewImpl(TeamViewImplUiBinder binder, 
			SageImageBundle sageImageBundle,
			InviteWidget inviteWidget, 
			Header headerWidget, 
			Footer footerWidget, 
			SynapseJSNIUtils synapseJSNIUtils) {
		initWidget(binder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		setDropdownHandlers();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		showMapLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onShowMap();
			}
		});
	}
	
	@Override
	public void showMapModal() {
		mapModal.show();
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
		synapseEmailField.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				synapseEmailField.selectAll();
			}
		});
	}
	
	@Override
	public void clear() {
		mediaObjectContainer.clear();
		commandsContainer.setVisible(false);
		inviteMemberItem.setVisible(false);
		editTeamItem.setVisible(false);
		deleteTeamItem.setVisible(false);
		leaveTeamItem.setVisible(false);
		publicJoinField.setVisible(false);
		synapseEmailField.setValue("");
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
	public void showMemberMenuItems() {
		leaveTeamItem.setVisible(true);
		commandsContainer.setVisible(true);
	}
	
	@Override
	public void showAdminMenuItems() {
		deleteTeamItem.setVisible(true);
		editTeamItem.setVisible(true);
		inviteMemberItem.setVisible(true);
		commandsContainer.setVisible(true);
	}
	
	@Override
	public void setMediaObjectPanel(Team team, String xsrfToken) {
		String pictureUrl = null;
		if (team.getIcon() != null) {
			pictureUrl = DisplayUtils.createTeamIconUrl(synapseJSNIUtils.getBaseFileHandleUrl(), team.getId(), xsrfToken) + "&imageId=" + team.getIcon();
		}
		FlowPanel mediaObjectPanel = DisplayUtils.getMediaObject(team.getName(), team.getDescription(), null,  pictureUrl, false, 2);
		mediaObjectContainer.setWidget(mediaObjectPanel.asWidget());
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
	public void setTotalMemberCount(String memberCount) {
		totalMemberCountField.setText(memberCount);
	}

	@Override
	public void setTeamEmailAddress(String teamEmail) {
		synapseEmailField.setValue(teamEmail);
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
}
