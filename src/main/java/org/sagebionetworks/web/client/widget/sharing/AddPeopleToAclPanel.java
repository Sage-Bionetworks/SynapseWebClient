package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddPeopleToAclPanel implements AddPeopleToAclPanelView.Presenter, SynapseWidgetPresenter {

	private AddPeopleToAclPanelView view;
	
	@Inject
	public AddPeopleToAclPanel(AddPeopleToAclPanelView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public UserGroupSuggestBox getSuggestBox() {
		return view.getSuggestBox();
	}
	
	public void configure(ListBox permissionListBox, CallbackP<Void> addPersonCallback) {
		view.configure(permissionListBox, addPersonCallback);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
