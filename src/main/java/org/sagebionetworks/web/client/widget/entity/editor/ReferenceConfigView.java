package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface ReferenceConfigView extends IsWidget, WidgetEditorView {
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setAuthor(String author);
	public String getAuthor();
	
	public void setTitle(String title);
	public String getTitle();
	
	public void setDate(String date);
	public String getDate();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
