package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface ButtonLinkConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor);
	public void setLinkUrl(String url);
	public String getLinkUrl();
	
	public void setName(String name);
	public String getName();
	
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
