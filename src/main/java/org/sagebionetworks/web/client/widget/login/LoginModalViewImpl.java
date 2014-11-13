package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginModalViewImpl implements LoginModalView {

	public interface Binder extends UiBinder<Modal, LoginModalViewImpl> {}
	
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
	
	@Inject
	public LoginModalViewImpl(Binder binder){
		modal = binder.createAndBindUi(this);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
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
	public void setLoading(boolean loading) {
		if(!loading){
			this.primaryButton.state().reset();
		}else{
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
		if (encodingType != null)
			FormElement.as(formPanel.getElement()).setEnctype(encodingType);
		formPanel.setAction(actionUrl);
		formPanel.setMethod(method);
		formPanel.submit();
	}
	
	@Override
	public void clearForm() {
		usernameField.setValue("");
		passwordField.setValue("");
	}
	
}
