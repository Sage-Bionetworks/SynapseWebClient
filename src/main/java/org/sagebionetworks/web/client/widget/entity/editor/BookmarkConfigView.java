package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface BookmarkConfigView extends IsWidget, WidgetEditorView {
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setLinkText(String linkText);
	public String getLinkText();
	
	public void setTargetId(String targetId);
	public String getTargetId();
	
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}

}
