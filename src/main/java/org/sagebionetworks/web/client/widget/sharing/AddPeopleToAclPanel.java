package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.ListBox;
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
	
	public void configure(ListBox permissionListBox) {
		view.configure(permissionListBox);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
