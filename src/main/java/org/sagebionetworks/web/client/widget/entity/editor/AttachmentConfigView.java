package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface AttachmentConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(WikiPageKey wikiKey, DialogCallback window);
	void showNote(String html);
	String getFileName();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void addFileHandleId(String fileHandleId);
	}
}
