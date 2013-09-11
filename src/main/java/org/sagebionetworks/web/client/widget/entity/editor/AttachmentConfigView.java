package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.extjs.gxt.ui.client.widget.Dialog;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface AttachmentConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public String getUploadedFileHandleName();
	public void setUploadedFileHandleName(String fileHandleName);
	
	public void configure(WikiPageKey wikiKey, Dialog window);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
