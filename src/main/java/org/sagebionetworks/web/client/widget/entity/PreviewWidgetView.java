package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface PreviewWidgetView extends IsWidget{
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setImagePreview(String fullFileUrl, String previewUrl);
	public void setCodePreview(String text);
	public void setTextPreview(String text);
	/**
	 * text must not be escaped (a regular expression will be used to split it into cells)
	 * @param text
	 * @param delimiter
	 */
	public void setTablePreview(String text, String delimiter);
	
	public void showErrorMessage(String message);
	public void clear();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}


}
