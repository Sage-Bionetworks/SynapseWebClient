package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AttachmentConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void configure(WikiPageKey wikiKey, DialogCallback window);

	void setFileInputWidget(Widget fileInputWidget);

	void setWikiAttachmentsWidget(Widget widget);

	void showUploadFailureUI(String error);

	void showUploadSuccessUI(String fileName);

	void setWikiAttachmentsWidgetVisible(boolean visible);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void addFileHandleId(String fileHandleId);
	}
}
