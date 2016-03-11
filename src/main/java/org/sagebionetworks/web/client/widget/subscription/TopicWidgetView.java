package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface TopicWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setTopicText(String text);
	void setTopicHref(String href);
	void addStyleNames(String styleNames);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
