package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.ModalSize;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A modal dialog to handle a login.
 * 
 */
public class LoginModalWidget implements LoginModalView.Presenter, IsWidget {

	LoginModalView view;
	String action, method, encodingType;
	
	@Inject
	public LoginModalWidget(LoginModalView view){
		this.view = view;
		this.view.setPresenter(this);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		view.submitForm(action, method, encodingType);
		view.hideModal();
	}

	
	public void setLoading(boolean loading) {
		view.showAlert(false);
		view.setLoading(loading);
	}

	public void setPrimaryButtonText(String text) {
		view.setPrimaryButtonText(text);
	}

	
	public void setInstructionMessage(String message) {
		view.setInstructionsMessage(message);
	}

	public void setErrorMessage(String message) {
		view.showAlert(true);
		view.showErrorMessage(message);
		view.setLoading(false);
	}

	public void setTitle(String title) {
		view.setTitle(title);
	}

	public void setModalSize(ModalSize size) {
		view.setSize(size);
	}

	
	public void showModal() {
		view.clearForm();
		this.view.showModal();
	}
	
	public void configure(String action, String method, String encodingType) {
		this.action = action;
		this.method = method;
		this.encodingType = encodingType;
	}
}
