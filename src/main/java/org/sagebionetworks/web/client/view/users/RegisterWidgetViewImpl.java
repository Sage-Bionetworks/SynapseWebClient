package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterWidgetViewImpl implements RegisterWidgetView {
	public interface Binder extends UiBinder<Widget, RegisterWidgetViewImpl> {
	}

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
		emailAddressField.getElement().setAttribute("placeholder", "Your email address");
		initClickHandlers();
	}

	public void initClickHandlers() {
		registerBtn.addClickHandler(event -> presenter.registerUser(emailAddressField.getValue()));
		emailAddressField.addKeyDownHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				registerBtn.click();
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
		DisplayUtils.showInfo(message);
	}

	@Override
	public void enableEmailAddressField(boolean enabled) {
		emailAddressField.setEnabled(enabled);
	}
}
