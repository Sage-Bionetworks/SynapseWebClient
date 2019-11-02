package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.ui.IsWidget;

public interface ImageUploadView extends IsWidget, HasAttachHandlers {

	public interface Presenter {
		void onFileSelected();

		void onFileProcessed(JavaScriptObjectWrapper blob, String contentType);
	}

	void setPresenter(Presenter presenter);

	void showProgress(boolean b);

	void setInputEnabled(boolean b);

	String getInputId();

	void updateProgress(double d, String progressText);

	void setSynAlert(IsWidget w);

	void resetForm();

	void setUploadedFileText(String text);

	void processFile();
}
