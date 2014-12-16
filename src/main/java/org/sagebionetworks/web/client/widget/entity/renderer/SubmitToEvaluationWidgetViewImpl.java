package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.BootstrapAlertType;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubmitToEvaluationWidgetViewImpl extends FlowPanel implements SubmitToEvaluationWidgetView {
	private Presenter presenter;
	
	@Inject
	public SubmitToEvaluationWidgetViewImpl() {
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, boolean isAvailableEvaluation, String unavailableMessage, String buttonText) {
		this.clear();
		
		if (isAvailableEvaluation) {
			String primaryButtonText = buttonText == null ? "Submit To Challenge" : buttonText;
			Button button = DisplayUtils.createButton(primaryButtonText, ButtonType.PRIMARY);
			button.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					presenter.submitToChallengeClicked();
				}
			});				
			add(button);
		} else if (unavailableMessage != null && unavailableMessage.trim().length() > 0){
			add(new HTML(DisplayUtils.getAlertHtmlSpan(SafeHtmlUtils.htmlEscape(unavailableMessage), "", BootstrapAlertType.INFO)));
		}
	}
	
	@Override
	public void showErrorMessage(String error) {
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showAnonymousRegistrationMessage() {
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

		DisplayUtils.showPopup("Login or Register", DisplayConstants.ANONYMOUS_JOIN_EVALUATION, MessagePopup.INFO, okCallback, cancelCallback);
	}
	

	@Override
	public void showLoading() {
	}
	
	/*
	 * Private Methods
	 */

}
