package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.client.widget.entity.controller.ServiceErrorHandlerImpl;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChangeUsernameViewImpl extends Composite implements ChangeUsernameView {

	public interface ChangeUsernameViewImplUiBinder extends UiBinder<Widget, ChangeUsernameViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	Button changeUsernameButton;
	@UiField
	TextBox username;
	@UiField
	SpanElement messageLabel;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	
	@Inject
	public ChangeUsernameViewImpl(ChangeUsernameViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget,
			SageImageBundle imageBundle) {
		initWidget(binder.createAndBindUi(this));
		
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		username.getElement().setAttribute("placeholder", "Username");
		changeUsernameButton.setText(DisplayConstants.SAVE_BUTTON_LABEL);
		username.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) 
					changeUsernameButton.click();				
			}
		});
		changeUsernameButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				messageLabel.setInnerHTML("");
				changeUsernameButton.setEnabled(false);
				presenter.setUsername(username.getValue());
			}
		});
	}

	@Override
	public void setPresenter(final Presenter presenter) {
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		changeUsernameButton.setEnabled(true);
		username.setValue("");
	}
	
	@Override
	public void showUsernameInvalid() {
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">Username format is invalid.</h4> <span class=\"text-warning\">"+DisplayConstants.USERNAME_FORMAT_ERROR+"</span>");
		clear();
	}
	@Override
	public void showSetUsernameError(Throwable t) {
		SynapseJSNIUtilsImpl._consoleError(ServiceErrorHandlerImpl.getStackTrace(t));
		messageLabel.setInnerHTML("<br/><br/><h4 class=\"text-warning\">Unable to set username: </h4> <span class=\"text-warning\">"+t.getMessage()+"</span>");
		clear();
	}
}
