package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.shared.WebConstants;
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
	
	public static final String CREATE_ACL_HELP_TEXT = "By default the sharing settings are inhereted from the parent folder or project.  If you want to have different settings on a specific file folder or table you need to create local sharing settings then modify them.";

	public static final String DELETE_ACL_HELP_TEXT = "If a file or folder has sharing settings that are different from its parent folder or project you can delete the setting thereby inherting the sharing settings of the parent folder or project.";

	private static final String CANNOT_MODIFY_ACL_TEXT = "You do not have sufficient privileges to modify the sharing settings.";
	
	private Presenter presenter;
	private Map<PermissionLevel, String> permissionDisplay;
	private SageImageBundle sageImageBundle;
	private Long publicAclPrincipalId;
	private Boolean isPubliclyVisible;
	private boolean showEditColumns;
	private PermissionLevel defaultPermissionLevel;
	
	private SharingPermissionsGrid permissionsGrid;
	
	private AclAddPeoplePanel addPeoplePanel;
	
	private PermissionLevel[] permList;	// To enforce order.
	private Button deleteAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_DELETE_ACL);
	
	@Inject
	public AccessControlListEditorViewImpl(SageImageBundle sageImageBundle,
					SharingPermissionsGrid permissionsGrid, AclAddPeoplePanel addPeoplePanel) {
		this.sageImageBundle = sageImageBundle;
		this.permissionsGrid = permissionsGrid;
		this.addPeoplePanel = addPeoplePanel;
		
		// 'Delete ACL' button
		deleteAclButton.setType(ButtonType.DANGER);
		deleteAclButton.setSize(ButtonSize.SMALL);
		deleteAclButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				presenter.deleteAcl();					
			}
		});
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
			permissionsGrid.insert(aclEntry, 0, permList, permissionDisplay, true); // insert groups first
		} else {
			permissionsGrid.add(aclEntry, permList, permissionDisplay);
		}
	}
	
	@Override
	public void setPublicAclPrincipalId(Long publicAclPrincipalId) {
		this.publicAclPrincipalId = publicAclPrincipalId;
	}
	@Override
	public void setPublicPrivateButtonVisible(boolean isVisible) {
		addPeoplePanel.setPublicPrivateButtonVisible(isVisible);
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
	public void buildWindow(boolean isInherited, boolean canEnableInheritance, boolean canChangePermission, PermissionLevel defaultPermissionLevel) {		
		clear();
		this.defaultPermissionLevel = defaultPermissionLevel;
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
				readOnly.addStyleName("margin-bottom-10");
				add(readOnly);
				
				// 'Create ACL' button
				Button createAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_CREATE_NEW_ACL, IconType.PLUS, new ClickHandler() {
	
					@Override
					public void onClick(ClickEvent event) {
						presenter.createAcl();
					}
					
				});
				createAclButton.setType(ButtonType.SUCCESS);
				createAclButton.setSize(ButtonSize.SMALL);
				add(createAclButton);
				HelpWidget helpWidget = new HelpWidget();
				helpWidget.setHelpMarkdown(CREATE_ACL_HELP_TEXT);
				helpWidget.setHref(WebConstants.DOCS_URL + "access_controls.html");
				helpWidget.setAddStyleNames("margin-left-5");
				add(helpWidget.asWidget());
			} else {
				// Configure AddPeopleToAclPanel.
				CallbackP<SynapseSuggestion> addPersonCallback = new CallbackP<SynapseSuggestion>() {
					@Override
					public void invoke(SynapseSuggestion param) {
						addPersonToAcl(param);
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
				
				addPeoplePanel.configure(permList, addPersonCallback, makePublicCallback, isPubliclyVisible);
				add(addPeoplePanel.asWidget());
				deleteAclButton.setEnabled(canEnableInheritance);
				add(deleteAclButton);
				HelpWidget helpWidget = new HelpWidget();
				helpWidget.setHelpMarkdown(DELETE_ACL_HELP_TEXT);
				helpWidget.setHref(WebConstants.DOCS_URL + "access_controls.html");
				helpWidget.setAddStyleNames("margin-left-5");
				add(helpWidget.asWidget());
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
	
	/*
	 * Private Methods
	 */	
	private void showAddMessage(String message) {
		// TODO : put this on the form somewhere
		showErrorMessage(message);
	}
	
	private void addPersonToAcl(SynapseSuggestion suggestion) {
		if(suggestion != null) {
			SynapseSuggestion selectedUser = suggestion;
			String principalIdStr = selectedUser.getId();
			Long principalId = (Long.parseLong(principalIdStr));
			
			presenter.setAccess(principalId, defaultPermissionLevel);
			// clear selections
			addPeoplePanel.getSuggestBox().clear();
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
