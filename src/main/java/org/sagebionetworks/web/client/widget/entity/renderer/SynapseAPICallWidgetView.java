package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseAPICallWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(Map<String, List<String>> columnData, String columnNames, String displayColumnNames, String rendererNames);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
