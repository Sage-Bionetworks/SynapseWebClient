package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetViewImpl.UserGroupListWidgetViewImplUiBinder;
import org.sagebionetworks.web.shared.users.AclEntry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SharingPermissionsGridViewImpl extends Composite implements SharingPermissionsGridView {
	public interface SharingPermissionsGridViewImplUiBinder extends UiBinder<Widget, SharingPermissionsGridViewImpl> {};
	
	@UiField 
	TBody tableBody;
	
	@Inject
	public SharingPermissionsGridViewImpl(SharingPermissionsGridViewImplUiBinder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void insert(AclEntry aclEntry, int beforeIndex) {
		tableBody.insert(createAclEntryTableRow(aclEntry), beforeIndex);
	}
	
	@Override
	public void add(AclEntry aclEntry) {
		tableBody.add(createAclEntryTableRow(aclEntry));
	}
	
	private TableRow createAclEntryTableRow(AclEntry aclEntry) {
		TableRow row = new TableRow();
		
		// Poeple label
		TableData data = new TableData();
		data.add(new Label(aclEntry.getTitle()));
		row.add(data);
		
		// Access list
		data = new TableData();
		ListBox listBox = getAccessListBox();
		if (aclEntry.isOwner())
			listBox.setEnabled(false);
		data.add(listBox);
		row.add(data);
		
		// Delete button
		data = new TableData();
		Button button = new Button("", IconType.SEARCH, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Window.alert("THIS WILL DELETE!!");
			}
			
		});	// TODO: IconType.REMOVE??
		if (aclEntry.isOwner()) {
			button.setEnabled(false);
		}
		data.add(button);
		row.add(data);;
		
		return row;
	}
	
	private ListBox getAccessListBox() {
		ListBox listBox = new ListBox();
		listBox.addItem("TEST 1");	// TODO: Change
		listBox.addItem("TEST 2");
		listBox.addItem("TEST 3");
		
		return listBox;
	}
	
	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		tableBody.clear();
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
}
