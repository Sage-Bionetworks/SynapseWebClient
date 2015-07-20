package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface IFrameConfigView extends IsWidget, WidgetEditorView {

	public void setVideoUrl(String url);
	public String getVideoUrl();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
