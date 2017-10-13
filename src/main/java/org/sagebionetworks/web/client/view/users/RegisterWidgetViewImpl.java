package org.sagebionetworks.web.client.view.users;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.web.client.DisplayUtils;

public class RegisterWidgetViewImpl implements RegisterWidgetView {
	public interface Binder extends UiBinder<Widget, RegisterWidgetViewImpl> {}
	private Presenter presenter;
	Widget widget;
	@UiField
	Div synAlertContainer;
	@UiField
	Div blockUI;

	@UiField
	TextBox emailAddressField;
	@UiField
	Button registerBtn;
	
	@Inject
	public RegisterWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		emailAddressField.getElement().setAttribute("placeholder", "Your email");
		initClickHandlers();
	}

	public void initClickHandlers() {
		registerBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				NewUser newUser = new NewUser();
				newUser.setEmail(emailAddressField.getValue());
				newUser.setEncodedMembershipInvtnSignedToken(presenter.getEncodedMembershipInvtnSignedToken());
				presenter.registerUser(newUser);
			}
		});
		
		emailAddressField.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		        	registerBtn.click();
		        }
		    }
		});
	}
	
	@Override
	public Widget asWidget() {		
		return widget;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		widget.setVisible(isVisible);
	}
	@Override
	public void enableRegisterButton(boolean enable) {
		registerBtn.setEnabled(enable);
	}
	
	@Override
	public void clear() {
		emailAddressField.setText("");
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setEmail(String email) {
		emailAddressField.setText(email);
	}
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message, "");
	}

	@Override
	public void enableEmailAddressField(boolean enabled) {
		emailAddressField.setEnabled(enabled);
	}
}
