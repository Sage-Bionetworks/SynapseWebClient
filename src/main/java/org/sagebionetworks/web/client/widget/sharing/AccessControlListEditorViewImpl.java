package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListEditorViewImpl extends FlowPanel implements AccessControlListEditorView {

	public static final String CREATE_ACL_HELP_TEXT = "By default the sharing settings are inherited from the parent folder or project. If you want to have different settings on a specific file, folder, or table you need to create local sharing settings and then modify them.";

	private static final String CANNOT_MODIFY_ACL_TEXT = "You do not have sufficient privileges to modify the sharing settings.";
	private static final String CANNOT_MODIFY_ACL_ANONYMOUS_HTML = "You must be <a href=\"#!LoginPlace:0\">logged in</a> and have sufficient privileges to modify the sharing settings.";
	private Presenter presenter;
	private Map<PermissionLevel, String> permissionDisplay;
	private Long publicAclPrincipalId, authenticatedPrincipalId;
	private Boolean isPubliclyVisible;
	private boolean showEditColumns;
	private PermissionLevel defaultPermissionLevel;

	private SharingPermissionsGrid permissionsGrid;

	private AclAddPeoplePanel addPeoplePanel;

	private PermissionLevel[] permList; // To enforce order.
	private Button deleteAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_DELETE_ACL);
	private HelpWidget helpWidget = new HelpWidget();
	private IsWidget synAlertWidget;
	private PortalGinInjector ginInjector;

	@Inject
	public AccessControlListEditorViewImpl(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;

		helpWidget.setHelpMarkdown("Learn more about managing access controls and permissions in Synapse.");
		helpWidget.setHref(WebConstants.DOCS_URL + "access_controls.html");
		helpWidget.setAddStyleNames("margin-left-5");

		// 'Delete ACL' button
		deleteAclButton.addStyleName("text-danger");
		deleteAclButton.setSize(ButtonSize.SMALL);
		deleteAclButton.addClickHandler(event -> {
			presenter.deleteAcl();
		});
	}

	public AclAddPeoplePanel getAclAddPeoplePanel() {
		if (addPeoplePanel == null) {
			addPeoplePanel = ginInjector.getAclAddPeoplePanel();
		}
		return addPeoplePanel;
	}

	public SharingPermissionsGrid getSharingPermissionsGrid() {
		if (permissionsGrid == null) {
			permissionsGrid = ginInjector.getSharingPermissionsGrid();
		}
		return permissionsGrid;
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
		if (!aclEntry.isIndividual()) {
			getSharingPermissionsGrid().insert(aclEntry, 0, permList, permissionDisplay, true); // insert groups first
		} else {
			getSharingPermissionsGrid().add(aclEntry, permList, permissionDisplay);
		}
	}

	@Override
	public void setPublicAclPrincipalId(Long publicAclPrincipalId) {
		this.publicAclPrincipalId = publicAclPrincipalId;
	}

	@Override
	public void setAuthenticatedAclPrinciapalId(Long authenticatedPrincipalId) {
		this.authenticatedPrincipalId = authenticatedPrincipalId;
	}

	@Override
	public void setPublicPrivateButtonVisible(boolean isVisible) {
		getAclAddPeoplePanel().setPublicPrivateButtonVisible(isVisible);
	}

	@Override
	public void setIsPubliclyVisible(Boolean isPubliclyVisible) {
		this.isPubliclyVisible = isPubliclyVisible;
		if (isPubliclyVisible != null) {
			getAclAddPeoplePanel().setMakePublicButtonDisplay(isPubliclyVisible);
		}
	}

	@Override
	public void setSynAlert(IsWidget w) {
		this.synAlertWidget = w;
	}

	@Override
	public void clear() {
		super.clear();
		if (permissionsGrid != null) {
			permissionsGrid.clear();
		}
	}

	@Override
	public void buildWindow(boolean isProject, boolean isInherited, String aclEntityId, boolean canEnableInheritance, boolean canChangePermission, PermissionLevel defaultPermissionLevel, boolean isLoggedIn) {
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
		Div div = new Div();
		div.setMarginBottom(20);
		add(div);
		if (!isInherited) {
			if (isProject) {
				div.add(new Text("The sharing settings shown below apply to this project and are inherited by all project contents unless local sharing settings have been set."));
				if (canChangePermission) {
					add(new Heading(HeadingSize.H5, "Manage Sharing Settings"));
				}
			} else {
				div.add(new HTML("The local sharing settings shown below are <strong>not</strong> being inherited from a parent resource."));
				if (canChangePermission) {
					add(new Heading(HeadingSize.H5, "Manage Local Sharing Settings"));
				}
			}
		} else {
			// is inherited
			div.add(new Span("The sharing settings shown below are currently being inherited from&nbsp;"));
			EntityIdCellRenderer entityRenderer = ginInjector.getEntityIdCellRenderer();
			ClickHandler customClickHandler = event -> {
				DisplayUtils.newWindow("#!Synapse:" + aclEntityId, "", "");
			};
			entityRenderer.setValue(aclEntityId, customClickHandler, false);
			div.add(entityRenderer);
			div.add(new Span("&nbsp;and cannot be modified here."));
		}
		SetAccessCallback setAccessCallback = new SetAccessCallback() {
			@Override
			public void invoke(Long principalId, PermissionLevel permissionLevel) {
				presenter.setAccess(principalId, permissionLevel);
			}
		};

		getSharingPermissionsGrid().configure(removeUserCallback, setAccessCallback);
		add(getSharingPermissionsGrid().asWidget());

		if (!canChangePermission) {
			// Inform user of restricted privileges.
			IsWidget canNotModify;
			if (isLoggedIn) {
				canNotModify = new Label(CANNOT_MODIFY_ACL_TEXT);
			} else {
				canNotModify = new HTML(CANNOT_MODIFY_ACL_ANONYMOUS_HTML);
			}
			add(canNotModify);
		} else {
			if (isInherited) {
				// Notify user of inherited sharing settings.
				Label readOnly = new Label(CREATE_ACL_HELP_TEXT);
				readOnly.addStyleName("margin-bottom-10");
				add(readOnly);

				// 'Create ACL' button
				Button createAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_CREATE_NEW_ACL, IconType.PLUS, event -> {
					presenter.createAcl();
				});
				createAclButton.setType(ButtonType.SUCCESS);
				createAclButton.setSize(ButtonSize.SMALL);
				add(createAclButton);
				add(helpWidget);
			} else {
				// Configure AddPeopleToAclPanel.
				CallbackP<UserGroupSuggestion> addPersonCallback = new CallbackP<UserGroupSuggestion>() {
					@Override
					public void invoke(UserGroupSuggestion param) {
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
								presenter.setAccess(authenticatedPrincipalId, PermissionLevel.CAN_DOWNLOAD);
							}
						}
					}
				};

				getAclAddPeoplePanel().configure(permList, addPersonCallback, makePublicCallback, isPubliclyVisible);
				add(getAclAddPeoplePanel().asWidget());
				if (canEnableInheritance) {
					Label deleteInfoLabel = new Label("The sharing settings will be inherited from the parent folder or project if local sharing settings are deleted.");
					deleteInfoLabel.addStyleName("margin-bottom-10");
					add(deleteInfoLabel);
					add(deleteAclButton);
					add(helpWidget);
				}
			}
		}
		add(synAlertWidget);
	}

	@Override
	public Boolean isNotifyPeople() {
		return getAclAddPeoplePanel().getNotifyPeopleCheckBox().getValue();
	}

	@Override
	public void setNotifyCheckboxVisible(boolean isVisible) {
		getAclAddPeoplePanel().getNotifyPeopleCheckBox().setVisible(isVisible);
	}

	@Override
	public void setDeleteLocalACLButtonVisible(boolean isVisible) {
		deleteAclButton.setVisible(isVisible);
	}

	@Override
	public void setIsNotifyPeople(Boolean value) {
		if (value != null)
			getAclAddPeoplePanel().getNotifyPeopleCheckBox().setValue(value);
	}

	@Override
	public void showLoading() {
		this.clear();
		this.add(DisplayUtils.getLoadingWidget());
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showInfoSuccess(String title, String message) {
		DisplayUtils.showInfo(message);
	}

	/*
	 * Private Methods
	 */
	private void showAddMessage(String message) {
		// TODO : put this on the form somewhere
		showErrorMessage(message);
	}

	private void addPersonToAcl(UserGroupSuggestion suggestion) {
		if (suggestion != null) {
			UserGroupSuggestion selectedUser = suggestion;
			String principalIdStr = selectedUser.getId();
			Long principalId = (Long.parseLong(principalIdStr));

			presenter.setAccess(principalId, defaultPermissionLevel);
			// clear selections
			getAclAddPeoplePanel().getSuggestBox().clear();
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
