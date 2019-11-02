package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleUploadWidgetImpl implements FileHandleUploadWidget, FileHandleUploadView.Presenter {

	private FileValidator validator;
	private FileHandleUploadView view;
	private MultipartUploader multipartUploader;
	private CallbackP<FileUpload> finishedUploadingCallback;
	private Callback startedUploadingCallback;
	private SynapseJSNIUtils synapseJsniUtils;
	private int count;
	private FileMetadata[] fileMetaArr;
	private JavaScriptObject fileList;

	@Inject
	public FileHandleUploadWidgetImpl(FileHandleUploadView view, MultipartUploader multipartUploader, SynapseJSNIUtils synapseJsniUtils) {
		super();
		this.view = view;
		this.multipartUploader = multipartUploader;
		this.synapseJsniUtils = synapseJsniUtils;
		this.view.setPresenter(this);
		this.view.allowMultipleFileUpload(false);
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
	public void allowMultipleFileUpload(boolean value) {
		this.view.allowMultipleFileUpload(value);
	}

	@Override
	public FileMetadata[] getSelectedFileMetadata() {
		String inputId = view.getInputId();
		FileMetadata[] results = null;
		fileList = synapseJsniUtils.getFileList(inputId);
		String[] fileNames = synapseJsniUtils.getMultipleUploadFileNames(fileList);
		if (fileNames != null) {
			results = new FileMetadata[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				String name = fileNames[i];
				String contentType = org.sagebionetworks.web.client.ContentTypeUtils.fixDefaultContentType(synapseJsniUtils.getContentType(fileList, i), name);
				double fileSize = synapseJsniUtils.getFileSize(synapseJsniUtils.getFileBlob(i, fileList));
				results[i] = new FileMetadata(name, contentType, fileSize);
			}
		}
		return results;
	}

	@Override
	public void onFileSelected() {
		fileMetaArr = getSelectedFileMetadata();
		if (fileMetaArr != null) {
			FileMetadata fileMeta = fileMetaArr[0];
			boolean isValidUpload = validator == null || validator.isValid(fileMeta);
			boolean isValidFilename = AbstractFileValidator.isValidFilename(fileMeta.getFileName());
			if (!isValidFilename) {
				view.showError(WebConstants.INVALID_ENTITY_NAME_MESSAGE);
				if (validator != null && validator.getInvalidFileCallback() != null) {
					validator.getInvalidFileCallback().invoke();
				}
			} else if (isValidUpload) {
				if (startedUploadingCallback != null) {
					startedUploadingCallback.invoke();
				}
				view.updateProgress(1, "1%");
				view.showProgress(true);
				view.setInputEnabled(false);
				view.hideError();
				beginMultiFileUpload();
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

	private void beginMultiFileUpload() {
		count = 0;
		doMultipartUpload();
	}

	private void uploadNext() {
		count++;
		if (count != fileMetaArr.length) {
			int progress = count * 100 / fileMetaArr.length;
			view.updateProgress(progress, progress + "%");
			doMultipartUpload();
		} else {
			// Set the view at 100%
			view.updateProgress(100, "100%");
			view.showProgress(false);
			view.setInputEnabled(true);
		}
	}


	private void doMultipartUpload() {
		// The uploader does the real work
		JavaScriptObject blob = synapseJsniUtils.getFileBlob(count, fileList);
		FileMetadata fileMetadata = fileMetaArr[count];

		multipartUploader.uploadFile(fileMetadata.getFileName(), fileMetadata.getContentType(), blob, new ProgressingFileUploadHandler() {
			@Override
			public void uploadSuccess(String fileHandleId) {
				FileUpload uploadedFile = new FileUpload(fileMetaArr[count], fileHandleId);
				finishedUploadingCallback.invoke(uploadedFile);
				uploadNext();
			}

			@Override
			public void uploadFailed(String error) {
				view.showProgress(false);
				view.setInputEnabled(true);
				view.showError(error);
			}

			@Override
			public void updateProgress(double currentProgress, String progressText, String uploadSpeed) {
				int totalProgress = count * 100 / fileMetaArr.length + (int) (currentProgress * 100 / fileMetaArr.length);
				view.updateProgress(totalProgress, totalProgress + "%");
			}
		}, null, view);
	}

}
