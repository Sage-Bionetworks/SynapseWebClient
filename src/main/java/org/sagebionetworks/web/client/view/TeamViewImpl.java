package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.DropdownButton;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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
	FlowPanel mainContainer;
	@UiField
	SimplePanel mediaObjectContainer;
	@UiField
	FlowPanel commandsContainer;
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
	private Uploader uploader;
	private IconsImageBundle iconsImageBundle;
	
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
			Uploader uploader,
			IconsImageBundle iconsImageBundle
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
		this.iconsImageBundle = iconsImageBundle;
		
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}
	
	@Override
	public void clear() {
		mediaObjectContainer.clear();
		commandsContainer.clear();
		mainContainer.clear();
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
			pictureUrl = DisplayUtils.createTeamIconUrl(synapseJSNIUtils.getBaseFileHandleUrl(), team.getId());
		}
		
		FlowPanel mediaObjectPanel = DisplayUtils.getMediaObject(team.getName(), team.getDescription(), null,  pictureUrl, false, 2);
		mediaObjectContainer.add(mediaObjectPanel);
		mainContainer.add(new HTML("<div><span class=\"padding-left-74 boldText margin-right-5\">Total members: </span><span>"+totalMemberCount+"</span></div>"));
		//initialize the tools menu button
		toolsButton = new DropdownButton(DisplayConstants.BUTTON_TOOLS_MENU, ButtonType.DEFAULT, "glyphicon-cog");
		toolsButton.addStyleName("pull-right margin-left-5");
				
		if (isAdmin) {
			CallbackP<String> fileHandleIdCallback = new CallbackP<String>(){
				@Override
				public void invoke(String fileHandleId) {
					presenter.updateTeamInfo(team.getName(), team.getDescription(), TeamSearchPresenter.getCanPublicJoin(team), fileHandleId);
				}
			};
			Widget uploadLink = DisplayUtils.getUploadButton(fileHandleIdCallback, uploader, iconsImageBundle, "Update Icon", ButtonType.LINK); 
			uploadLink.addStyleName("updateTeamIconLink");
			//only show upload link if direct upload is supported
			if (synapseJSNIUtils.isDirectUploadSupported())
				mainContainer.add(uploadLink);
			Callback refreshCallback = getRefreshCallback(team.getId());
			//show invite UI
			inviteWidget.configure(team.getId(), refreshCallback);
			mainContainer.add(inviteWidget.asWidget());
			openMembershipRequestsWidget.configure(team.getId(), refreshCallback);
			mainContainer.add(openMembershipRequestsWidget.asWidget());
			
			openUserInvitationsWidget.configure(team.getId(), refreshCallback);
			mainContainer.add(openUserInvitationsWidget.asWidget());
			
			//fill in the tools menu button
			addEditItem(toolsButton);
			addDeleteItem(toolsButton);
		}
		
		if (teamMembershipStatus != null) {
			if (!teamMembershipStatus.getIsMember()) {
				//not a member, add Join widget
				joinTeamWidget.configure(team.getId(), TeamSearchPresenter.getCanPublicJoin(team), false, teamMembershipStatus, getRefreshCallback(team.getId()), null, null, null);
				Widget joinTeamView = joinTeamWidget.asWidget();
				joinTeamView.addStyleName("margin-top-15");	
				mainContainer.add(joinTeamView);
			}
			else {
				//add Leave Team menu item, and show tools menu
				addLeaveItem(toolsButton); 
				commandsContainer.add(toolsButton);
			}
		}
		memberListWidget.configure(team.getId(), isAdmin, getRefreshCallback(team.getId()));
		Widget memberListView = memberListWidget.asWidget();
		memberListView.addStyleName("margin-top-15");
		mainContainer.add(memberListView);
	}
	
	private void showEditMode() {
		clear();
		LayoutContainer form = new LayoutContainer();
		form.addStyleName("margin-top-15 margin-bottom-40");
		
		final TextBox nameField = new TextBox();
		nameField.setValue(team.getName());
		nameField.addStyleName("col-md-12 font-size-32 margin-left-10 margin-bottom-10");
		nameField.setHeight("35px");
		form.add(DisplayUtils.wrap(nameField));
		
		final TextBox descriptionField = new TextBox();
		descriptionField.setValue(team.getDescription());
		descriptionField.getElement().setAttribute("placeholder", DisplayConstants.SHORT_TEAM_DESCRIPTION);
		descriptionField.addStyleName("col-md-12 margin-left-10 margin-bottom-10");
		form.add(DisplayUtils.wrap(descriptionField));
		final CheckBox publicJoinCb = new CheckBox("People can join this team without team administrator authorization");
		boolean isPublicJoin = TeamSearchPresenter.getCanPublicJoin(team);
		publicJoinCb.setValue(isPublicJoin);
		FlowPanel cbPanel = new FlowPanel();
		cbPanel.addStyleName("checkbox margin-left-10");
		cbPanel.add(publicJoinCb);
		form.add(DisplayUtils.wrap(cbPanel));
		Button saveButton = DisplayUtils.createButton(DisplayConstants.SAVE_BUTTON_LABEL, ButtonType.PRIMARY);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.updateTeamInfo(nameField.getValue(), descriptionField.getValue(), publicJoinCb.getValue(), team.getIcon());
			}
		});
		saveButton.addStyleName("right margin-right-5");
		Button cancelButton = DisplayUtils.createButton(DisplayConstants.BUTTON_CANCEL);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.refresh(team.getId());
			}
		});
		cancelButton.addStyleName("right margin-right-15");
		
		LayoutContainer buttonsContainer = new LayoutContainer();
		buttonsContainer.addStyleName("row");
		buttonsContainer.add(cancelButton);
		buttonsContainer.add(saveButton);
		
		form.add(buttonsContainer);
		mainContainer.add(form);
	}
	
	private void addEditItem(DropdownButton menuBtn) {
		Anchor a = new Anchor(DisplayConstants.BUTTON_EDIT);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				showEditMode();
			}
		});
		menuBtn.addMenuItem(a);
	}

	private void addLeaveItem(DropdownButton menuBtn) {
		Anchor a = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayConstants.BUTTON_LEAVE_TEAM));
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog("Leave Team?", DisplayConstants.PROMPT_SURE_LEAVE_TEAM, new Callback() {
					@Override
					public void invoke() {
						presenter.leaveTeam();
					}
				}); 
			}
		});				
		menuBtn.addMenuItem(a);
	}
	
	private void addDeleteItem(DropdownButton menuBtn) {
		Anchor a = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIcon("glyphicon-remove") + " "
				+ DisplayConstants.LABEL_DELETE + " Team"));
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog(DisplayConstants.LABEL_DELETE +" Team?", DisplayConstants.PROMPT_SURE_DELETE + " Team" +"?", new Callback() {
					@Override
					public void invoke() {
						presenter.deleteTeam();
					}
				});
			}
		});
		menuBtn.addMenuItem(a);
	}
	
	
	private Callback getRefreshCallback(final String teamId) {
		return new Callback() {
			@Override
			public void invoke() {
				presenter.refresh(teamId);
			}
		};
	}

}
