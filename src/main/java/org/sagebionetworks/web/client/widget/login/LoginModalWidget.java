package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentHelper;
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
	public LoginModalWidget(LoginModalView view) {
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

	/**
	 * This is only called when the servlet responds with html that does a postMessage, where the
	 * message is an UploadResult (json representation). If successful, then the servlet streams the
	 * resulting file to the browser.
	 */
	@Override
	public void onSubmitComplete(String resultHtml) {
		/**
		 * Unfortunately, gwt does not have a nice way to ask for the status code of a submit complete
		 * event. https://groups.google.com/forum/#!topic/google-web-toolkit/yuHZkiL-x5U
		 * https://groups.google.com/forum/#!topic/google-web-toolkit/v7Qi8cbp0MM
		 */
		if (resultHtml == null)
			resultHtml = "";
		// try to parse
		UploadResult uploadResult = AddAttachmentHelper.getUploadResult(resultHtml);
		onSubmitComplete(uploadResult);
	}

	public void onSubmitComplete(UploadResult uploadResult) {
		if (UploadStatus.FAILED.equals(uploadResult.getUploadStatus())) {
			view.showErrorMessagePopup(uploadResult.getMessage());
		}
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
