package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface LinkConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

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
