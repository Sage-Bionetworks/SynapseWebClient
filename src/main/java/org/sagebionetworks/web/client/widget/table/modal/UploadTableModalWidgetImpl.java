package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.PreviewUploadHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVConfigurationWidget;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic is here.
 * 
 * @author John
 *
 */
public class UploadTableModalWidgetImpl implements UploadTableModalWidget, UploadTableModalView.Presenter, FileUploadHandler, PreviewUploadHandler {
	
	private static final String PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD = "Please select a CSV or TSV file to upload";

	private static final String PLEASE_SELECT_A_COMMA_SEPARATED_OR_TAB_SEPARATED_FILE_UNKNOWN_TYPE = "Please select a comma-separated or tab-separated file.  Unknown type: ";

	public static final String CHOOSE_A_CSV_OR_TSV_FILE = "Choose a CSV or TSV file and then click next.";
	
	// injected fields
	TableCreatedHandler handler;
	UploadTableModalView view;
	FileInputWidget fileInputWidget;
	UploadCSVConfigurationWidget uploadPreviewWidget;
	
	// data fields
	String fileHandleId;
	String parentId;
	ContentTypeDelimiter type;
	String fileName;

	@Inject
	public UploadTableModalWidgetImpl(UploadTableModalView view, FileInputWidget fileInputWidget, UploadCSVConfigurationWidget uploadPreviewWidget) {
		this.view = view;
		this.fileInputWidget = fileInputWidget;
		this.uploadPreviewWidget = uploadPreviewWidget;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		// proceed if valid
		if(validateSelecedFile()){
			view.showAlert(false);
			view.setPrimaryEnabled(false);
			// Upload the file
			fileInputWidget.uploadSelectedFile();
		}
	}

	@Override
	public void configure(String parentId, TableCreatedHandler handler) {
		this.parentId = parentId;
		this.handler = handler;
	}

	@Override
	public void showModal() {
		view.setPrimaryEnabled(true);
		view.setInstructionsMessage(CHOOSE_A_CSV_OR_TSV_FILE);
		view.showAlert(false);
		fileInputWidget.configure(this);
		view.setBody(fileInputWidget);
		view.showModal();
	}

	@Override
	public void uploadSuccess(String fileHandleId) {
		this.fileHandleId = fileHandleId;
		view.setBody(uploadPreviewWidget);
		uploadPreviewWidget.configure(this.type, this.fileName, this.parentId, fileHandleId, this);
	}

	@Override
	public void uploadFailed(String error) {
		view.setPrimaryEnabled(true);
		view.showErrorMessage(error);
		view.showAlert(true);
	}

	@Override
	public void setLoading(boolean loading) {
		view.setPrimaryEnabled(!loading);
	}
	/**
	 * Validate the content type of the selected file.
	 * @return
	 */
	private boolean validateSelecedFile(){
		// Frist validate the input
		FileMetadata[] meta = fileInputWidget.getSelectedFileMetadata();
		if(meta == null || meta.length != 1){
			this.uploadFailed(PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD);
			return false;
		}
		String contentType = meta[0].getContentType();
		try{
			this.type = ContentTypeDelimiter.findByContentType(contentType);
			this.fileName =  meta[0].getFileName();
			return true;
		}catch(IllegalArgumentException e){
			this.uploadFailed(PLEASE_SELECT_A_COMMA_SEPARATED_OR_TAB_SEPARATED_FILE_UNKNOWN_TYPE+contentType);
			return false;
		}
	}

	@Override
	public void onCancel() {
		this.view.hideModal();
	}


}
