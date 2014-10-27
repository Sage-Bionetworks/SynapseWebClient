package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.users.AclEntry;

import com.google.gwt.user.client.ui.IsWidget;

public interface SharingPermissionsGridView extends IsWidget, SynapseView {

	void insert(AclEntry aclEntry, int beforeIndex, ListBox permListBox);
	void add(AclEntry aclEntry, ListBox permListBox);
	void configure(CallbackP<Long> deleteButtonCallback);
	public interface Presenter{}
	
}
