package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellEditorImpl implements FileCellEditor, FileCellEditorView.Presenter, FileUploadHandler{
	
	public static final String MUST_BE_A_FILE_ID_NUMBER = "Must be a File ID number";
	public static final String PLEASE_SELECT_A_FILE_TO_UPLOAD = "Please select a file to upload.";
	FileCellEditorView view;
	FileInputWidget fileInputWidget;
	
	@Inject
	public FileCellEditorImpl(FileCellEditorView view, FileInputWidget fileInputWidget){
		this.view = view;
		this.fileInputWidget = fileInputWidget;
		this.view.setPresenter(this);
		this.view.addFileInputWidget(fileInputWidget);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.trimWithEmptyAsNull(view.getValue());
		if(value != null){
			try {
				Long.parseLong(value);
			} catch (NumberFormatException e) {
				view.setValueError(MUST_BE_A_FILE_ID_NUMBER);
				return false;
			}
		}
		view.clearValueError();
		return true;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		this.view.setValue(value);
	}

	@Override
	public String getValue() {
		return this.view.getValue();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		// cannot handle for this widget
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// cannot handle for this widget
	}

	@Override
	public int getTabIndex() {
		return view.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		view.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		view.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		view.setTabIndex(index);
	}

	@Override
	public void onToggleCollapse() {
		view.hideErrorMessage();
		this.fileInputWidget.reset();
		view.resetUploadButton();
		view.toggleCollapse();
	}

	@Override
	public void onUploadFile() {
		// are any files selected?
		FileMetadata[] metaArray = this.fileInputWidget.getSelectedFileMetadata();
		if(metaArray == null || metaArray.length != 1){
			view.showErrorMessage(PLEASE_SELECT_A_FILE_TO_UPLOAD);
		}else{
			view.hideErrorMessage();
			view.setUploadButtonLoading();
			// tell the upload widget to upload the file.
			this.fileInputWidget.uploadSelectedFile(this);
		}
	}

	/**
	 * Called after the file input widget uploads a file and creates a filehandle.
	 */
	@Override
	public void uploadSuccess(String fileHandleId) {
		view.setValue(fileHandleId);
		view.hideCollapse();
	}

	@Override
	public void uploadFailed(String error) {
		view.showErrorMessage(error);
		view.resetUploadButton();
	}

	@Override
	public void onCancelUpload() {
		view.hideCollapse();
	}

}
