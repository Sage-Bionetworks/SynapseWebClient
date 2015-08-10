package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.EventHandlerUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.JavaScriptCallback;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamWidgetViewImpl extends FlowPanel implements JoinTeamWidgetView {
	
	public interface JoinTeamWidgetViewImplUiBinder extends UiBinder<Widget, JoinTeamWidgetViewImpl> {}

	@UiField
	SimplePanel joinWizardContainer;
	@UiField
	Button anonUserButton;
	@UiField
	Button acceptInviteButton;
	@UiField
	Button simpleRequestButton;
	@UiField
	TextArea messageArea;
	@UiField
	Button sendRequestButton;
	@UiField
	Span isMemberMessageSpan;
	@UiField
	Span requestOpenMessageSpan;
	@UiField
	HTMLPanel userPanel;
	@UiField
	Button requestButton;
	@UiField
	Collapse requestUIPanel;

	
	private static final int FIELD_WIDTH = 500;
	private SageImageBundle sageImageBundle;
	
	private JoinTeamWidgetView.Presenter presenter;
//	private FlowPanel requestUIPanel;
//	private Button requestButton, acceptInviteButton, anonymousUserButton, simpleRequestButton;
//	private HTML requestedMessage;
//	private TextArea messageArea;
	private MarkdownWidget wikiPage;
	private Dialog joinWizard;
	private FlowPanel currentWizardContent;
	private Callback okButtonCallback;
	private WizardProgressWidget progressWidget;
	private HandlerRegistration messageHandler;
	private Frame externalFrame;
	private Widget widget;
		
	@Inject
	public JoinTeamWidgetViewImpl(JoinTeamWidgetViewImplUiBinder binder, SageImageBundle sageImageBundle, MarkdownWidget wikiPage, WizardProgressWidget progressWidget, Dialog joinWizard) {
		widget = binder.createAndBindUi(this);
		this.sageImageBundle = sageImageBundle;
		this.wikiPage = wikiPage;
		this.progressWidget = progressWidget;
		this.joinWizard = joinWizard;
		joinWizard.addStyleName("modal-fullscreen");
		anonUserButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//redirect to login page
				showAnonymousMessage();
			}
		});		
		acceptInviteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.sendJoinRequest("", true);
			}
		});		
		simpleRequestButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.sendJoinRequest("", false);
			}
		});
		sendRequestButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.sendJoinRequest(messageArea.getValue(), false);
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void clear() {
		// default button text and state
		simpleRequestButton.setText("Join");
		requestOpenMessageSpan.setText("");
		isMemberMessageSpan.setText("Your request to join this team has been sent.");
		isMemberMessageSpan.setVisible(false);
		anonUserButton.setVisible(false);
		acceptInviteButton.setVisible(false);
		requestOpenMessageSpan.setVisible(false);
		simpleRequestButton.setVisible(false);
		requestButton.setVisible(false);
		userPanel.setVisible(false);
		requestButton.setVisible(false);
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
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}

	@Override
	public void setButtonsEnabled(boolean enable) {
		requestButton.setEnabled(enable);
		anonUserButton.setEnabled(enable);
		acceptInviteButton.setEnabled(enable);
		simpleRequestButton.setEnabled(enable);
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
		if (joinWizard != null && joinWizard.isVisible()) {
			joinWizard.hide();
		}
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
		enablePrimaryButton();
	}
			
	public void showChallengeInfoPage(UserProfile profile, WikiPageKey challengeInfoWikiPageKey, Callback presenterCallback) {
		okButtonCallback = presenterCallback;
		Widget wikiPageWidget = wikiPage.asWidget();
        currentWizardContent.clear();
        currentWizardContent.add(wikiPageWidget);
		wikiPage.loadMarkdownFromWikiPage(challengeInfoWikiPageKey, true, false);
	}
	
	@Override
	public void showWikiAccessRequirement(Widget wikiPageWidget, Callback touAcceptanceCallback) {
		showAccessRequirement(wikiPageWidget, touAcceptanceCallback, DisplayConstants.ACCEPT);
	}
	
	@Override
	public void showTermsOfUseAccessRequirement(String arText, Callback touAcceptanceCallback) {
		showAccessRequirement(new HTML(arText), touAcceptanceCallback, DisplayConstants.ACCEPT);
	}
	
	@Override
	public void showACTAccessRequirement(String arText, Callback callback) {
		showAccessRequirement(new HTML(arText), callback, DisplayConstants.BUTTON_CONTINUE);
	}
	
	private void showAccessRequirement(Widget arTextWidget, Callback callback, String primaryButtonText) {
		joinWizard.getPrimaryButton().setText(primaryButtonText);
		currentWizardContent.clear();
        currentWizardContent.add(arTextWidget);
        okButtonCallback = callback;
	}
	
	@Override
	public void showPostMessageContentAccessRequirement(String url, Callback touAcceptanceCallback) {
		//add the iframe, and wait for the message event to re-enable the continue button
		joinWizard.getPrimaryButton().setEnabled(false);
		joinWizard.getPrimaryButton().setText(DisplayConstants.BUTTON_CONTINUE);
		currentWizardContent.clear();
		externalFrame = new Frame(url);
		externalFrame.setHeight("920px");
		externalFrame.getElement().setAttribute("seamless", "true");
		currentWizardContent.add(externalFrame);
		okButtonCallback = touAcceptanceCallback;
	}
	
	/**
	 * Called when message is received from iframe (via postMessage)
	 */
	public void enablePrimaryButton() {
		joinWizard.getPrimaryButton().setEnabled(true);
	}

	@Override
	protected void onAttach() {
		//register to listen for the "message" events
		if (messageHandler == null) {
			messageHandler = EventHandlerUtils.addEventListener("message", EventHandlerUtils.getWnd(), new JavaScriptCallback() {
				@Override
				public void invoke(JavaScriptObject event) {
					if (_isSuccessMessage(event)) {
						enablePrimaryButton();
					} else if (_isSetHeightMessage(event)) {
						if (externalFrame != null)
							externalFrame.setHeight(_getSetHeight(event));
					}
				}
			});
		}
		super.onAttach();
	}
	
	private static native boolean _isSuccessMessage(JavaScriptObject event) /*-{
		console.log("event received: "+event);
		console.log("event.data received: "+event.data);
		return (event !== undefined && event.data !== undefined && typeof event.data === 'string'  && 'success' === event.data.toLowerCase());
    }-*/;
	
	private static native boolean _isSetHeightMessage(JavaScriptObject event) /*-{
		return (event !== undefined && event.data !== undefined && 
			Object.prototype.toString.call( event.data ) === '[object Array]' &&
			'setHeight' === event.data[0]
			);
	}-*/;
	
	private static native String _getSetHeight(JavaScriptObject event) /*-{
		return event.data[1]; 
	}-*/;

	
	@Override
	protected void onDetach() {
		if (messageHandler != null) {
			messageHandler.removeHandler();
			messageHandler = null;
		}
		super.onDetach();
	}
	
	@Override
	public void updateWizardProgress(int currentPage, int totalPages) {
		progressWidget.configure(currentPage, totalPages);
	}

	@Override
	public void showUserPanel() {
		userPanel.setVisible(true);
	}
	
	@Override
	public void hideUserPanel() {
		userPanel.setVisible(false);
	}

	@Override
	public void setIsMemberMessage(String htmlEscape) {
		isMemberMessageSpan.setText(htmlEscape);
	}

	@Override
	public void showRequestedMessage() {
		requestOpenMessageSpan.setVisible(true);
	}

	@Override
	public void showSimpleRequestButton() {
		simpleRequestButton.setVisible(true);
	}

	@Override
	public void showRequestButton() {
		requestButton.setVisible(true);
	}

	@Override
	public void showAcceptInviteButton() {
		acceptInviteButton.setVisible(true);
	}

	@Override
	public void showAnonUserButton() {
		anonUserButton.setVisible(true);
	}

	@Override
	public void setJoinButtonText(String joinButtonText) {
		simpleRequestButton.setText(joinButtonText);
	}

	@Override
	public void setRequestOpenText(String requestOpenText) {
		requestOpenMessageSpan.setText(requestOpenText);
	}

	@Override
	public void showIsMemberMessage() {
		isMemberMessageSpan.setVisible(true);
	}
	
	
}
