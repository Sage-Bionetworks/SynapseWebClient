package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.BootstrapAlertType;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.AnimationProtector;
import org.sagebionetworks.web.client.utils.AnimationProtectorViewImpl;

import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

public class JoinTeamWidgetViewImpl extends FlowPanel implements JoinTeamWidgetView {
	
	private static final int FIELD_WIDTH = 500;
	private SageImageBundle sageImageBundle;
	private JoinTeamWidgetView.Presenter presenter;
	private AnimationProtector versionAnimation;
	private LayoutContainer requestUIPanel;
	private Button requestButton, acceptInviteButton;
	private HTML requestedMessage;
	private TextArea messageArea;
	
	@Inject
	public JoinTeamWidgetViewImpl(SageImageBundle sageImageBundle) {
		this.sageImageBundle = sageImageBundle;
	}
	
	@Override
	public void configure(boolean isLoggedIn, TeamMembershipStatus teamMembershipStatus) {
		clear();
		initView();
		//only shown if user is logged in
		if (isLoggedIn && !teamMembershipStatus.getIsMember()) {
			//add request UI, different based on membership state
			if (teamMembershipStatus.getHasOpenInvitation()) {
				//Team admin invited you!
				add(acceptInviteButton);
			} else if (teamMembershipStatus.getHasOpenRequest()) {
				//already requested membership
				add(requestedMessage);
			} else {
				//no affiliation, yet...
				//full expandable UI.  ask for message
				add(requestButton);
				add(requestUIPanel);
				requestUIPanel.setVisible(false);
			} 
		}
	}
	
	private void initView() {
		if (requestUIPanel == null) {
			acceptInviteButton = DisplayUtils.createButton("Accept Invitation to Join Team", ButtonType.SUCCESS);
			acceptInviteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.sendJoinRequest("", true);
				}
			});
			requestedMessage = new HTML(DisplayUtils.getAlertHtml("Request open.", "Your request to join this team has been sent.", BootstrapAlertType.INFO));
			requestUIPanel = new LayoutContainer();
			requestUIPanel.addStyleName("margin-top-0 highlight-box highlight-line-min");
			requestButton = DisplayUtils.createIconButton("Request to Join Team", ButtonType.DEFAULT, "glyphicon-plus");
			versionAnimation = new AnimationProtector(new AnimationProtectorViewImpl(requestButton, requestUIPanel));
			FxConfig hideConfig = new FxConfig(400);
			hideConfig.setEffectCompleteListener(new Listener<FxEvent>() {
				@Override
				public void handleEvent(FxEvent be) {
					// This call to layout is necessary to force the scroll bar to appear on page-load
					requestUIPanel.layout(true);
				}
			});
			versionAnimation.setHideConfig(hideConfig);
			FxConfig showConfig = new FxConfig(400);
			showConfig.setEffectCompleteListener(new Listener<FxEvent>() {
				@Override
				public void handleEvent(FxEvent be) {
					// This call to layout is necessary to force the scroll bar to appear on page-load
					requestUIPanel.layout(true);
				}
			});
			versionAnimation.setShowConfig(showConfig);
			
			messageArea = new TextArea();
			messageArea.setWidth(FIELD_WIDTH);
			messageArea.setEmptyText("Enter message... (optional)");
			requestUIPanel.add(messageArea);
			
			Button sendRequestButton = DisplayUtils.createButton("Send Request");
			sendRequestButton.addStyleName("margin-top-5");
			sendRequestButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.sendJoinRequest(messageArea.getValue(), false);
				}
			});
			requestUIPanel.add(sendRequestButton);
		}
	}	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
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
	}
}
