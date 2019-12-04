package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellEditor implements CellEditor, FileCellEditorView.Presenter {

	public static final String MUST_BE_A_FILE_ID_NUMBER = "Must be a File ID number";
	public static final String PLEASE_SELECT_A_FILE_TO_UPLOAD = "Please select a file to upload.";
	FileCellEditorView view;
	FileHandleUploadWidget fileInputWidget;
	PortalGinInjector ginInjector;

	@Inject
	public FileCellEditor(final FileCellEditorView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.view.setPresenter(this);
	}

	public void initFileInputWidget() {
		if (fileInputWidget == null) {
			fileInputWidget = ginInjector.getFileHandleUploadWidget();
			fileInputWidget.configure(WebConstants.DEFAULT_FILE_HANDLE_WIDGET_TEXT, new CallbackP<FileUpload>() {
				@Override
				public void invoke(FileUpload file) {
					view.hideErrorMessage();
					view.setValue(file.getFileHandleId());
					view.hideCollapse();
				}
			});
			this.view.addFileInputWidget(fileInputWidget);
		}
	}


	@Override
	public boolean isValid() {
		String value = StringUtils.emptyAsNull(view.getValue());
		if (value != null) {
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
		initFileInputWidget();
		view.hideErrorMessage();
		this.fileInputWidget.reset();
		view.toggleCollapse();
	}

	@Override
	public void onCancelUpload() {
		view.hideCollapse();
	}

}
