package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface WizardProgressWidgetView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void configure(int current, int total);

	public interface Presenter {
		void configure(int current, int total);
	}
}
