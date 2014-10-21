package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddPeopleToAclPanel implements AddPeopleToAclPanelView.Presenter, SynapseWidgetPresenter {

	private AddPeopleToAclPanelView view;
	private PermissionLevel selectedPermissionLevel;
	
	@Inject
	public AddPeopleToAclPanel(AddPeopleToAclPanelView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public UserGroupSuggestBox getSuggestBox() {
		return view.getSuggestBox();
	}
	
	public void configure(PermissionLevel[] permLevels, Map<PermissionLevel, String> permDisplay, CallbackP<Void> selectPermissionCallback, CallbackP<Void> addPersonCallback,
						CallbackP<Void> makePublicCallback, Boolean isPubliclyVisible) {
		view.configure(permLevels, permDisplay, selectPermissionCallback, addPersonCallback, makePublicCallback, isPubliclyVisible);
	}
	
	public CheckBox getNotifyPeopleCheckBox() {
		return view.getNotifyPeopleCheckBox();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setMakePublicButtonDisplay(boolean makePublic) {
		view.setMakePublicButtonDisplay(makePublic);
	}
	
	
	public PermissionLevel getSelectedPermissionLevel() {
		return selectedPermissionLevel;
	}
	
	@Override
	public void setSelectedPermissionLevel(PermissionLevel selectedPermissionLevel) {
		this.selectedPermissionLevel = selectedPermissionLevel;
	}

}
