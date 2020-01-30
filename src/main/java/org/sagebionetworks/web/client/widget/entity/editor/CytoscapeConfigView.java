package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface CytoscapeConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void setEntity(String entityId);

	String getEntity();

	void setStyleEntity(String entityId);

	String getStyleEntity();

	void setHeight(String height);

	String getHeight();

	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}

}
