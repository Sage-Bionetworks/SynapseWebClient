package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl.SetAccessCallback;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import com.google.gwt.user.client.ui.IsWidget;

public interface SharingPermissionsGridView extends IsWidget, SynapseView {

	void insert(AclEntry aclEntry, int beforeIndex, PermissionLevel[] permissionLevels, Map<PermissionLevel, String> permissionDisplays, boolean deleteButtonVisible);

	void add(AclEntry aclEntry, PermissionLevel[] permissionLevels, Map<PermissionLevel, String> permissionDisplays, boolean deleteButtonVisible);

	void configure(CallbackP<Long> deleteButtonCallback, SetAccessCallback setAccessCallback);

	public interface Presenter {
	}

}
