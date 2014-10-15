package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;

import com.google.gwt.user.client.ui.IsWidget;

public interface AddPeopleToAclPanelView extends IsWidget, SynapseView {

	UserGroupSuggestBox getSuggestBox();
	void configure(ListBox permissionListBox, CallbackP<Void> addPersonCallback);
	
	void setPresenter(Presenter presenter);
	
	public interface Presenter {}
}
