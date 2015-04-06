package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListEditorViewImpl extends FlowPanel implements AccessControlListEditorView {
	
	private static final String CANNOT_MODIFY_ACL_TEXT = "You do not have sufficient privileges to modify the sharing settings.";
	
	private Presenter presenter;
	private Map<PermissionLevel, String> permissionDisplay;
	private SageImageBundle sageImageBundle;
	private Long publicAclPrincipalId;
	private Boolean isPubliclyVisible;
	private boolean showEditColumns;
	
	private SharingPermissionsGrid permissionsGrid;
	
	private AclAddPeoplePanel addPeoplePanel;
	
	private PermissionLevel[] permList;	// To enforce order.
	private Button deleteAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_DELETE_ACL);
	private Tooltip toolTipAndDeleteAclButton;
	
	@Inject
	public AccessControlListEditorViewImpl(SageImageBundle sageImageBundle,
					SharingPermissionsGrid permissionsGrid, AclAddPeoplePanel addPeoplePanel) {
		this.sageImageBundle = sageImageBundle;
		this.permissionsGrid = permissionsGrid;
		this.addPeoplePanel = addPeoplePanel;
		
		// 'Delete ACL' button
		deleteAclButton.setType(ButtonType.DANGER);
		deleteAclButton.setSize(ButtonSize.EXTRA_SMALL);
		deleteAclButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				presenter.deleteAcl();					
			}
		});
			
		toolTipAndDeleteAclButton = new Tooltip();
		toolTipAndDeleteAclButton.setWidget(deleteAclButton);
		toolTipAndDeleteAclButton.setTitle(DisplayConstants.PERMISSIONS_DELETE_ACL_TEXT);
		toolTipAndDeleteAclButton.setPlacement(Placement.BOTTOM);
	}
	
	public void setPermissionsToDisplay(PermissionLevel[] permList, Map<PermissionLevel, String> permissionsDisplay) {
		this.permList = permList;
		this.permissionDisplay = permissionsDisplay;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void addAclEntry(AclEntry aclEntry) {
		if (permissionsGrid == null)
			throw new IllegalStateException("Permissions window has not been built yet");
		if (!aclEntry.isIndividual()) {
			permissionsGrid.insert(aclEntry, 0, permList, permissionDisplay); // insert groups first
		} else if (aclEntry.isOwner()) {
			//owner should be the first (after groups, if present)
			int insertIndex = 0;
			for (; insertIndex < permissionsGrid.getCount(); insertIndex++) {
				if (permissionsGrid.getAt(insertIndex).isIndividual())
					break;
			}
			permissionsGrid.insert(aclEntry, insertIndex, permList, permissionDisplay); // insert owner
		}
		else
			permissionsGrid.add(aclEntry, permList, permissionDisplay);
	}
	
	@Override
	public void setPublicAclPrincipalId(Long publicAclPrincipalId) {
		this.publicAclPrincipalId = publicAclPrincipalId;
	}
	
	@Override
	public void setIsPubliclyVisible(Boolean isPubliclyVisible) {
		this.isPubliclyVisible = isPubliclyVisible;
		if (isPubliclyVisible != null) {
			addPeoplePanel.setMakePublicButtonDisplay(isPubliclyVisible);
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		permissionsGrid.clear();
	}
	
	@Override
	public void buildWindow(boolean isInherited, boolean canEnableInheritance, boolean canChangePermission) {		
		clear();
		
		// Display Permissions grid.
		showEditColumns = canChangePermission && !isInherited;
		CallbackP<Long> removeUserCallback = null;
		if (showEditColumns) {
			removeUserCallback = new CallbackP<Long>() {
					@Override
					public void invoke(Long principalId) {
						presenter.removeAccess(principalId);
					}
				};
		}
		
		SetAccessCallback setAccessCallback = new SetAccessCallback() {
			@Override
			public void invoke(Long principalId, PermissionLevel permissionLevel) {
				presenter.setAccess(principalId, permissionLevel);
			}
		};
		
		permissionsGrid.configure(removeUserCallback, setAccessCallback);
		add(permissionsGrid.asWidget());
		
		if (!canChangePermission) {
			// Inform user of restricted privileges.
			Label canNotModify = new Label();
			canNotModify.setText(CANNOT_MODIFY_ACL_TEXT);
			add(canNotModify);
		} else {
			if(isInherited) {
				// Notify user of inherited sharing settings.
				Label readOnly = new Label(DisplayConstants.PERMISSIONS_INHERITED_TEXT);
				add(readOnly);
				
				// 'Create ACL' button
				Button createAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_CREATE_NEW_ACL, IconType.PLUS, new ClickHandler() {
	
					@Override
					public void onClick(ClickEvent event) {
						presenter.createAcl();
					}
					
				});
				createAclButton.setType(ButtonType.SUCCESS);
				createAclButton.addStyleName("margin-top-10");
				
				Tooltip toolTipAndCreateAclButton = new Tooltip();
				toolTipAndCreateAclButton.setWidget(createAclButton);
				toolTipAndCreateAclButton.setTitle(DisplayConstants.PERMISSIONS_CREATE_NEW_ACL_TEXT);
				toolTipAndCreateAclButton.setPlacement(Placement.BOTTOM);
				add(toolTipAndCreateAclButton);
			} else {
				// Configure AddPeopleToAclPanel.
				CallbackP<Void> selectPermissionCallback = new CallbackP<Void>() {
					@Override
					public void invoke(Void param) {
						presenter.setUnsavedViewChanges(true);
					}
				};
				
				CallbackP<Void> addPersonCallback = new CallbackP<Void>() {
					@Override
					public void invoke(Void param) {
						addPersonToAcl();
					}
				};
				
				CallbackP<Void> makePublicCallback = new CallbackP<Void>() {
					@Override
					public void invoke(Void param) {
						// Add the ability for PUBLIC to see this entity.
						if (isPubliclyVisible) {
							presenter.makePrivate();
						} else {
							if (publicAclPrincipalId != null) {
								presenter.setAccess(publicAclPrincipalId, PermissionLevel.CAN_VIEW);
							}
						}
					}
				};
				
				addPeoplePanel.configure(permList, permissionDisplay, selectPermissionCallback, addPersonCallback, makePublicCallback, isPubliclyVisible);
				add(addPeoplePanel.asWidget());
				deleteAclButton.setEnabled(canEnableInheritance);
				add(toolTipAndDeleteAclButton);
			}
		}
	}
	
	@Override
	public Boolean isNotifyPeople(){
		return addPeoplePanel.getNotifyPeopleCheckBox().getValue();
	}
	
	@Override
	public void setNotifyCheckboxVisible(boolean isVisible) {
		addPeoplePanel.getNotifyPeopleCheckBox().setVisible(isVisible);
	}
	@Override
	public void setDeleteLocalACLButtonVisible(boolean isVisible) {
		deleteAclButton.setVisible(isVisible);
	}
	@Override
	public void setIsNotifyPeople(Boolean value) {
		if (value != null)
			addPeoplePanel.getNotifyPeopleCheckBox().setValue(value);
	}
	
	@Override
	public void showLoading() {
		this.clear();
		this.add(new HTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.loading16()) + " Loading...")));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showInfoSuccess(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showInfoError(String title, String message) {
		DisplayUtils.showErrorMessage(message);
	}
	

	@Override
	public void alertUnsavedViewChanges(final Callback saveCallback) {
		DisplayUtils.showConfirmDialog(DisplayConstants.UNSAVED_CHANGES, DisplayConstants.ADD_ACL_UNSAVED_CHANGES, 
				new Callback() {
					@Override
					public void invoke() {
						addPersonToAcl();
						saveCallback.invoke();
					}
				});
	}
	
	/*
	 * Private Methods
	 */	
	private void showAddMessage(String message) {
		// TODO : put this on the form somewhere
		showErrorMessage(message);
	}
	
	private void addPersonToAcl() {
		UserGroupSuggestBox peopleSuggestBox = addPeoplePanel.getSuggestBox();
		if(peopleSuggestBox.getSelectedSuggestion() != null) {
			String principalIdStr = peopleSuggestBox.getSelectedSuggestion().getHeader().getOwnerId();
			Long principalId = (Long.parseLong(principalIdStr));
			
			if (addPeoplePanel.getSelectedPermissionLevel() != null) {
				PermissionLevel level = addPeoplePanel.getSelectedPermissionLevel();
				presenter.setAccess(principalId, level);
				
				// clear selections
				peopleSuggestBox.clear();
				presenter.setUnsavedViewChanges(false);
			} else {
				showAddMessage("Please select a permission level to grant.");
			}
		} else {
			showAddMessage("Please select a user or team to grant permission to.");
		}
	}
	
	
	/*
	 * Set Access Callback
	 */
	public interface SetAccessCallback {
		public void invoke(Long principalId, PermissionLevel permissionLevel);
	}

}
