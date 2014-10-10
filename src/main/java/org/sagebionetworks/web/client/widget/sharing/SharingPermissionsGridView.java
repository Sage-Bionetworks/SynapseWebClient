package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.users.AclEntry;

import com.google.gwt.user.client.ui.IsWidget;

public interface SharingPermissionsGridView extends IsWidget, SynapseView {

	void insert(AclEntry aclEntry, int beforeIndex);
	void add(AclEntry aclEntry);
	
	public interface Presenter{}
	
}
