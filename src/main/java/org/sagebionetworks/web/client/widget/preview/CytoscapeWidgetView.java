package org.sagebionetworks.web.client.widget.preview;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface CytoscapeWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void configure(String graphJson);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
