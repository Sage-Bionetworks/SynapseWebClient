package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.BootstrapAlertType;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamWidgetViewImpl extends FlowPanel implements JoinTeamWidgetView {
	
	private static final int FIELD_WIDTH = 500;
	private SageImageBundle sageImageBundle;
	
	private JoinTeamWidgetView.Presenter presenter;
	private FlowPanel requestUIPanel;
	private Button requestButton, acceptInviteButton, anonymousUserButton;
	private HTML requestedMessage;
	private TextArea messageArea;
	private MarkdownWidget wikiPage;
	private Dialog joinWizard;
	private FlowPanel currentWizardContent;
	private Callback okButtonCallback;
	private WizardProgressWidget progressWidget;
	
	@Inject
	public JoinTeamWidgetViewImpl(SageImageBundle sageImageBundle, MarkdownWidget wikiPage, WizardProgressWidget progressWidget, Dialog joinWizard) {
		this.sageImageBundle = sageImageBundle;
		this.wikiPage = wikiPage;
		this.progressWidget = progressWidget;
		this.joinWizard = joinWizard;
		joinWizard.setSize(ModalSize.LARGE);
	}
	
	@Override
	public void configure(boolean isLoggedIn, boolean canPublicJoin, TeamMembershipStatus teamMembershipStatus, String isMemberMessage, String buttonText) {
		clear();
		String joinButtonText = buttonText == null ? WidgetConstants.JOIN_TEAM_DEFAULT_BUTTON_TEXT : buttonText;
		initView(joinButtonText);
		add(joinWizard);
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

		DisplayUtils.showPopup("Login or Register", DisplayConstants.ANONYMOUS_JOIN, MessagePopup.INFO, okCallback, cancelCallback);
	}
	
	private void initView(String joinButtonText) {
		if (requestUIPanel == null) {
			anonymousUserButton = new Button(joinButtonText, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//redirect to login page
					showAnonymousMessage();
				}
			});
			anonymousUserButton.setType(ButtonType.PRIMARY);
			anonymousUserButton.setSize(ButtonSize.LARGE);
			
			acceptInviteButton =  new Button(joinButtonText, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.sendJoinRequest("", true);
				}
			});
			acceptInviteButton.setType(ButtonType.PRIMARY);
			acceptInviteButton.setSize(ButtonSize.LARGE);
			
			requestedMessage = new HTML(DisplayUtils.getAlertHtmlSpan("Request open.", "Your request to join this team has been sent.", BootstrapAlertType.INFO));
			requestUIPanel = new FlowPanel();
			requestUIPanel.addStyleName("margin-top-0 highlight-box highlight-line-min");
			requestButton = new Button("Request to Join Team", IconType.PLUS, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					requestUIPanel.setVisible(!requestUIPanel.isVisible());
				}
			});
			
			messageArea = new TextArea();
			messageArea.setWidth(FIELD_WIDTH + "px");
			messageArea.setPlaceholder("Enter message... (optional)");

			requestUIPanel.add(messageArea);
			
			Button sendRequestButton = new Button("Send Request", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.sendJoinRequest(messageArea.getValue(), false);
				}
			});
			sendRequestButton.addStyleName("margin-top-5");
			requestUIPanel.add(sendRequestButton);
		}
		messageArea.setValue("");
		currentWizardContent = new FlowPanel();
		currentWizardContent.addStyleName("min-height-400 padding-5");
	}	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}

	@Override
	public void setButtonsEnabled(boolean enable) {
		requestButton.setEnabled(enable);
		anonymousUserButton.setEnabled(enable);
		acceptInviteButton.setEnabled(enable);
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
		FlowPanel body = new FlowPanel();
		body.add(progressWidget.asWidget());
        body.add(currentWizardContent);
		joinWizard.configure("Join", body, "Continue", DisplayConstants.BUTTON_CANCEL, new Dialog.Callback() {
			@Override
			public void onPrimary() {
				okButtonCallback.invoke();
			}
			
			@Override
			public void onDefault() {
				joinWizard.hide();
			}
		}, false);
		
		joinWizard.show();
	}
			
	public void showChallengeInfoPage(UserProfile profile, WikiPageKey challengeInfoWikiPageKey, Callback presenterCallback) {
		okButtonCallback = presenterCallback;
		Widget wikiPageWidget = wikiPage.asWidget();
        currentWizardContent.clear();
        currentWizardContent.add(wikiPageWidget);
		wikiPage.loadMarkdownFromWikiPage(challengeInfoWikiPageKey, true);
	}
		
	@Override
	public void showAccessRequirement(
			String arText,
			final Callback touAcceptanceCallback) {
		joinWizard.getPrimaryButton().setText(DisplayConstants.ACCEPT);
		currentWizardContent.clear();
        currentWizardContent.add(new HTML(arText));
        okButtonCallback = touAcceptanceCallback;
	}
	
			@Override
	public void updateWizardProgress(int currentPage, int totalPages) {
		progressWidget.configure(currentPage, totalPages);
	}
	
	
}
