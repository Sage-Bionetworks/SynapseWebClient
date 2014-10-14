package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface AddPeopleToAclPanelView extends IsWidget, SynapseView {

	
	void setPresenter(Presenter presenter);
	
	public interface Presenter {}
}
