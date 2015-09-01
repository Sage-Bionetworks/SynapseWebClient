package org.sagebionetworks.web.client.widget.biodalliance13;


import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface BiodallianceSourceView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
	
	Reference getEntity();
	void setEntity(String entityId, Long version);

	String getColor();
	void setColor(String color);

	String getHeight();
	void setHeight(String height);
	
	String getSourceName();
	void setSourceName(String sourceName);
	
}
