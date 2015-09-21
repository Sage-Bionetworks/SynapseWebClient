package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleUploadWidgetImpl implements FileHandleUploadWidget,  FileHandleUploadView.Presenter {

	private FileValidator validator;
	private FileHandleUploadView view;
	private MultipartUploader multipartUploader;
	private CallbackP<FileUpload> finishedUploadingCallback;
	private Callback startedUploadingCallback;
	private SynapseJSNIUtils synapseJsniUtils;
	
	@Inject
	public FileHandleUploadWidgetImpl(FileHandleUploadView view, MultipartUploader multipartUploader,
			SynapseJSNIUtils synapseJsniUtils) {
		super();
		this.view = view;
		this.multipartUploader = multipartUploader;
		this.synapseJsniUtils = synapseJsniUtils;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(String buttonText, CallbackP<FileUpload> finishedUploadingCallback) {
		this.finishedUploadingCallback = finishedUploadingCallback;
		view.showProgress(false);
		view.hideError();
		view.setButtonText(buttonText);
	}
	
	@Override
	public void setUploadingCallback(Callback startedUploadingCallback) {
		this.startedUploadingCallback = startedUploadingCallback;
	}
	
	@Override
	public void setUploadedFileText(String text) {
		view.setUploadedFileText(text);
	}
	
	@Override
	public void setValidation(FileValidator validator) {
		this.validator = validator;
	}
	
	@Override
	public FileMetadata[] getSelectedFileMetadata() {
		String inputId = view.getInputId();
		FileMetadata[] results = null;
		String[] fileNames = synapseJsniUtils.getMultipleUploadFileNames(inputId);
		if(fileNames != null){
			results = new FileMetadata[fileNames.length];
			for(int i=0; i<fileNames.length; i++){
				String name = fileNames[i];
				String contentType = fixDefaultContentType(synapseJsniUtils.getContentType(inputId, i), name);
				double fileSize = synapseJsniUtils.getFileSize(inputId, i);
				results[i] = new FileMetadata(name, contentType, fileSize);
			}
		}
		return results;
	}
	
	private String fixDefaultContentType(String type, String fileName) {
		String contentType = type;
		String lowercaseFilename = fileName.toLowerCase();
		if (type == null || type.trim().length() == 0) {
			if (ContentTypeUtils.isRecognizedCodeFileName(fileName)) {
				contentType = ContentTypeUtils.PLAIN_TEXT;
			}
			else if (lowercaseFilename.endsWith(".tsv") || lowercaseFilename.endsWith(".tab")) {
				contentType = WebConstants.TEXT_TAB_SEPARATED_VALUES;
			}
			else if (lowercaseFilename.endsWith(".csv")) {
				contentType = WebConstants.TEXT_COMMA_SEPARATED_VALUES;
			}
			else if (lowercaseFilename.endsWith(".txt")) {
				contentType = ContentTypeUtils.PLAIN_TEXT;
			}
		}
		return contentType;
	}
	

	@Override
	public void onFileSelected() {
		FileMetadata[] fileMetaArr = getSelectedFileMetadata();
		if (fileMetaArr != null) {
			FileMetadata fileMeta = fileMetaArr[0];
			boolean isValidUpload = validator == null || validator.isValid(fileMeta);
			if (isValidUpload) {
				if (startedUploadingCallback != null) {
					startedUploadingCallback.invoke();
				}
				view.updateProgress(1, "1%");
				view.showProgress(true);
				view.setInputEnabled(false);
				view.hideError();
				doMultipartUpload(fileMeta);		
			} else {
				Callback invalidFileCallback = validator.getInvalidFileCallback();
				if (invalidFileCallback == null) {
					String invalidMessage = validator.getInvalidMessage();
					if (invalidMessage == null)
						view.showError("Please select a valid filetype.");
					else
						view.showError(invalidMessage);	
				} else {
					invalidFileCallback.invoke();
				}
			}
		}
	}
	
	@Override
	public void reset() {
		view.setInputEnabled(true);
		view.showProgress(false);
		view.hideError();
		view.resetForm();
	}
	
	private void doMultipartUpload(final FileMetadata fileMeta) {
		// The uploader does the real work
		multipartUploader.uploadSelectedFile(view.getInputId(),
				new ProgressingFileUploadHandler() {
					@Override
					public void uploadSuccess(String fileHandleId) {
						FileUpload uploadedFile = new FileUpload(fileMeta, fileHandleId);
						// Set the view at 100%
						view.updateProgress(100, "100%");
						view.showProgress(false);
						view.setInputEnabled(true);
						finishedUploadingCallback.invoke(uploadedFile);
					}

					@Override
					public void uploadFailed(String error) {
						view.showProgress(false);
						view.setInputEnabled(true);
						view.showError(error);
					}

					@Override
					public void updateProgress(double currentProgress,
							String progressText) {
						view.updateProgress(currentProgress*100, progressText);
					}
		}, null);
	}


}
