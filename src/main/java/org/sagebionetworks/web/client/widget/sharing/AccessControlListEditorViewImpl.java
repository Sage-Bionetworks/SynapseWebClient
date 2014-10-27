package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListEditorViewImpl extends FlowPanel implements AccessControlListEditorView {
 
	private static final String STYLE_VERTICAL_ALIGN_MIDDLE = "vertical-align:middle !important;";
	private static final String PRINCIPAL_COLUMN_ID = "principalData";
	private static final String ACCESS_COLUMN_ID = "accessData";
	private static final String REMOVE_COLUMN_ID = "removeData";
	
	private static final String CANNOT_MODIFY_ACL_TEXT = "You do not have sufficient privileges to modify the ACL.";	// TODO: Check if this text is ok.
	
	private Presenter presenter;
	private Map<PermissionLevel, String> permissionDisplay;
	private SageImageBundle sageImageBundle;
	private PublicPrincipalIds publicPrincipalIds;
	private Boolean isPubliclyVisible;
	private boolean showEditColumns;
	
	private SharingPermissionsGrid permissionsGrid;
	
	private AddPeopleToAclPanel addPeoplePanel;
	
	private Dialog dialog;	// For access to the save button.
	
	private PermissionLevel[] permList = {PermissionLevel.CAN_VIEW, PermissionLevel.CAN_EDIT, PermissionLevel.CAN_EDIT_DELETE, PermissionLevel.CAN_ADMINISTER};	// To enforce order.
	
	@Inject
	public AccessControlListEditorViewImpl(SageImageBundle sageImageBundle,
					SharingPermissionsGrid permissionsGrid, AddPeopleToAclPanel addPeoplePanel) {
		this.sageImageBundle = sageImageBundle;
		this.permissionsGrid = permissionsGrid;
		this.addPeoplePanel = addPeoplePanel;
		permissionDisplay = new HashMap<PermissionLevel, String>();
		permissionDisplay.put(PermissionLevel.CAN_VIEW, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_VIEW);
		permissionDisplay.put(PermissionLevel.CAN_EDIT, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_EDIT);
		permissionDisplay.put(PermissionLevel.CAN_EDIT_DELETE, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_EDIT_DELETE);
		permissionDisplay.put(PermissionLevel.CAN_ADMINISTER, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER);		
		permissionDisplay.put(PermissionLevel.OWNER, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void addAclEntry(AclEntry aclEntry) {
		if (permissionsGrid == null)
			throw new IllegalStateException("Permissions window has not been built yet");
		ListBox permissionsListBox = createEditAccessListBox(aclEntry);
		if (!aclEntry.isIndividual()) {
			permissionsGrid.insert(aclEntry, 0, permissionsListBox); // insert groups first // TODO: PUBLIC is just a group? No team?
		} else if (aclEntry.isOwner()) {
			//owner should be the first (after groups, if present)
			int insertIndex = 0;
			for (; insertIndex < permissionsGrid.getCount(); insertIndex++) {
				if (permissionsGrid.getAt(insertIndex).isIndividual())
					break;
			}
			permissionsGrid.insert(aclEntry, insertIndex, permissionsListBox); // insert owner
		}
		else
			permissionsGrid.add(aclEntry, permissionsListBox);
	}
	
	@Override
	public void setPublicPrincipalIds(PublicPrincipalIds publicPrincipalIds) {
		this.publicPrincipalIds = publicPrincipalIds;
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
	public void buildWindow(boolean isInherited, boolean canEnableInheritance, boolean unsavedChanges, boolean canChangePermission) {		
		clear();
		
		// Display Permissions grid.
		showEditColumns = canChangePermission && !isInherited;
		CallbackP<Long> removeUserCallback = null;
		if (showEditColumns) {
			removeUserCallback = new CallbackP<Long>() {
					@Override
					public void invoke(Long principalId) {
						if (dialog != null)
							dialog.getPrimaryButton().setEnabled(true);
						presenter.removeAccess(principalId);
					}
				};
		}
		permissionsGrid.configure(removeUserCallback);
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
				org.gwtbootstrap3.client.ui.Button createAclButton = new org.gwtbootstrap3.client.ui.Button(DisplayConstants.BUTTON_PERMISSIONS_CREATE_NEW_ACL, IconType.PLUS, new ClickHandler() {
	
					@Override
					public void onClick(ClickEvent event) {
						presenter.createAcl();
					}
					
				});
				createAclButton.setType(ButtonType.SUCCESS);
				createAclButton.addStyleName("margin-top-10");
				
				Tooltip toolTipAndCreateAclButton = new Tooltip();
				toolTipAndCreateAclButton.setWidget(createAclButton);
				toolTipAndCreateAclButton.setText(DisplayConstants.PERMISSIONS_CREATE_NEW_ACL_TEXT);
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
						if (dialog != null)
							dialog.getPrimaryButton().setEnabled(true);
						// Add the ability for PUBLIC to see this entity.
						if (isPubliclyVisible) {
							presenter.makePrivate();
						} else {
							if (publicPrincipalIds.getPublicAclPrincipalId() != null) {
								presenter.setAccess(publicPrincipalIds.getPublicAclPrincipalId(), PermissionLevel.CAN_VIEW);
							}
						}
					}
				};
				
				addPeoplePanel.configure(permList, permissionDisplay, selectPermissionCallback, addPersonCallback, makePublicCallback, isPubliclyVisible);
				add(addPeoplePanel.asWidget());
				
				// 'Delete ACL' button
				org.gwtbootstrap3.client.ui.Button deleteAclButton = new org.gwtbootstrap3.client.ui.Button(DisplayConstants.BUTTON_PERMISSIONS_DELETE_ACL);
				deleteAclButton.setType(ButtonType.DANGER);
				deleteAclButton.setSize(ButtonSize.EXTRA_SMALL);
				deleteAclButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent ce) {
						presenter.deleteAcl();					
						}
				});
					
				Tooltip toolTipAndDeleteAclButton = new Tooltip();
				toolTipAndDeleteAclButton.setWidget(deleteAclButton);
				toolTipAndDeleteAclButton.setText(DisplayConstants.PERMISSIONS_DELETE_ACL_TEXT);
				toolTipAndDeleteAclButton.setPlacement(Placement.BOTTOM);
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
	public void setIsNotifyPeople(Boolean value) {
		if (value != null)
			addPeoplePanel.getNotifyPeopleCheckBox().setValue(value);
	}
	
	@Override
	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
		
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
	
	/*
	 * Private Methods
	 */	
	private void showAddMessage(String message) {
		// TODO : put this on the form somewhere
		showErrorMessage(message);
	}

	public static Grid<PermissionsTableEntry> createPermissionsGrid(
			ListStore<PermissionsTableEntry> permissionsStore,
			GridCellRenderer<PermissionsTableEntry> peopleRenderer,
			GridCellRenderer<PermissionsTableEntry> buttonRenderer,
			GridCellRenderer<PermissionsTableEntry> removeRenderer,
			boolean isEditable) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig column = new ColumnConfig();
		column.setId(PRINCIPAL_COLUMN_ID);
		column.setHeader("People");
		column.setWidth(200);
		column.setRenderer(peopleRenderer);
		configs.add(column);

		column = new ColumnConfig();
		column.setId(ACCESS_COLUMN_ID);
		column.setHeader("Access");
		column.setWidth(110);
		column.setRenderer(buttonRenderer);
		column.setStyle(STYLE_VERTICAL_ALIGN_MIDDLE);
		configs.add(column);

		column = new ColumnConfig();
		column.setId(REMOVE_COLUMN_ID);
		column.setHeader("");
		column.setWidth(25);
		column.setRenderer(removeRenderer);
		column.setStyle(STYLE_VERTICAL_ALIGN_MIDDLE);
		column.setHidden(!isEditable);
		configs.add(column);

		Grid<PermissionsTableEntry> permissionsGrid = new Grid<PermissionsTableEntry>(
				permissionsStore, new ColumnModel(configs));
		permissionsGrid.setAutoExpandColumn(PRINCIPAL_COLUMN_ID);
		permissionsGrid.setBorders(true);
		permissionsGrid.setWidth(520);
		permissionsGrid.setHeight(180);
		return permissionsGrid;
	}
	
	private ListBox createEditAccessListBox(final AclEntry aclEntry) {
		final Long principalId = Long.parseLong(aclEntry.getOwnerId());
		
		final ListBox listBox = new ListBox();
		
		if (aclEntry.isOwner()) {
			listBox.addItem("Owner");
			listBox.setEnabled(false);
			return listBox;
		}
		
		PermissionLevel permLevel = AclUtils.getPermissionLevel(new HashSet<ACCESS_TYPE>(aclEntry.getAccessTypes()));
		for (int i = 0; i < permList.length; i++) {
			listBox.addItem(permissionDisplay.get(permList[i]));
			if (permList[i].equals(permLevel))
				listBox.setSelectedIndex(i);
		}
		
		listBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.setAccess(principalId, permList[listBox.getSelectedIndex()]);
			}
		});
		
		return listBox;
	}

	public static GridCellRenderer<PermissionsTableEntry> createPeopleRenderer(
			final PublicPrincipalIds publicPrincipalIds, 
			final SynapseJSNIUtils synapseJSNIUtils,
			final IconsImageBundle iconsImageBundle) {
		GridCellRenderer<PermissionsTableEntry> personRenderer = new GridCellRenderer<PermissionsTableEntry>() {
			@Override
			public Object render(PermissionsTableEntry model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<PermissionsTableEntry> store,
					Grid<PermissionsTableEntry> grid) {
				PermissionsTableEntry entry = store.getAt(rowIndex);
				AclEntry aclEntry = entry.getAclEntry();
				String principalHtml = "";
				Long publicPrincipalId = publicPrincipalIds.getPublicAclPrincipalId();
				Long authenticatedPrincipalId = publicPrincipalIds.getAuthenticatedAclPrincipalId();
				Long anonymousUserPrincipalId = publicPrincipalIds.getAnonymousUserPrincipalId();
				
				if (aclEntry != null & aclEntry.getOwnerId() != null) {
					if (publicPrincipalId != null && aclEntry.getOwnerId().equals(publicPrincipalId.toString())) {
						//is public group
						principalHtml = DisplayUtils.getUserNameDescriptionHtml(DisplayConstants.PUBLIC_ACL_TITLE, DisplayConstants.PUBLIC_ACL_DESCRIPTION);
					} else if (authenticatedPrincipalId != null && aclEntry.getOwnerId().equals(authenticatedPrincipalId.toString())) {
						//is authenticated group
						principalHtml = DisplayUtils.getUserNameDescriptionHtml(DisplayConstants.AUTHENTICATED_USERS_ACL_TITLE, DisplayConstants.AUTHENTICATED_USERS_ACL_DESCRIPTION);	
					} else if (anonymousUserPrincipalId != null && aclEntry.getOwnerId().equals(anonymousUserPrincipalId.toString())) {
						//is anonymous user
						principalHtml = DisplayUtils.getUserNameDescriptionHtml(DisplayConstants.PUBLIC_USER_ACL_TITLE, DisplayConstants.PUBLIC_USER_ACL_DESCRIPTION);
					} else {
						principalHtml = DisplayUtils.getUserNameDescriptionHtml(aclEntry.getTitle(), aclEntry.getSubtitle());
					}
				}
				
				String iconHtml = "";
				if (publicPrincipalId != null && aclEntry.getOwnerId().equals(publicPrincipalId.toString())){
					ImageResource icon = iconsImageBundle.globe32();
					iconHtml = DisplayUtils.getIconThumbnailHtml(icon);	
				} else if (!aclEntry.isIndividual()) {
					//if a group, then try to fill in the icon from the team
					String url = DisplayUtils.createTeamIconUrl(
							synapseJSNIUtils.getBaseFileHandleUrl(), 
							aclEntry.getOwnerId()
					);
					iconHtml = DisplayUtils.getThumbnailPicHtml(url);
				} else {
					// try to get the userprofile picture
					String url = DisplayUtils.createUserProfilePicUrl(
							synapseJSNIUtils.getBaseProfileAttachmentUrl(), 
							aclEntry.getOwnerId() 
					);
					iconHtml = DisplayUtils.getThumbnailPicHtml(url);
				}
				return iconHtml + "&nbsp;&nbsp;" + principalHtml;
			}
			
		};
		return personRenderer;
	}

	public static GridCellRenderer<PermissionsTableEntry> createRemoveRenderer(final IconsImageBundle iconsImageBundle, final CallbackP<Long> callback) {
		GridCellRenderer<PermissionsTableEntry> removeButton = new GridCellRenderer<PermissionsTableEntry>() {  			   
			@Override  
			public Object render(final PermissionsTableEntry model, String property, ColumnData config, int rowIndex,  
				  final int colIndex, ListStore<PermissionsTableEntry> store, Grid<PermissionsTableEntry> grid) {				 
				  final PermissionsTableEntry entry = store.getAt(rowIndex);
					Anchor removeAnchor = new Anchor();
					removeAnchor.setHTML(DisplayUtils.getIconHtml(iconsImageBundle.deleteButton16()));
					removeAnchor.addClickHandler(new ClickHandler() {			
						@Override
						public void onClick(ClickEvent event) {
							Long principalId = (Long.parseLong(entry.getAclEntry().getOwnerId()));
							callback.invoke(principalId);
						}
					});
					return removeAnchor;
			  }
			};  
		return removeButton;
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

}
