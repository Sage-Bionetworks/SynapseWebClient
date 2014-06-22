package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.BootstrapAlertType;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.AnimationProtector;
import org.sagebionetworks.web.client.utils.AnimationProtectorViewImpl;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamWidgetViewImpl extends FlowPanel implements JoinTeamWidgetView {
	
	private static final int FIELD_WIDTH = 500;
	private SageImageBundle sageImageBundle;
	
	private JoinTeamWidgetView.Presenter presenter;
	private AnimationProtector versionAnimation;
	private LayoutContainer requestUIPanel;
	private Button requestButton, acceptInviteButton, anonymousUserButton;
	private HTML requestedMessage;
	private TextArea messageArea;
	private MarkdownWidget wikiPage;
	private Window joinWizard;
	private Button okButton;
	private FlowPanel currentWizardContent;
	private Callback okButtonCallback;
	private WizardProgressWidget progressWidget;
	
	@Inject
	public JoinTeamWidgetViewImpl(SageImageBundle sageImageBundle, MarkdownWidget wikiPage, WizardProgressWidget progressWidget) {
		this.sageImageBundle = sageImageBundle;
		this.wikiPage = wikiPage;
		this.progressWidget = progressWidget;
	}
	
	@Override
	public void configure(boolean isLoggedIn, boolean canPublicJoin, TeamMembershipStatus teamMembershipStatus, String isMemberMessage, String buttonText) {
		clear();
		String joinButtonText = buttonText == null ? WidgetConstants.JOIN_TEAM_DEFAULT_BUTTON_TEXT : buttonText;
		initView(joinButtonText);
		
		if (isLoggedIn) {
			//(note:  in all cases, clicking UI will check for unmet ToU)
			if (teamMembershipStatus.getIsMember()) {
				// don't show anything?
				if(isMemberMessage != null && isMemberMessage.length() > 0){
					add(new HTML(DisplayUtils.getAlertHtmlSpan(SafeHtmlUtils.htmlEscape(isMemberMessage), "", BootstrapAlertType.INFO)));
				}
			} else if (teamMembershipStatus.getCanJoin()) { // not in team but can join with a single request
				// show join button; clicking Join joins the team
				add(acceptInviteButton);
			} else if (teamMembershipStatus.getHasOpenRequest()) {
				// display a message saying "your membership request is pending review by team administration"
				add(requestedMessage);
			} else if (teamMembershipStatus.getMembershipApprovalRequired()) {
				// show request UI 
				add(requestButton);
				add(requestUIPanel);
				requestUIPanel.setVisible(false);
			} else if (teamMembershipStatus.getHasUnmetAccessRequirement()) {
			    // show Join; clicking shows ToU
				add(acceptInviteButton);
			} else {
			    // illegal state
				showErrorMessage("Unable to determine state");
			}
		}
		else {
			add(anonymousUserButton);
		}
	}
	
	private void showAnonymousMessage() {
		Callback okCallback = new Callback() {
			@Override
			public void invoke() {
				presenter.gotoLoginPage();
			}
		};
		Callback cancelCallback = new Callback() {
			@Override
			public void invoke() {
			}	
		};

		DisplayUtils.showOkCancelMessage("Login or Register", DisplayConstants.ANONYMOUS_JOIN, MessagePopup.INFO, 320, okCallback, cancelCallback);
	}
	
	private void initView(String joinButtonText) {
		if (requestUIPanel == null) {
			anonymousUserButton = DisplayUtils.createButton(joinButtonText, ButtonType.PRIMARY);
			anonymousUserButton.addStyleName("btn-lg");
			anonymousUserButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//redirect to login page
					showAnonymousMessage();
				}
			});
			
			acceptInviteButton = DisplayUtils.createButton(joinButtonText, ButtonType.PRIMARY);
			acceptInviteButton.addStyleName("btn-lg");
			acceptInviteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.sendJoinRequest("", true);
				}
			});
			requestedMessage = new HTML(DisplayUtils.getAlertHtmlSpan("Request open.", "Your request to join this team has been sent.", BootstrapAlertType.INFO));
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
		messageArea.setValue("");
		currentWizardContent = new FlowPanel();
		currentWizardContent.addStyleName("min-height-400 whiteBackground padding-5");
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
		clear();
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(message)));
		hideJoinWizard();
	}

	@Override
	public void hideJoinWizard() {
		if (joinWizard != null && joinWizard.isVisible())
			joinWizard.hide();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
			@Override
	public void showJoinWizard() {
		joinWizard = new Window();
		joinWizard.addStyleName("whiteBackground");
       	joinWizard.setMaximizable(false);
        joinWizard.setSize(640, 480);
        joinWizard.setPlain(true); 
        joinWizard.setModal(true); 
        joinWizard.setAutoHeight(true);
        joinWizard.setResizable(false);
        joinWizard.setHeading("Join");
		FlowPanel buttonPanel = new FlowPanel();
		buttonPanel.addStyleName("bottomright");
		Button cancelButton = DisplayUtils.createButton(DisplayConstants.BUTTON_CANCEL);
		cancelButton.addStyleName("right margin-bottom-10 margin-right-10");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				joinWizard.hide();
			}
		});
			
		okButton = DisplayUtils.createButton("Continue", ButtonType.PRIMARY);
		okButton.addStyleName("right margin-bottom-10 margin-right-10");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				okButtonCallback.invoke();
			}
		});
			
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		joinWizard.add(progressWidget.asWidget());
        joinWizard.add(currentWizardContent);
        joinWizard.add(buttonPanel);
		joinWizard.show();
		DisplayUtils.center(joinWizard);
	}
			
	public void showChallengeInfoPage(UserProfile profile, WikiPageKey challengeInfoWikiPageKey, Callback presenterCallback) {
		okButtonCallback = presenterCallback;
		Widget wikiPageWidget = wikiPage.asWidget();
        currentWizardContent.clear();
        ScrollPanel panel = new ScrollPanel(wikiPageWidget);
        panel.setHeight("400px");
        panel.addStyleName("whiteBackground padding-5 margin-bottom-60");
        currentWizardContent.add(panel);
		wikiPage.loadMarkdownFromWikiPage(challengeInfoWikiPageKey, true);
		joinWizard.layout(true);
	}
		
	@Override
	public void showAccessRequirement(
			String arText,
			final Callback touAcceptanceCallback) {
		DisplayUtils.relabelIconButton(okButton, DisplayConstants.ACCEPT, null);
		currentWizardContent.clear();
        ScrollPanel panel = new ScrollPanel(new HTML(arText));
		panel.setHeight("400px");
		panel.addStyleName("whiteBackground padding-5 margin-bottom-60");
        currentWizardContent.add(panel);
        joinWizard.layout(true);
        okButtonCallback = touAcceptanceCallback;
	}
	
			@Override
	public void updateWizardProgress(int currentPage, int totalPages) {
		progressWidget.configure(currentPage, totalPages);
			}
	
	
}
