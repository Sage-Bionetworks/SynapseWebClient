package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.List;

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
	
	public AclEntry getAt(int index) {
		return aclEntries.get(index);
	}
	
	public void insert(AclEntry aclEntry, int beforeIndex) {
		aclEntries.add(beforeIndex, aclEntry);
		view.insert(aclEntry, beforeIndex);
	}
	
	public void add(AclEntry aclEntry) {
		aclEntries.add(aclEntry);
		view.add(aclEntry);
	}
	
	public int getCount() {
		return aclEntries.size();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
