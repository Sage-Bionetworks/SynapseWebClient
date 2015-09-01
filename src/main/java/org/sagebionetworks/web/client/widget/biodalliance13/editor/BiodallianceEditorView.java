package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface BiodallianceEditorView extends IsWidget, WidgetEditorView {

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

}
