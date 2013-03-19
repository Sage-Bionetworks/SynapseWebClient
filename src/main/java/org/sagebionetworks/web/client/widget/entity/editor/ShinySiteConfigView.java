package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants.MarkdownWidthParam;

import com.google.gwt.user.client.ui.IsWidget;

public interface ShinySiteConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(String url, int width, int height);
	public String getSiteUrl();
	public MarkdownWidthParam getSiteWidth();
	public Integer getSiteHeight();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
