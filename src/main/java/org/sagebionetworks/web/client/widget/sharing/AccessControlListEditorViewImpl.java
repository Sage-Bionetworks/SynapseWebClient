package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtilsGWT;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListEditorViewImpl extends LayoutContainer implements AccessControlListEditorView {
 
	private static final int FIELD_WIDTH = 500;
	private static final String STYLE_VERTICAL_ALIGN_MIDDLE = "vertical-align:middle !important;";
	private static final String PRINCIPAL_COLUMN_ID = "principalData";
	private static final String ACCESS_COLUMN_ID = "accessData";
	private static final String REMOVE_COLUMN_ID = "removeData";
	private static final int DEFAULT_WIDTH = 380;
	private static final int BUTTON_PADDING = 3;
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private UrlCache urlCache;
	private Grid<PermissionsTableEntry> permissionsGrid;
	private Map<PermissionLevel, String> permissionDisplay;
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	private ListStore<PermissionsTableEntry> permissionsStore;
	private ColumnModel columnModel;
	private Long publicPrincipalId, authenticatedPrincipalId;
	private Boolean isPubliclyVisible;
	private Button publicButton;
	
	@Inject
	public AccessControlListEditorViewImpl(IconsImageBundle iconsImageBundle, 
			SageImageBundle sageImageBundle, UrlCache urlCache, SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;		
		this.sageImageBundle = sageImageBundle;
		this.urlCache = urlCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
		permissionDisplay = new HashMap<PermissionLevel, String>();
		permissionDisplay.put(PermissionLevel.CAN_VIEW, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_VIEW);
		permissionDisplay.put(PermissionLevel.CAN_EDIT, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_EDIT);
		permissionDisplay.put(PermissionLevel.CAN_ADMINISTER, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER);		
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
	}
		
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void addAclEntry(AclEntry aclEntry) {
		if (permissionsStore == null || columnModel == null || permissionsGrid == null)
			throw new IllegalStateException("Permissions window has not been built yet");
		if (!aclEntry.getPrincipal().getIsIndividual())
			permissionsStore.insert(new PermissionsTableEntry(aclEntry), 0); // insert groups first
		else if (aclEntry.isOwner()) {
			//owner should be the first (after groups, if present)
			int insertIndex = 0;
			for (; insertIndex < permissionsStore.getCount(); insertIndex++) {
				UserGroupHeader item = permissionsStore.getAt(insertIndex).getAclEntry().getPrincipal();
				if (item.getIsIndividual())
					break;
			}
			permissionsStore.insert(new PermissionsTableEntry(aclEntry), insertIndex); // insert owner
		}
		else
			permissionsStore.add(new PermissionsTableEntry(aclEntry));
		permissionsGrid.reconfigure(permissionsStore, columnModel);
	}
	
	@Override
	public void setPublicPrincipalId(Long id) {
		publicPrincipalId = id;
	}
	@Override
	public void setAuthenticatedPrincipalId(Long id) {
		authenticatedPrincipalId = id;
	}
	@Override
	public void setIsPubliclyVisible(Boolean isPubliclyVisible) {
		this.isPubliclyVisible = isPubliclyVisible;
		if (publicButton != null) {
			if (isPubliclyVisible) {
				//already publicly visible, button removes access to public
				publicButton.setText(DisplayConstants.BUTTON_REVOKE_PUBLIC_ACL);
				publicButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.lockGrey16()));
				//TODO: use bootstrap tooltip instead.  can't get it working with the Button (and all other tooltips on dialog are standard).
				publicButton.setToolTip(new ToolTipConfig(DisplayConstants.BUTTON_REVOKE_PUBLIC_ACL, DisplayConstants.BUTTON_REVOKE_PUBLIC_ACL_TOOLTIP));
//				DisplayUtils.addTooltip(this.synapseJSNIUtils, publicButtonWrapper, DisplayConstants.BUTTON_REVOKE_PUBLIC_ACL_TOOLTIP, TOOLTIP_POSITION.BOTTOM);
			}
			else {
				publicButton.setText(DisplayConstants.BUTTON_MAKE_PUBLIC_ACL);
				publicButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.globe16()));
				publicButton.setToolTip(new ToolTipConfig(DisplayConstants.BUTTON_MAKE_PUBLIC_ACL, DisplayConstants.BUTTON_MAKE_PUBLIC_ACL_TOOLTIP));
//				DisplayUtils.addTooltip(this.synapseJSNIUtils, publicButtonWrapper, DisplayConstants.BUTTON_MAKE_PUBLIC_ACL_TOOLTIP, TOOLTIP_POSITION.BOTTOM);
			}
		}
	}
	
	@Override
	public void buildWindow(boolean isInherited, boolean canEnableInheritance, boolean unsavedChanges) {		
		this.removeAll(true);
		this.setLayout(new FlowLayout(10));

		// show existing permissions
		permissionsStore = new ListStore<PermissionsTableEntry>();
		createPermissionsGrid(permissionsStore);
		
		// create panel to hold ACL management buttons
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setWidth(FIELD_WIDTH);
		TableData tdLeft = new TableData("1%", "100%");
		tdLeft.setPadding(BUTTON_PADDING);
		TableData tdRight = new TableData();
		tdRight.setPadding(BUTTON_PADDING);
		
		if(isInherited) { 
			permissionsGrid.disable();
			Label readOnly = new Label(DisplayConstants.PERMISSIONS_INHERITED_TEXT);		
			readOnly.setWidth(450);
			add(readOnly, new MarginData(15, 0, 0, 0));			
			
			// 'Create ACL' button
			Button createAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_CREATE_NEW_ACL, AbstractImagePrototype.create(iconsImageBundle.addSquare16()));
			createAclButton.setToolTip(new ToolTipConfig("Warning", DisplayConstants.PERMISSIONS_CREATE_NEW_ACL_TEXT));
			createAclButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					presenter.createAcl();
				}
			});
			hPanel.setHorizontalAlign(HorizontalAlignment.LEFT);
			hPanel.add(createAclButton, tdLeft);
		} else {
			// show add people view
			FormPanel form2 = new FormPanel();  
			form2.setFrame(false);  
			form2.setHeaderVisible(false);  
			form2.setAutoWidth(true);			
			form2.setLayout(new FlowLayout());
			
			FormLayout layout = new FormLayout();  
			layout.setLabelWidth(75);
			layout.setDefaultWidth(DEFAULT_WIDTH);
			  
			FieldSet fieldSet = new FieldSet();  
			fieldSet.setHeading(DisplayConstants.LABEL_PERMISSION_TEXT_ADD_PEOPLE);  
			fieldSet.setCheckboxToggle(false);
			fieldSet.setCollapsible(false);			
			fieldSet.setLayout(layout);
			fieldSet.setWidth(FIELD_WIDTH);
			
			// user/group combobox
			final ComboBox<ModelData> peopleCombo = UserGroupSearchBox.createUserGroupSearchSuggestBox(urlCache.getRepositoryServiceUrl(), publicPrincipalId, authenticatedPrincipalId);
			peopleCombo.setEmptyText("Enter a user or group name...");
			peopleCombo.setFieldLabel("User/Group");
			peopleCombo.setForceSelection(true);
			peopleCombo.setTriggerAction(TriggerAction.ALL);
			fieldSet.add(peopleCombo);			

			// permission level combobox
			final SimpleComboBox<PermissionLevelSelect> permissionLevelCombo = new SimpleComboBox<PermissionLevelSelect>();
			permissionLevelCombo.add(new PermissionLevelSelect(permissionDisplay.get(PermissionLevel.CAN_VIEW), PermissionLevel.CAN_VIEW));
			permissionLevelCombo.add(new PermissionLevelSelect(permissionDisplay.get(PermissionLevel.CAN_EDIT), PermissionLevel.CAN_EDIT));
			permissionLevelCombo.add(new PermissionLevelSelect(permissionDisplay.get(PermissionLevel.CAN_ADMINISTER), PermissionLevel.CAN_ADMINISTER));			
			permissionLevelCombo.setEmptyText("Select access level...");
			permissionLevelCombo.setFieldLabel("Access Level");
			permissionLevelCombo.setTypeAhead(false);
			permissionLevelCombo.setEditable(false);
			permissionLevelCombo.setForceSelection(true);
			permissionLevelCombo.setTriggerAction(TriggerAction.ALL);
			fieldSet.add(permissionLevelCombo);
			
			// share button and listener
			Button shareButton = new Button("Add");
			shareButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					if(peopleCombo.getValue() != null) {
						ModelData selectedModel = peopleCombo.getValue();
						String principalIdStr = (String) selectedModel.get(UserGroupSearchBox.KEY_PRINCIPAL_ID);
						Long principalId = (Long.parseLong(principalIdStr));
						
						if(permissionLevelCombo.getValue() != null) {
							PermissionLevel level = permissionLevelCombo.getValue().getValue().getLevel();
							presenter.setAccess(principalId, level);
							
							// clear selections
							peopleCombo.clearSelections();
							permissionLevelCombo.clearSelections();
						} else {
							showAddMessage("Please select a permission level to grant.");
						}
					} else {
						showAddMessage("Please select a user or group to grant permission to.");
					}
				}
			});

			fieldSet.add(shareButton);
			form2.add(fieldSet);
			

			//Make Public button
			publicButton = new Button();
			publicButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					//add the ability for PUBLIC to see this entity
					if (isPubliclyVisible) {
						if (publicPrincipalId != null){
							presenter.removeAccess(publicPrincipalId);
						}
					}
					else {
						if (publicPrincipalId != null) {
							presenter.setAccess(publicPrincipalId, PermissionLevel.CAN_VIEW);
						}
					}
					
				}
			});
			form2.add(publicButton, tdLeft);
			add(form2);
			
			// 'Delete ACL' button
			Button deleteAclButton = new Button(DisplayConstants.BUTTON_PERMISSIONS_DELETE_ACL, AbstractImagePrototype.create(iconsImageBundle.deleteButton16()));
			deleteAclButton.setToolTip(new ToolTipConfig("Warning", DisplayConstants.PERMISSIONS_DELETE_ACL_TEXT));
			deleteAclButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					presenter.deleteAcl();					
				}
			});
			
			hPanel.setHorizontalAlign(HorizontalAlignment.LEFT);
			//delete button takes up the rest of the line space
			hPanel.add(deleteAclButton, tdLeft);
			deleteAclButton.setEnabled(canEnableInheritance);
		}
		
		// Unsaved changes label
//		Label blank = new Label("");
//		Label unsavedChangesLabel = new Label(DisplayUtils.getIconHtml(iconsImageBundle.warning16()) + " " + DisplayConstants.PERMISSIONS_UNSAVED_CHANGES);
//	   	hPanel.setHorizontalAlign(HorizontalAlignment.RIGHT);
//		hPanel.add(unsavedChanges ? unsavedChangesLabel : blank, tdRight);
		
		this.add(hPanel, new MarginData(10, 0, 0, 0));
		this.layout(true);
	}
	
	@Override
	public void showLoading() {
		this.removeAll(true);
		this.add(new HTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.loading16()) + " Loading...")));
		this.layout(true);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showErrorMessage(String message) {
		MessageBox.info("Message", message, null);
	}

	@Override
	public void clear() {
		this.removeAll();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showInfoSuccess(String title, String message) {
		DisplayUtils.showInfo(title, message);
		//TODO: Move info messages on top of modal shade
//		Alert alert = new Alert(title, message);
//		alert.setTimeout(1000);
//		alert.setAlertType(AlertType.Success);
//		this.insert(alert, 0);
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

	private void createPermissionsGrid(ListStore<PermissionsTableEntry> permissionsStore) {			
		GridCellRenderer<PermissionsTableEntry> peopleRenderer = createPeopleRenderer();
		GridCellRenderer<PermissionsTableEntry> buttonRenderer = createButtonRenderer();
		GridCellRenderer<PermissionsTableEntry> removeRenderer = createRemoveRenderer();						   
				   
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
		configs.add(column);  
				   				   			   
		columnModel = new ColumnModel(configs);  				  				 
		permissionsGrid = new Grid<PermissionsTableEntry>(permissionsStore, columnModel);
		permissionsGrid.setAutoExpandColumn(PRINCIPAL_COLUMN_ID);  
		permissionsGrid.setBorders(true);		
		permissionsGrid.setWidth(520);
		permissionsGrid.setHeight(180);
		
		add(permissionsGrid, new MarginData(5, 0, 0, 0));
		
	}
	
	private Menu createEditAccessMenu(final AclEntry aclEntry) {
		final Long principalId = Long.parseLong(aclEntry.getPrincipal().getOwnerId());
		Menu menu = new Menu();
		MenuItem item;
		
		item = new MenuItem(permissionDisplay.get(PermissionLevel.CAN_VIEW));			
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.setAccess(principalId, PermissionLevel.CAN_VIEW);
			}
		});
		menu.add(item);

		item = new MenuItem(permissionDisplay.get(PermissionLevel.CAN_EDIT));			
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.setAccess(principalId, PermissionLevel.CAN_EDIT);
			}
		});
		menu.add(item);

		item = new MenuItem(permissionDisplay.get(PermissionLevel.CAN_ADMINISTER));			
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.setAccess(principalId, PermissionLevel.CAN_ADMINISTER);
			}
		});
		menu.add(item);
		
		return menu;
	}

	private GridCellRenderer<PermissionsTableEntry> createPeopleRenderer() {
		GridCellRenderer<PermissionsTableEntry> personRenderer = new GridCellRenderer<PermissionsTableEntry>() {
			@Override
			public Object render(PermissionsTableEntry model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<PermissionsTableEntry> store,
					Grid<PermissionsTableEntry> grid) {
				PermissionsTableEntry entry = store.getAt(rowIndex);
				UserGroupHeader principal = entry.getAclEntry().getPrincipal();
				String principalHtml;
				boolean isThePublicGroup = principal != null && principal.getOwnerId().equals(publicPrincipalId.toString());
				boolean isTheAuthenticatedUsersGroup = principal != null && principal.getOwnerId().equals(authenticatedPrincipalId.toString());
				
				if (isThePublicGroup)
					principalHtml = DisplayUtils.getUserNameEmailHtml(DisplayConstants.PUBLIC_ACL_TITLE, DisplayConstants.PUBLIC_ACL_DESCRIPTION);
				else if (isTheAuthenticatedUsersGroup)
					principalHtml = DisplayUtils.getUserNameEmailHtml(DisplayConstants.AUTHENTICATED_USERS_ACL_TITLE, DisplayConstants.AUTHENTICATED_USERS_ACL_DESCRIPTION);
				else
					principalHtml = DisplayUtils.getUserNameEmailHtml(principal);
				
				String iconHtml = "";
				if (principal.getPic() != null) {
					// Principal has a profile picture
					String url = DisplayUtils.createUserProfileAttachmentUrl(
							synapseJSNIUtils.getBaseProfileAttachmentUrl(), 
							principal.getOwnerId(), 
							principal.getPic().getPreviewId(), 
							null
					);
					iconHtml = DisplayUtils.getThumbnailPicHtml(url);
				} else if (principal.getOwnerId().equals(publicPrincipalId.toString())){
					ImageResource icon = iconsImageBundle.globe32();
					iconHtml = DisplayUtils.getIconThumbnailHtml(icon);	
				} else {
					// Default to generic user or group avatar
					ImageResource icon = principal.getIsIndividual() ? iconsImageBundle.userBusinessGrey40() : iconsImageBundle.usersGrey40();
					iconHtml = DisplayUtils.getIconThumbnailHtml(icon);	
				}
				return iconHtml + "&nbsp;&nbsp;" + principalHtml;
			}
			
		};
		return personRenderer;
	}

	private GridCellRenderer<PermissionsTableEntry> createButtonRenderer() {
		GridCellRenderer<PermissionsTableEntry> buttonRenderer = new GridCellRenderer<PermissionsTableEntry>() {  
			   
			  private boolean init;  
			  @Override	   
			  public Object render(final PermissionsTableEntry model, String property, ColumnData config, final int rowIndex,  
			      final int colIndex, ListStore<PermissionsTableEntry> store, Grid<PermissionsTableEntry> grid) {
				  PermissionsTableEntry entry = store.getAt(rowIndex);
			    if (!init) {  
			      init = true;  
			      grid.addListener(Events.ColumnResize, new Listener<GridEvent<PermissionsTableEntry>>() {  
					   
			        public void handleEvent(GridEvent<PermissionsTableEntry> be) {  
			          for (int i = 0; i < be.getGrid().getStore().getCount(); i++) {  
			            if (be.getGrid().getView().getWidget(i, be.getColIndex()) != null  
			                && be.getGrid().getView().getWidget(i, be.getColIndex()) instanceof BoxComponent) {  
			              ((BoxComponent) be.getGrid().getView().getWidget(i, be.getColIndex())).setWidth(be.getWidth() - 10);  
			            }  
			          }  
			        }  
			      });  
			    }
			    if(entry.getAclEntry().isOwner()) {
				    Button b = new Button(DisplayConstants.MENU_PERMISSION_LEVEL_IS_OWNER);
				    b.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 15);
				    b.disable();
					return b;		    	
			    } else {
				    Button b = new Button((String) model.get(property));  
				    b.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 25);  
				    b.setToolTip("Click to change");				  
				    b.setMenu(createEditAccessMenu(entry.getAclEntry()));
				    return b;
			    }
			  }
			};  
			
			return buttonRenderer;
	}

	private GridCellRenderer<PermissionsTableEntry> createRemoveRenderer() {
		GridCellRenderer<PermissionsTableEntry> removeButton = new GridCellRenderer<PermissionsTableEntry>() {  			   
			@Override  
			public Object render(final PermissionsTableEntry model, String property, ColumnData config, int rowIndex,  
			      final int colIndex, ListStore<PermissionsTableEntry> store, Grid<PermissionsTableEntry> grid) {				 
				  final PermissionsTableEntry entry = store.getAt(rowIndex);
			    if(entry.getAclEntry().isOwner()) {
					return new Label("");		    	
			    } else {				    
					Anchor removeAnchor = new Anchor();
					removeAnchor.setHTML(DisplayUtils.getIconHtml(iconsImageBundle.deleteButton16()));
					removeAnchor.addClickHandler(new ClickHandler() {			
						@Override
						public void onClick(ClickEvent event) {
							Long principalId = (Long.parseLong(entry.getAclEntry().getPrincipal().getOwnerId()));
							presenter.removeAccess(principalId);
						}
					});
					return removeAnchor;
				    
			    }
			  }
			};  
		return removeButton;
	}
	
	/*
	 * Private Classes
	 */
	private class PermissionsTableEntry extends BaseModelData {
		private static final long serialVersionUID = -5153720887903543399L;
		private AclEntry aclEntry;
		public PermissionsTableEntry(AclEntry aclEntry) {			
			super();			
			this.aclEntry = aclEntry;
			UserGroupHeader principal = aclEntry.getPrincipal();
			this.set(PRINCIPAL_COLUMN_ID, principal);			
			this.set(REMOVE_COLUMN_ID, principal);			
			PermissionLevel level = AclUtils.getPermissionLevel(new HashSet<ACCESS_TYPE>(aclEntry.getAccessTypes()));			
			if(level != null) {
				this.set(ACCESS_COLUMN_ID, permissionDisplay.get(level)); 
			}			
		}
		public AclEntry getAclEntry() {
			return aclEntry;
		}		
	}

	private class PermissionLevelSelect {
		private String display;
		private PermissionLevel level;
		public PermissionLevelSelect(String display, PermissionLevel level) {
			super();
			this.display = display;
			this.level = level;
		}
		public String getDisplay() {
			return display;
		}
		public void setDisplay(String display) {
			this.display = display;
		}
		public PermissionLevel getLevel() {
			return level;
		}
		public void setLevel(PermissionLevel level) {
			this.level = level;
		}		
		
		public String toString() {
			return display;
		}
	}
}
