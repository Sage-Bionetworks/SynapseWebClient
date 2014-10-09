package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePage;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic is here.
 * 
 * @author John
 *
 */
public class UploadTableModalWidgetImpl implements UploadTableModalWidget, UploadTableModalView.Presenter, ModalPage.ModalPresenter {
	
	// injected fields
	UploadTableModalView view;
	UploadCSVFilePage firstPage;
	// dynamic data
	ModalPage currentPage;
	TableCreatedHandler tableCreatedHandler;

	@Inject
	public UploadTableModalWidgetImpl(UploadTableModalView view, UploadCSVFilePage uploadCSVFileWidget) {
		this.view = view;
		this.firstPage = uploadCSVFileWidget;
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
	public void configure(String parentId, TableCreatedHandler handler) {
		this.firstPage.configure(parentId);
		this.tableCreatedHandler = handler;
	}

	@Override
	public void showModal() {
		setNextActivePage(this.firstPage);
		view.showModal();
	}

	@Override
	public void onCancel() {
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
	public void onTableCreated(TableEntity table) {
		this.tableCreatedHandler.tableCreated(table);
		this.view.hideModal();
	}

}
