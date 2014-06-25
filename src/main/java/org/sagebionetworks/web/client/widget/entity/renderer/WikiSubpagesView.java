package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.FlowPanel;
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
	public void configure(TocItem root, FlowPanel wikiSubpagesContainer, FlowPanel wikiPageContainer);
	void hideSubpages();
	void showSubpages();
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
