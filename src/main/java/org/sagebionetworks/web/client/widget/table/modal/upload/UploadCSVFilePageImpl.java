package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.UploadedFile;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadCSVFilePageImpl implements UploadCSVFilePage {

	public static final String NEXT = "Next";
	public static final String PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD = "Please select a CSV or TSV file to upload";
	public static final String UNKNOWN_TYPE_SLECTED = "The selected files is not of the expected type. Please select a comma-separated or tab-separated file.";
	public static final String CHOOSE_A_CSV_OR_TSV_FILE = "Choose a CSV or TSV file and then click next.";
	
	// Injected dependencies.
	FileHandleUploadWidget fileInputWidget;
	ModalPresenter presenter;
	UploadCSVPreviewPage nextPage;
	
//	 data fields
//	String fileHandleId;
	String parentId;
	String tableId;
//	ContentTypeDelimiter type;
//	String fileName;
	
	@Inject
	public UploadCSVFilePageImpl(FileHandleUploadWidget fileInputWidget, UploadCSVPreviewPage uploadCSVConfigurationWidget) {
		super();
		this.fileInputWidget = fileInputWidget;
		this.nextPage = uploadCSVConfigurationWidget;
	}

	@Override
	public void onPrimary() {
		presenter.setErrorMessage(PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD);
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
		this.fileInputWidget.configure("Browse...", new Callback() {
			@Override
			public void invoke() {
				presenter.setLoading(true);				
			}
		}, new CallbackP<UploadedFile>() {
			@Override
			public void invoke(UploadedFile uploadFile) {
				presenter.setLoading(false);	
				fileHandleCreated(uploadFile);
			}			
		});
		
	}

	@Override
	public void configure(String parentId, String tableId) {
		this.parentId = parentId;
		this.tableId = tableId;
	}

	/**
	 * Once a FileHandle is created move to the next page.
	 * @param fileHandleId
	 */
	private void fileHandleCreated(UploadedFile uploadFile) {
		FileMetadata meta = uploadFile.getFileMeta();
		String contentType = meta.getContentType();
		String fileName =  meta.getFileName();
		ContentTypeDelimiter contentTypeDelimiter = ContentTypeDelimiter.findByContentType(contentType, fileName);
		this.nextPage.configure(contentTypeDelimiter, fileName, this.parentId, uploadFile.getFileHandleId(), this.tableId);
		this.presenter.setNextActivePage(this.nextPage);
	}

}
