package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface VideoWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(String mp4SynapseId, String oggSynapseId, String webmSynapseId, String width, String height, String xsrfToken);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
