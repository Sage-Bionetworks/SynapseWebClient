package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ErrorView extends IsWidget, SynapseView {
	
	void setPresenter(Presenter presenter);	
	void setEntry(LogEntry entry);
	void setSynAlertWidget(Widget w);
	public interface Presenter {
    }
	
}
