package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetViewImpl.UserGroupListWidgetViewImplUiBinder;
import org.sagebionetworks.web.shared.users.AclEntry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SharingPermissionsGridViewImpl extends Composite implements SharingPermissionsGridView {
	public interface SharingPermissionsGridViewImplUiBinder extends UiBinder<Widget, SharingPermissionsGridViewImpl> {};
	
	private static final int PERMISSION_COLUMN_WIDTH_PERCENTAGE = 27;
	private static final int DELETE_COLUMN_WIDTH_PERCENTAGE = 5;
	
	CallbackP<Long> deleteButtonCallback;
	
	@UiField 
	TBody tableBody;
	@UiField
	TableHeader permissionColumnHeader;
	@UiField
	TableHeader deleteColumnHeader;
	
	private PortalGinInjector ginInjector;
	
	@Inject
	public SharingPermissionsGridViewImpl(SharingPermissionsGridViewImplUiBinder uiBinder, PortalGinInjector ginInjector) {
		initWidget(uiBinder.createAndBindUi(this));
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void insert(AclEntry aclEntry, int beforeIndex, ListBox permListBox) {
		tableBody.insert(createAclEntryTableRow(aclEntry, permListBox), beforeIndex);
	}
	
	@Override
	public void configure(CallbackP<Long> deleteButtonCallback) {
		this.deleteButtonCallback = deleteButtonCallback;
	}
	
	@Override
	public void add(AclEntry aclEntry, ListBox permListBox) {
		tableBody.add(createAclEntryTableRow(aclEntry, permListBox));
	}
	
	private TableRow createAclEntryTableRow(final AclEntry aclEntry, ListBox permListBox) {
		final TableRow row = new TableRow();
		
		// People label
		TableData data = new TableData();
		//data.add(new Label(aclEntry.getTitle()));
		Widget badgeWidget;
		if (aclEntry.isIndividual()) {
			UserBadge badge = ginInjector.getUserBadgeWidget();
			badge.configure(aclEntry.getOwnerId());
			badgeWidget = badge.asWidget();
		} else {
			TeamBadge badge = ginInjector.getTeamBadgeWidget();
			badge.configure(aclEntry.getOwnerId(), aclEntry.getTitle());
			badgeWidget = badge.asWidget();
		}
		data.add(badgeWidget);
		row.add(data);
		
		// Permissions List Box
		data = new TableData();
		permListBox.addStyleName("input-xs");
		data.add(permListBox);
		row.add(data);
		
		if (deleteButtonCallback == null) {
			// Don't allow editing the permissions and don't add delete button.
			permListBox.setEnabled(false);
			permissionColumnHeader.setWidth(PERMISSION_COLUMN_WIDTH_PERCENTAGE + DELETE_COLUMN_WIDTH_PERCENTAGE + "%");
			deleteColumnHeader.setWidth("0%");
		} else {
			// Add delete button and size columns.
			data = new TableData();
			Button button = new Button("", new ClickHandler() {
	
				@Override
				public void onClick(ClickEvent event) {
					tableBody.remove(row);
					deleteButtonCallback.invoke(Long.parseLong(aclEntry.getOwnerId()));
				}
				
			});
			button.setSize(ButtonSize.SMALL);
			button.addStyleName("glyphicon glyphicon-remove");
			Icon icon = new Icon();
			if (aclEntry.isOwner()) {
				button.setEnabled(false);
			}
			button.setType(ButtonType.DANGER);
			data.add(button);
			row.add(data);
			
			permissionColumnHeader.setWidth(PERMISSION_COLUMN_WIDTH_PERCENTAGE + "%");
			deleteColumnHeader.setWidth(DELETE_COLUMN_WIDTH_PERCENTAGE + "%");
		}
		return row;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void clear() {
		tableBody.clear();
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}
}
