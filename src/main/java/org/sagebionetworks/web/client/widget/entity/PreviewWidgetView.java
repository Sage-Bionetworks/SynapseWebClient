package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PreviewWidgetView extends IsWidget{
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setImagePreview(String fileUrl);
	public void setCodePreview(String text, String language);
	public void setTextPreview(String text);
	public void setPreviewWidget(IsWidget w);
	void addStyleName(String style);
	public void setHTML(String html);
	void showLoading();
	
	/**
	 * text must not be escaped (a regular expression will be used to split it into cells)
	 * @param text
	 * @param delimiter
	 */
	public void setTablePreview(String text, String delimiter);
	
	void addSynapseAlertWidget(IsWidget w);
	public void clear();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void imagePreviewLoadFailed(ErrorEvent e);
	}


}
