package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.user.client.ui.IsWidget;

public interface PreviewWidgetView extends IsWidget {
	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setImagePreview(String fileUrl);

	public void setCodePreview(String text, String language);

	public void setTextPreview(String text);

	public void setPreviewWidget(IsWidget w);

	void addStyleName(String style);

	void showLoading();

	void showNoPreviewAvailable(String entityId, Long version);

	/**
	 * @param text
	 */
	public void setTablePreview(ArrayList<String[]> rows);

	void addSynapseAlertWidget(IsWidget w);

	public void clear();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void imagePreviewLoadFailed(ErrorEvent e);
	}


}
