package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface DownView extends IsWidget, SynapseView {
	
	void setPresenter(Presenter loginPresenter);	
	public interface Presenter {
    }
	
}
