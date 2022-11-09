package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl.SetAccessCallback;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

public class SharingPermissionsGridViewImpl
  extends Composite
  implements SharingPermissionsGridView {

  public interface SharingPermissionsGridViewImplUiBinder
    extends UiBinder<Widget, SharingPermissionsGridViewImpl> {}

  private CallbackP<Long> deleteButtonCallback;
  private SetAccessCallback setAccessCallback;

  @UiField
  TBody tableBody;

  @UiField
  TableHeader permissionColumnHeader;

  @UiField
  TableHeader deleteColumnHeader;

  private PortalGinInjector ginInjector;
  private String publicAclPrincipalId;
  private boolean isOpenData;

  @Inject
  public SharingPermissionsGridViewImpl(
    SharingPermissionsGridViewImplUiBinder uiBinder,
    PortalGinInjector ginInjector
  ) {
    initWidget(uiBinder.createAndBindUi(this));
    this.ginInjector = ginInjector;
    publicAclPrincipalId =
      ginInjector
        .getSynapseProperties()
        .getSynapseProperty(WebConstants.PUBLIC_ACL_PRINCIPAL_ID);
  }

  @Override
  public void insert(
    AclEntry aclEntry,
    int beforeIndex,
    PermissionLevel[] permissionLevels,
    Map<PermissionLevel, String> permissionDisplays,
    boolean deleteButtonVisible
  ) {
    tableBody.insert(
      createAclEntryTableRow(
        aclEntry,
        permissionLevels,
        permissionDisplays,
        deleteButtonVisible
      ),
      beforeIndex
    );
  }

  @Override
  public void configure(
    CallbackP<Long> deleteButtonCallback,
    SetAccessCallback setAccessCallback,
    boolean isOpenData
  ) {
    this.deleteButtonCallback = deleteButtonCallback;
    this.setAccessCallback = setAccessCallback;
    this.isOpenData = isOpenData;
  }

  @Override
  public void add(
    AclEntry aclEntry,
    PermissionLevel[] permissionLevels,
    Map<PermissionLevel, String> permissionDisplay,
    boolean deleteButtonVisible
  ) {
    tableBody.add(
      createAclEntryTableRow(
        aclEntry,
        permissionLevels,
        permissionDisplay,
        deleteButtonVisible
      )
    );
  }

  private TableRow createAclEntryTableRow(
    final AclEntry aclEntry,
    PermissionLevel[] permissionLevels,
    Map<PermissionLevel, String> permissionDisplay,
    boolean deleteButtonVisible
  ) {
    final TableRow row = new TableRow();

    // People label
    TableData data = new TableData();
    // data.add(new Label(aclEntry.getTitle()));
    Widget badgeWidget;
    if (aclEntry.isIndividual()) {
      UserBadge badge = ginInjector.getUserBadgeWidget();
      badge.configure(aclEntry.getOwnerId());
      badge.setOpenInNewWindow();
      badgeWidget = badge.asWidget();
    } else {
      TeamBadge badge = ginInjector.getTeamBadgeWidget();
      badge.configure(aclEntry.getOwnerId(), aclEntry.getTitle());
      badge.setOpenNewWindow(true);
      badgeWidget = badge.asWidget();
    }
    data.add(badgeWidget);
    row.add(data);

    // Permissions List Box

    ListBox permListBox = createEditAccessListBox(
      aclEntry,
      permissionLevels,
      permissionDisplay
    );
    permListBox.addStyleName("input-xs");
    data = new TableData();
    row.add(data);
    boolean isDeleteButtonAvailable =
      deleteButtonVisible && deleteButtonCallback != null;
    Widget permissionWidget;

    if (aclEntry.getOwnerId().equals(publicAclPrincipalId)) {
      // SWC-6314 (and SWC-6083): If this is the public group, render Can Download if OPEN_DATA, otherwise Can View.
      if (isOpenData) {
        permissionWidget =
          new Text(permissionDisplay.get(PermissionLevel.CAN_DOWNLOAD));
      } else {
        permissionWidget = new Text(permListBox.getSelectedItemText());
      }
    } else if (isDeleteButtonAvailable) {
      permissionWidget = permListBox;
    } else {
      // Don't allow editing the permissions and don't add delete button.
      permissionWidget = new Text(permListBox.getSelectedItemText());
    }

    data.add(permissionWidget);

    if (isDeleteButtonAvailable) {
      // Add delete button and size columns.
      TableData deleteButtonContainer = new TableData();
      Anchor deleteButton = new Anchor();
      deleteButton.setIcon(IconType.TIMES);
      deleteButton.addClickHandler(event -> {
        tableBody.remove(row);
        deleteButtonCallback.invoke(Long.parseLong(aclEntry.getOwnerId()));
      });
      deleteButtonContainer.add(deleteButton);
      row.add(deleteButtonContainer);

      permissionColumnHeader.addStyleName("col-md-2");
      deleteColumnHeader.addStyleName("col-md-1");
    } else {
      permissionColumnHeader.setStyleName("col-md-3");
    }

    return row;
  }

  private ListBox createEditAccessListBox(
    final AclEntry aclEntry,
    final PermissionLevel[] permissionLevels,
    Map<PermissionLevel, String> permissionDisplay
  ) {
    final Long principalId = Long.parseLong(aclEntry.getOwnerId());

    final ListBox listBox = new ListBox();

    PermissionLevel permLevel = AclUtils.getPermissionLevel(
      new HashSet<ACCESS_TYPE>(aclEntry.getAccessTypes())
    );
    boolean foundMatchingPermissionLevel = false;
    for (int i = 0; i < permissionLevels.length; i++) {
      listBox.addItem(permissionDisplay.get(permissionLevels[i]));
      if (permissionLevels[i].equals(permLevel)) {
        foundMatchingPermissionLevel = true;
        listBox.setSelectedIndex(i);
      }
    }
    if (!foundMatchingPermissionLevel) {
      listBox.addItem("Custom");
      listBox.setSelectedIndex(listBox.getItemCount() - 1);
    }

    listBox.addChangeHandler(
      new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          if (
            setAccessCallback != null &&
            listBox.getSelectedIndex() < permissionLevels.length
          ) setAccessCallback.invoke(
            principalId,
            permissionLevels[listBox.getSelectedIndex()]
          );
        }
      }
    );

    return listBox;
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
  public void showLoading() {}

  @Override
  public void showInfo(String message) {}

  @Override
  public void showErrorMessage(String message) {}
}
