package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.place.OAuth2NewAccount;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidgetViewImpl;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterAccountViewImpl extends Composite implements RegisterAccountView {
	public static final String GOOGLE_OAUTH_CALLBACK_URL = LoginWidgetViewImpl.GOOGLE_OAUTH_CALLBACK_URL + "&state=";
	public interface RegisterAccountViewImplUiBinder extends UiBinder<Widget, RegisterAccountViewImpl> {}
	
	@UiField
	Div registerWidgetContainer;
	@UiField
	TextBox userNameField;
	@UiField
	Button googleSignUpButton;
	@UiField
	Div googleSynAlertContainer;
	SynapseAlert synAlert;
	GWTTimer timer;
	Presenter presenter;
	private Header headerWidget;
	
	@Inject
	public RegisterAccountViewImpl(RegisterAccountViewImplUiBinder binder, 
			GlobalApplicationState globalAppState,
			Header headerWidget,
			GWTTimer timer) {		
		initWidget(binder.createAndBindUi(this));
		googleSignUpButton.addClickHandler(event -> globalAppState.getPlaceChanger().goTo(new OAuth2NewAccount("default")));
		this.timer = timer;
		this.headerWidget = headerWidget;
		timer.configure(() -> {
			setGoogleRegisterButtonEnabled(false);
			if (checkUsernameFormat())
				presenter.checkUsernameAvailable(userNameField.getValue());
		});
		KeyDownHandler register = event -> {
			if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				googleSignUpButton.click();
			} else {
				timer.cancel();
				timer.schedule(SynapseSuggestBox.DELAY);
			}
		};
		userNameField.addKeyDownHandler(register);
		googleSignUpButton.addClickHandler(event -> {
			if(checkUsernameFormat()) {
				String encodedUsername = URL.encodeQueryString(userNameField.getValue());
				Window.Location.assign(GOOGLE_OAUTH_CALLBACK_URL + encodedUsername);
			}
		});
	}
	
	private boolean checkUsernameFormat() {
		synAlert.clear();
		if (userNameField.getValue().length() > 3 && ValidationUtils.isValidUsername(userNameField.getValue())) {
			return true;
		} else {
			synAlert.showError(DisplayConstants.USERNAME_FORMAT_ERROR);
			return false;
		}
	}

	@Override
	public void setRegisterWidget(Widget w) {
		registerWidgetContainer.clear();
		registerWidgetContainer.add(w);
	}
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setGoogleRegisterButtonEnabled(boolean enabled) {
		googleSignUpButton.setEnabled(enabled);
	}
	@Override
	public void setGoogleSynAlert(SynapseAlert w) {
		googleSynAlertContainer.clear();
		googleSynAlertContainer.add(w);
		synAlert = w;
	}
}
