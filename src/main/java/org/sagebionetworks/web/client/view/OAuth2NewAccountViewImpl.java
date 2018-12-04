package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OAuth2NewAccountViewImpl extends Composite implements OAuth2NewAccountView {
	
	public interface NewAccountViewImplUiBinder extends UiBinder<Widget, OAuth2NewAccountViewImpl> {}
	public static final String GOOGLE_OAUTH_CALLBACK_URL = "/Portal/oauth2NewAccountCallback?oauth2provider=GOOGLE_OAUTH_2_0&username=";

	@UiField
	TextBox userNameField;
	@UiField
	Div synAlertContainer;
	@UiField
	Button registerBtn;
	
	private Presenter presenter;
	private Header headerWidget;
	SynapseAlert synAlert;
	
	@Inject
	public OAuth2NewAccountViewImpl(NewAccountViewImplUiBinder binder,
			Header headerWidget) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		headerWidget.configure();
		init();
	}
	
	// Apply to all input fields if clickEvent is enter
	public void init() {
		KeyDownHandler register = event -> {
			if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				registerBtn.click();
			}
		};
		userNameField.addKeyDownHandler(register);
		registerBtn.addClickHandler(event -> {
			if(checkUsernameFormat()) {
				DisplayUtils.newWindow(GOOGLE_OAUTH_CALLBACK_URL + userNameField.getValue(), "_self", "");
			}
		});
		userNameField.addBlurHandler(event -> {
			if (checkUsernameFormat())
				presenter.checkUsernameAvailable(userNameField.getValue());
		});
	}

	private boolean checkUsernameFormat() {
		synAlert.clear();
		if (ValidationUtils.isValidUsername(userNameField.getValue())) {
			return true;
		} else {
			synAlert.showError(DisplayConstants.USERNAME_FORMAT_ERROR);
			return false;
		}
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void showErrorMessage(String errorMessage) {
		DisplayUtils.showErrorMessage(errorMessage);
	}
	
	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		userNameField.setValue("");
	}

	@Override
	public void setSynAlert(SynapseAlert w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
		synAlert = w;
	}
}
