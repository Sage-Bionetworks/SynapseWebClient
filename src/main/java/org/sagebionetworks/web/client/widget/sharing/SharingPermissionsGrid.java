package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl.SetAccessCallback;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SharingPermissionsGrid implements SharingPermissionsGridView.Presenter, SynapseWidgetPresenter {

	private SharingPermissionsGridView view;
	
	private List<AclEntry> aclEntries;
	
	@Inject
	public SharingPermissionsGrid(SharingPermissionsGridView view) {
		this.view = view;
		aclEntries = new ArrayList<AclEntry>();
	}
	
	public void clear() {
		aclEntries.clear();
		view.clear();
	}
	
	/**
	 * @param deleteButtonCallback Callback for removing a user from
	 * the ACL. If null, the table will not be editable.
	 */
	public void configure(CallbackP<Long> deleteButtonCallback, SetAccessCallback setAccessCallback) {
		view.configure(deleteButtonCallback, setAccessCallback);
	}
	
	public AclEntry getAt(int index) {
		return aclEntries.get(index);
	}
	
	public void insert(AclEntry aclEntry, int beforeIndex, PermissionLevel[] permissionLevels, Map<PermissionLevel, String> permissionDisplay, boolean deleteButtonVisible) {
		aclEntries.add(beforeIndex, aclEntry);
		view.insert(aclEntry, beforeIndex, permissionLevels, permissionDisplay, deleteButtonVisible);
	}
	
	public void add(AclEntry aclEntry, PermissionLevel[] permissionLevels, Map<PermissionLevel, String> permissionDisplay) {
		aclEntries.add(aclEntry);
		view.add(aclEntry, permissionLevels, permissionDisplay);
	}
	
	public int getCount() {
		return aclEntries.size();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
