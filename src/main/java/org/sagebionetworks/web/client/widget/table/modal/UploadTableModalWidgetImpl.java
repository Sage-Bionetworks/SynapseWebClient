package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic is here.
 * 
 * @author John
 *
 */
public class UploadTableModalWidgetImpl implements UploadTableModalWidget, UploadTableModalView.Presenter, FileUploadHandler {
	
	private static final String CHOOSE_A_CSV_OR_TSV_FILE = "Choose a CSV or TSV file and then click next.";
	String parentId;
	TableCreatedHandler handler;
	String fileHandleId;
	UploadTableModalView view;
	FileInputWidget fileInputWidget;

	@Inject
	public UploadTableModalWidgetImpl(UploadTableModalView view, FileInputWidget fileInputWidget) {
		this.view = view;
		this.fileInputWidget = fileInputWidget;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		view.setErrorVisible(false);
		view.setPrimaryEnabled(false);
		// Upload the file
		fileInputWidget.uploadSelectedFile();
	}

	@Override
	public void configure(String parentId, TableCreatedHandler handler) {
		this.parentId = parentId;
		this.handler = handler;
	}

	@Override
	public void showModal() {
		view.setPrimaryEnabled(true);
		view.setInstructionsVisible(true);
		view.setInstructionsMessage(CHOOSE_A_CSV_OR_TSV_FILE);
		view.setErrorVisible(false);
		fileInputWidget.configure(this);
		view.setBody(fileInputWidget);
		view.showModal();
	}

	@Override
	public void uploadSuccess(String fileHandleId) {
		view.setPrimaryEnabled(true);
		view.showError("Uploaded file handle: "+fileHandleId);
		view.setErrorVisible(true);
	}

	@Override
	public void uploadFailed(String error) {
		view.setPrimaryEnabled(true);
		view.showError(error);
		view.setErrorVisible(true);
	}


}
