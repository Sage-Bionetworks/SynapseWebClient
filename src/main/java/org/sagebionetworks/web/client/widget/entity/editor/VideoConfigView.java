package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface VideoConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setMp4Entity(String mp4Entity);
	String getMp4Entity();
	void setOggEntity(String mp4Entity);
	String getOggEntity();
	
	void setWebMEntity(String mp4Entity);
	String getWebMEntity();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}

}
