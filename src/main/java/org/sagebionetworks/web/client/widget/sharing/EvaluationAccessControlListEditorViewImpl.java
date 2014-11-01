package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
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

public class EvaluationAccessControlListEditorViewImpl extends LayoutContainer implements EvaluationAccessControlListEditorView {
 
	private static final int FIELD_WIDTH = 500;
	static final String PRINCIPAL_COLUMN_ID = "principalData";
	static final String ACCESS_COLUMN_ID = "accessData";
	static final String REMOVE_COLUMN_ID = "removeData";
	private static final int DEFAULT_WIDTH = 380;
	private static final int BUTTON_PADDING = 3;
	
	private static final String STYLE_VERTICAL_ALIGN_MIDDLE = "vertical-align:middle !important;";
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private UrlCache urlCache;
	private Grid<PermissionsTableEntry> permissionsGrid;
	private Map<PermissionLevel, String> permissionDisplay;
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	private ListStore<PermissionsTableEntry> permissionsStore;
	private ColumnModel columnModel;
	private PublicPrincipalIds publicPrincipalIds;
	private Boolean isOpenParticipation;
	private Button openParticipationButton;
	private SimpleComboBox<PermissionLevelSelect> permissionLevelCombo;
	private UserGroupSuggestBox peopleSuggestBox;
	
	@Inject
	public EvaluationAccessControlListEditorViewImpl(IconsImageBundle iconsImageBundle, 
			SageImageBundle sageImageBundle, UrlCache urlCache, SynapseJSNIUtils synapseJSNIUtils,
			UserGroupSuggestBox peopleSuggestBox) {
		this.iconsImageBundle = iconsImageBundle;		
		this.sageImageBundle = sageImageBundle;
		this.urlCache = urlCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.peopleSuggestBox = peopleSuggestBox;
		permissionDisplay = new HashMap<PermissionLevel, String>();
		permissionDisplay.put(PermissionLevel.CAN_VIEW, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_VIEW);
		permissionDisplay.put(PermissionLevel.CAN_SCORE_EVALUATION, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_SCORE);
		permissionDisplay.put(PermissionLevel.CAN_PARTICIPATE_EVALUATION, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_PARTICIPATE);
		permissionDisplay.put(PermissionLevel.CAN_ADMINISTER_EVALUATION, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER);		
		permissionDisplay.put(PermissionLevel.OWNER, DisplayConstants.MENU_PERMISSION_LEVEL_CAN_ADMINISTER);
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
		if (!aclEntry.isIndividual())
			permissionsStore.insert(new PermissionsTableEntry(permissionDisplay, aclEntry), 0); // insert groups first
		else if (aclEntry.isOwner()) {
			//owner should be the first (after groups, if present)
			int insertIndex = 0;
			for (; insertIndex < permissionsStore.getCount(); insertIndex++) {
				if (permissionsStore.getAt(insertIndex).getAclEntry().isIndividual())
					break;
			}
			permissionsStore.insert(new PermissionsTableEntry(permissionDisplay, aclEntry), insertIndex); // insert owner
		}
		else
			permissionsStore.add(new PermissionsTableEntry(permissionDisplay, aclEntry));
		permissionsGrid.reconfigure(permissionsStore, columnModel);
	}
	
	@Override
	public void setPublicPrincipalIds(PublicPrincipalIds publicPrincipalIds) {
		this.publicPrincipalIds = publicPrincipalIds;
	}

	@Override
	public void setIsOpenParticipation(Boolean isOpenParticipation) {
		this.isOpenParticipation = isOpenParticipation;
		if (openParticipationButton != null) {
			if (isOpenParticipation) {
				//already publicly visible, button removes access to public
				openParticipationButton.setText(DisplayConstants.BUTTON_REVOKE_OPEN_PARTICIPATION_ACL);
				openParticipationButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.lockGrey16()));
				DisplayUtils.addToolTip(openParticipationButton, DisplayConstants.BUTTON_REVOKE_OPEN_PARTICIPATION_TOOLTIP);
			}
			else {
				openParticipationButton.setText(DisplayConstants.BUTTON_OPEN_PARTICIPATION_ACL);
				openParticipationButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.globe16()));
				DisplayUtils.addToolTip(openParticipationButton, DisplayConstants.BUTTON_OPEN_PARTICIPATION_TOOLTIP);
			}
		}
	}
	
	@Override
	public void buildWindow(boolean unsavedChanges) {		
		this.removeAll(true);
		this.setLayout(new FlowLayout(10));

		// show existing permissions
		permissionsStore = new ListStore<PermissionsTableEntry>();
		permissionsGrid = createPermissionsGrid(
				permissionsStore, 
				createPeopleRenderer(publicPrincipalIds, synapseJSNIUtils, iconsImageBundle), 
				createButtonRenderer(),
				createRemoveRenderer(iconsImageBundle, new CallbackP<Long>() {
					@Override
					public void invoke(Long principalId) {
						presenter.removeAccess(principalId);
					}
				}),
				true);

		add(permissionsGrid, new MarginData(5, 0, 0, 0));
		columnModel = permissionsGrid.getColumnModel();
		
		// create panel to hold ACL management buttons
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setWidth(FIELD_WIDTH);
		TableData tdLeft = new TableData("1%", "100%");
		tdLeft.setPadding(BUTTON_PADDING);
		TableData tdRight = new TableData();
		tdRight.setPadding(BUTTON_PADDING);
		
		
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

		// user/group Suggest Box
		peopleSuggestBox.configureURLs(synapseJSNIUtils.getBaseFileHandleUrl(), synapseJSNIUtils.getBaseProfileAttachmentUrl());
		peopleSuggestBox.setPlaceholderText("Enter a user or group name...");
		peopleSuggestBox.setWidth(DEFAULT_WIDTH + "px");
		HorizontalPanel userGroupPanel = new HorizontalPanel();
		userGroupPanel.addStyleName("x-form-item");	// TODO: Remove when moving away from gxt components.
		
		Label boxLbl = new Label("User/Group:");
		boxLbl.addStyleName("width-80");
		
		userGroupPanel.add(boxLbl);
		userGroupPanel.add(peopleSuggestBox.asWidget());
		fieldSet.add(userGroupPanel);	

		// permission level combobox
		permissionLevelCombo = new SimpleComboBox<PermissionLevelSelect>();
		permissionLevelCombo.add(new PermissionLevelSelect(permissionDisplay.get(PermissionLevel.CAN_VIEW), PermissionLevel.CAN_VIEW));
		permissionLevelCombo.add(new PermissionLevelSelect(permissionDisplay.get(PermissionLevel.CAN_PARTICIPATE_EVALUATION), PermissionLevel.CAN_PARTICIPATE_EVALUATION));
		permissionLevelCombo.add(new PermissionLevelSelect(permissionDisplay.get(PermissionLevel.CAN_SCORE_EVALUATION), PermissionLevel.CAN_SCORE_EVALUATION));
		permissionLevelCombo.add(new PermissionLevelSelect(permissionDisplay.get(PermissionLevel.CAN_ADMINISTER_EVALUATION), PermissionLevel.CAN_ADMINISTER_EVALUATION));			
		permissionLevelCombo.setEmptyText("Select access level...");
		permissionLevelCombo.setFieldLabel("Access Level");
		permissionLevelCombo.setTypeAhead(false);
		permissionLevelCombo.setEditable(false);
		permissionLevelCombo.setForceSelection(true);
		permissionLevelCombo.setTriggerAction(TriggerAction.ALL);
		permissionLevelCombo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<PermissionLevelSelect>>() {				
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<PermissionLevelSelect>> se) {
				presenter.setUnsavedViewChanges(true);
			}
		});
		fieldSet.add(permissionLevelCombo);
		
		// share button and listener
		Button shareButton = new Button("Add");
		shareButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				addPersonToAcl();
			}
		});

		fieldSet.add(shareButton);
		form2.add(fieldSet);
		

		//Make Public button
		openParticipationButton = new Button();
		openParticipationButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//add the ability for authenticated users to participate
				if (isOpenParticipation) {
					if (publicPrincipalIds.getAuthenticatedAclPrincipalId() != null){
						presenter.removeAccess(publicPrincipalIds.getAuthenticatedAclPrincipalId());
					}
				}
				else {
					if (publicPrincipalIds.getAuthenticatedAclPrincipalId() != null) {
						presenter.setAccess(publicPrincipalIds.getAuthenticatedAclPrincipalId(), PermissionLevel.CAN_PARTICIPATE_EVALUATION);
					}
				}
				
			}
		});
		form2.add(openParticipationButton, tdLeft);
		add(form2);
		
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
		DisplayUtils.showErrorMessage(message);
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
	
	private Menu createEditAccessMenu(final AclEntry aclEntry) {
		final Long principalId = Long.parseLong(aclEntry.getOwnerId());
		Menu menu = new Menu();
		menu.setEnableScrolling(false);
		MenuItem item;
		
		item = new MenuItem(permissionDisplay.get(PermissionLevel.CAN_VIEW));			
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.setAccess(principalId, PermissionLevel.CAN_VIEW);
			}
		});
		menu.add(item);

		item = new MenuItem(permissionDisplay.get(PermissionLevel.CAN_PARTICIPATE_EVALUATION));			
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.setAccess(principalId, PermissionLevel.CAN_PARTICIPATE_EVALUATION);
			}
		});
		menu.add(item);

		item = new MenuItem(permissionDisplay.get(PermissionLevel.CAN_SCORE_EVALUATION));			
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.setAccess(principalId, PermissionLevel.CAN_SCORE_EVALUATION);
			}
		});
		menu.add(item);

		item = new MenuItem(permissionDisplay.get(PermissionLevel.CAN_ADMINISTER_EVALUATION));			
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.setAccess(principalId, PermissionLevel.CAN_ADMINISTER_EVALUATION);
			}
		});
		menu.add(item);
		
		return menu;
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
	
	@Override
	public void alertUnsavedViewChanges(final Callback saveCallback) {
		DisplayUtils.showConfirmDialog(DisplayConstants.UNSAVED_CHANGES, DisplayConstants.ADD_ACL_UNSAVED_CHANGES, 
				new Callback() {
					@Override
					public void invoke() {
						addPersonToAcl();
						presenter.setUnsavedViewChanges(false);
						saveCallback.invoke();
					}
				});
	}

	private void addPersonToAcl() {
		if(peopleSuggestBox.getSelectedSuggestion() != null) {
			String principalIdStr = peopleSuggestBox.getSelectedSuggestion().getHeader().getOwnerId();
			Long principalId = (Long.parseLong(principalIdStr));
			
			if(permissionLevelCombo.getValue() != null) {
				PermissionLevel level = permissionLevelCombo.getValue().getValue().getLevel();
				presenter.setAccess(principalId, level);
				
				// clear selections
				peopleSuggestBox.clear();
				permissionLevelCombo.clearSelections();
				presenter.setUnsavedViewChanges(false);
			} else {
				showAddMessage("Please select a permission level to grant.");
			}
		} else {
			showAddMessage("Please select a user or group to grant permission to.");
		}
	}
	
	public Grid<PermissionsTableEntry> createPermissionsGrid(
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
	
	public GridCellRenderer<PermissionsTableEntry> createPeopleRenderer(
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

	public GridCellRenderer<PermissionsTableEntry> createRemoveRenderer(final IconsImageBundle iconsImageBundle, final CallbackP<Long> callback) {
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
}

