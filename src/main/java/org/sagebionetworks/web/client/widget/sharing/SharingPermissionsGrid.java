package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.users.AclEntry;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SharingPermissionsGrid implements SharingPermissionsGridView.Presenter, SynapseWidgetPresenter {

	SharingPermissionsGridView view;
	
	List<AclEntry> aclEntries;
	
	@Inject
	public SharingPermissionsGrid(SharingPermissionsGridView view) {
		this.view = view;
		aclEntries = new ArrayList<AclEntry>();
	}
	
	public void clear() {
		aclEntries.clear();
		view.clear();
	}
	
	public void configure(CallbackP<Long> deleteButtonCallback) {
		view.configure(deleteButtonCallback);
	}
	
	public AclEntry getAt(int index) {
		return aclEntries.get(index);
	}
	
	public void insert(AclEntry aclEntry, int beforeIndex, ListBox permListBox) {
		aclEntries.add(beforeIndex, aclEntry);
		view.insert(aclEntry, beforeIndex, permListBox);
	}
	
	public void add(AclEntry aclEntry, ListBox permListBox) {
		aclEntries.add(aclEntry);
		view.add(aclEntry, permListBox);
	}
	
	public int getCount() {
		return aclEntries.size();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
