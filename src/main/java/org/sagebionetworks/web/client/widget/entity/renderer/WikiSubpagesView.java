package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpagesView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Configure the view with the parent id
	 * @param entityId
	 * @param title
	 */
	public void configure(TocItem root);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
