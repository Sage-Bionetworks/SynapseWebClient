package org.sagebionetworks.web.client.widget.table.modal.wizard;

import org.gwtbootstrap3.client.ui.ModalSize;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The main modal dialog of a wizard.
 * 
 * @author jhill
 *
 */
public class ModalWizardWidgetImpl implements ModalWizardWidget,  ModalWizardView.Presenter, ModalPage.ModalPresenter, IsWidget {

	WizardCallback callback;
	ModalPage currentPage;
	ModalPage firstPage;
	ModalWizardView view;
	
	@Inject
	public ModalWizardWidgetImpl(ModalWizardView view){
		this.view = view;
		this.view.setPresenter(this);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		// pass this to the current page
		this.currentPage.onPrimary();
	}

	@Override
	public void onCancel() {
		if(this.callback != null){
			this.callback.onCanceled();
		}
		this.view.hideModal();
	}

	@Override
	public void setNextActivePage(ModalPage next) {
		this.currentPage = next;
		this.currentPage.setModalPresenter(this);
		// add the page to the dialog.
		this.view.setBody(this.currentPage);
		this.setLoading(false);
	}

	@Override
	public void setLoading(boolean loading) {
		view.showAlert(false);
		view.setLoading(loading);
	}

	@Override
	public void setPrimaryButtonText(String text) {
		view.setPrimaryButtonText(text);
	}

	@Override
	public void setInstructionMessage(String message) {
		view.setInstructionsMessage(message);
	}

	@Override
	public void setErrorMessage(String message) {
		view.showAlert(true);
		view.showErrorMessage(message);
		view.setLoading(false);
	}

	@Override
	public void onFinished() {
		if(callback != null){
			this.callback.onFinished();
		}
		this.view.hideModal();
	}

	@Override
	public void setTitle(String title) {
		view.setTile(title);
	}

	@Override
	public void setModalSize(ModalSize size) {
		view.setSize(size);
	}

	@Override
	public void showModal(WizardCallback callback) {
		setNextActivePage(this.firstPage);
		this.callback = callback;
		this.view.showModal();
	}

	@Override
	public void configure(ModalPage firstPage) {
		this.firstPage = firstPage;
	}
	
	@Override
	public void setHelp(String helpMarkdown, String helpUrl) {
		view.setHelp(helpMarkdown, helpUrl);
	}

}
