package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
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
	
	public void configure(ListBox permissionListBox, CallbackP<Void> addPersonCallback, CallbackP<Void> makePublicCallback, Boolean isPubliclyVisible) {
		view.configure(permissionListBox, addPersonCallback, makePublicCallback, isPubliclyVisible);
	}
	
	public CheckBox getNotifyPeopleCheckBox() {
		return view.getNotifyPeopleCheckBox();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	void setMakePublicButtonDisplay(boolean makePublic) {
		view.setMakePublicButtonDisplay(makePublic);
	}

}
