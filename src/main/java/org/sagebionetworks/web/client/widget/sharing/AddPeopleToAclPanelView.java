package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.ui.IsWidget;

public interface AddPeopleToAclPanelView extends IsWidget, SynapseView{

	UserGroupSuggestBox getSuggestBox();
	
	/**
	 * Configures the panel. If called multiple times, old callbacks are
	 * deregistered from their associated components.
	 */
	void configure(PermissionLevel[] permLevels, Map<PermissionLevel, String> permDisplay, CallbackP<Void> selectPermissionCallback, CallbackP<Void> addPersonCallback,
					CallbackP<Void> makePublicCallback, Boolean isPubliclyVisible);
	
	void setPresenter(Presenter presenter);
	CheckBox getNotifyPeopleCheckBox();
	void setMakePublicButtonDisplay(boolean isPubliclyVisible);
	
	public interface Presenter {
		void setSelectedPermissionLevel(PermissionLevel selectedPermissionLevel);
	}
}
