package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface VideoConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setEntity(String entity);
	String getEntity();
	void hideFinder();
	void setVideoFormatWarningVisible(boolean visible);
	void showFinderError(String error);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void validateSelection(Reference ref);
	}

}
