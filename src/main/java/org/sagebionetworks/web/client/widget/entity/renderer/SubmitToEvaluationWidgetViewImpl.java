package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.html.Div;
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
	private Div evaluationSubmitterContainer = new Div();
	@Inject
	public SubmitToEvaluationWidgetViewImpl() {
		this.addStyleName("min-height-48");
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String buttonText) {
		this.clear();
		//include the evaluation submitter widget on the page
		add(evaluationSubmitterContainer);
		
		String primaryButtonText = buttonText == null ? "Submit To Challenge" : buttonText;
		Button button = DisplayUtils.createButton(primaryButtonText, ButtonType.PRIMARY);
		button.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.submitToChallengeClicked();
			}
		});				
		add(button);
	}
	
	@Override
	public void showUnavailable(String message) {
		if (message != null && message.trim().length() > 0) {
			Alert alert = new Alert(SafeHtmlUtils.htmlEscape(message), AlertType.INFO);
			alert.addStyleName("displayInline");
			add(alert);
		}
	}
	
	@Override
	public void setEvaluationSubmitterWidget(Widget widget) {
		evaluationSubmitterContainer.clear();
		evaluationSubmitterContainer.add(widget);
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
