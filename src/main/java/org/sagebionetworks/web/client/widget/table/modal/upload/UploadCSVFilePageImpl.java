package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadCSVFilePageImpl implements UploadCSVFilePage {

	public static final String NEXT = "Next";
	public static final String PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD = "Please select a CSV or TSV file to upload";
	public static final String UNKNOWN_TYPE_SLECTED = "The selected files is not of the expected type. Please select a comma-separated or tab-separated file.";
	public static final String CHOOSE_A_CSV_OR_TSV_FILE = "Choose a CSV or TSV file and then click next.";
	
	// Injected dependencies.
	FileInputWidget fileInputWidget;
	ModalPresenter presenter;
	UploadCSVConfigurationPage nextPage;
	
	// data fields
	String fileHandleId;
	String parentId;
	ContentTypeDelimiter type;
	String fileName;
	
	@Inject
	public UploadCSVFilePageImpl(FileInputWidget fileInputWidget, UploadCSVConfigurationPage uploadCSVConfigurationWidget) {
		super();
		this.fileInputWidget = fileInputWidget;
		this.nextPage = uploadCSVConfigurationWidget;
	}

	@Override
	public void onPrimary() {
		// proceed if valid
		if(validateSelecedFile()){
			presenter.setLoading(true);
			// Upload the file
			fileInputWidget.uploadSelectedFile(new FileUploadHandler() {
				
				@Override
				public void uploadSuccess(String fileHandleId) {
					fileHandleCreated(fileHandleId);
				}
				
				@Override
				public void uploadFailed(String error) {
					presenter.setErrorMessage(error);
				}
			});
		}
	}

	@Override
	public Widget asWidget() {
		return fileInputWidget.asWidget();
	}

	@Override
	public void setModalPresenter(final ModalPresenter presenter) {
		this.presenter = presenter;
		this.presenter.setInstructionMessage(CHOOSE_A_CSV_OR_TSV_FILE);
		this.presenter.setPrimaryButtonText(NEXT);
		this.fileInputWidget.reset();
	}

	/**
	 * Validate the content type of the selected file.
	 * @return
	 */
	public boolean validateSelecedFile(){
		// Frist validate the input
		FileMetadata[] meta = fileInputWidget.getSelectedFileMetadata();
		if(meta == null || meta.length != 1){
			presenter.setErrorMessage(PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD);
			return false;
		}
		String contentType = meta[0].getContentType();
		try{
			this.type = ContentTypeDelimiter.findByContentType(contentType);
			this.fileName =  meta[0].getFileName();
			return true;
		}catch(IllegalArgumentException e){
			presenter.setErrorMessage(UNKNOWN_TYPE_SLECTED);
			return false;
		}
	}

	@Override
	public void configure(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * Once a FileHandle is created move to the next page.
	 * @param fileHandleId
	 */
	private void fileHandleCreated(String fileHandleId) {
		this.nextPage.configure(this.type, this.fileName, this.parentId, fileHandleId);
		this.presenter.setNextActivePage(this.nextPage);
	}

}
