package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface DoiWidgetView extends IsWidget, SynapseWidgetView {

	void setPresenter(Presenter presenter);
	void showCreateDoi();
	void showDoi(Doi doi);
	public interface Presenter {
		public void createDoi();
	}

}
