package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EventHandlerUtils;
import org.sagebionetworks.web.client.utils.JavaScriptCallback;
import org.sagebionetworks.web.client.widget.entity.download.UploaderViewImpl;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginModalViewImpl implements LoginModalView {

	public interface Binder extends UiBinder<Modal, LoginModalViewImpl> {
	}

	@UiField
	Button primaryButton;
	@UiField
	Text instructions;

	@UiField
	TextBox usernameField;
	@UiField
	Input passwordField;

	@UiField
	Form formPanel;

	@UiField
	Alert alert;

	Modal modal;
	Presenter presenter;
	private HandlerRegistration messageHandler;

	@Inject
	public LoginModalViewImpl(Binder binder) {
		modal = binder.createAndBindUi(this);
		primaryButton.addDomHandler(DisplayUtils.getPreventTabHandler(primaryButton), KeyDownEvent.getType());
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		KeyDownHandler login = new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					primaryButton.click();
				}
			}
		};
		usernameField.addKeyDownHandler(login);
		passwordField.addKeyDownHandler(login);
		primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onPrimary();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return modal;
	}


	@Override
	public void showModal() {
		modal.show();
	}

	@Override
	public void showAlert(boolean visible) {
		this.alert.setVisible(visible);
	}

	@Override
	public void showErrorMessage(String error) {
		this.alert.setText(error);
	}

	@Override
	public void showErrorMessagePopup(String error) {
		DisplayUtils.showErrorMessage(error);
	}

	@Override
	public void setLoading(boolean loading) {
		if (!loading) {
			this.primaryButton.state().reset();
		} else {
			this.primaryButton.state().loading();
		}
	}

	@Override
	public void hideModal() {
		modal.hide();
	}

	@Override
	public void setPrimaryButtonText(String text) {
		this.primaryButton.setText(text);
	}

	@Override
	public void setTitle(String title) {
		modal.setTitle(title);
	}

	@Override
	public void setSize(ModalSize size) {
		modal.setSize(size);
	}

	@Override
	public void setInstructionsMessage(String message) {
		this.instructions.setText(message);
	}

	@Override
	public void submitForm(String actionUrl, String method, String encodingType) {
		initMessageHandler();
		if (encodingType != null)
			FormElement.as(formPanel.getElement()).setEnctype(encodingType);
		formPanel.setAction(actionUrl);
		formPanel.setMethod(method);
		formPanel.submit();
	}

	private void clearMessageHandler() {
		if (messageHandler != null) {
			messageHandler.removeHandler();
			messageHandler = null;
		}
	}

	protected void initMessageHandler() {
		clearMessageHandler();
		// register to listen for the "message" events
		messageHandler = EventHandlerUtils.addEventListener("message", EventHandlerUtils.getWnd(), new JavaScriptCallback() {
			@Override
			public void invoke(JavaScriptObject event) {
				presenter.onSubmitComplete(UploaderViewImpl._getMessage(event));
				clearMessageHandler();
			}
		});
	}


	@Override
	public void clearForm() {
		usernameField.setValue("");
		passwordField.setValue("");
	}

}
