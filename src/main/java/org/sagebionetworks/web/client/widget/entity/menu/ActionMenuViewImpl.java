package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.download.LocationableUploader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.EntityType;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ActionMenuViewImpl extends HorizontalPanel implements ActionMenuView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private AccessMenuButton accessMenuButton;
	private AccessControlListEditor accessControlListEditor;
	private LocationableUploader locationableUploader;
	private MyEntitiesBrowser myEntitiesBrowser;
	private LicensedDownloader licensedDownloader;
	private Widget downloadButton = null;
	private EntityTypeProvider typeProvider;
	private boolean readOnly;

	
	private Button editButton;
	private Button shareButton;
	private Button addButton;
	private Button toolsButton;
	
		
	@Inject
	public ActionMenuViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			AccessMenuButton accessMenuButton,
			AccessControlListEditor accessControlListEditor,
			LocationableUploader locationableUploader, MyEntitiesBrowser myEntitiesBrowser, LicensedDownloader licensedDownloader, EntityTypeProvider typeProvider) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.accessMenuButton = accessMenuButton;	
		this.accessControlListEditor = accessControlListEditor;
		this.locationableUploader = locationableUploader;
		this.myEntitiesBrowser = myEntitiesBrowser;
		this.licensedDownloader = licensedDownloader;
		this.typeProvider = typeProvider;

//		this.setLayout(new FitLayout());
		this.setHorizontalAlign(HorizontalAlignment.RIGHT);
		this.setTableWidth("100%");
	}

	@Override
	public void createMenu(Entity entity, EntityType entityType, boolean isAdministrator,
			boolean canEdit, boolean readOnly) {
		this.readOnly = readOnly;
		
		if(downloadButton == null){
			downloadButton = licensedDownloader.asWidget(entity);
			downloadButton.setHeight("25px");
			add(downloadButton);
			this.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));	
		}
		if (downloadButton instanceof Button) {
			Button dlButton = (Button)downloadButton;
			if (entity instanceof Locationable)
				dlButton.enable();
			else
				dlButton.disable(); 
		}
		// Configure the button
		licensedDownloader.configureHeadless(entity);


		// edit button
		if(editButton == null) {			
			editButton = new Button(DisplayConstants.BUTTON_EDIT, AbstractImagePrototype.create(iconsImageBundle.editGrey16()));
			editButton.setHeight(25);
			this.add(editButton);
			this.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));			
		}				
		if (canEdit && !readOnly) editButton.enable();
		else editButton.disable();
		configureEditButton(entity, entityType);	
		
		// share button
		if(shareButton == null) { 
			shareButton = new Button(DisplayConstants.BUTTON_SHARE, AbstractImagePrototype.create(iconsImageBundle.mailGrey16()));
			shareButton.setHeight(25);
			this.add(shareButton);
			this.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
		}
		if (isAdministrator && !readOnly) shareButton.enable();
		else shareButton.disable();
		configureShareButton(entity);		

		// add Button
		if(addButton == null) {
			addButton = new Button(DisplayConstants.BUTTON_ADD, AbstractImagePrototype.create(iconsImageBundle.add16()));
			addButton.setHeight(25);
			this.add(addButton);
			this.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
		}
		if (canEdit && !readOnly) addButton.enable();
		else addButton.disable();
		configureAddMenu(entity, entityType);

		if(toolsButton == null) {
			toolsButton = new Button(DisplayConstants.BUTTON_TOOLS_MENU, AbstractImagePrototype.create(iconsImageBundle.adminToolsGrey16()));
			toolsButton.setHeight(25);
			this.add(toolsButton);	
		}							
		configureToolsMenu(entity, entityType, isAdministrator, canEdit);
	}
	
	@Override
	public Widget asWidget() {
		return this;
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
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		if(editButton != null) editButton.removeAllListeners();
		if(shareButton != null) shareButton.removeAllListeners();	
	}
	
	/*
	 * Private Methods
	 */
	private void configureEditButton(final Entity entity, EntityType entityType) {
		editButton.removeAllListeners();
		editButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				// the presenter should handle this
				presenter.onEdit();
			}
		});		
	}
	
	private void configureShareButton(Entity entity) {		
		accessControlListEditor.setResource(entity);
		shareButton.removeAllListeners();		
		shareButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Window window = new Window();  
				window.setSize(550, 465);
				window.setPlain(true);
				window.setModal(true);
				window.setBlinkModal(true);
				window.setHeading(DisplayConstants.TITLE_SHARING_PANEL);
				window.setLayout(new FitLayout());
				window.add(accessControlListEditor.asWidget(), new FitData(4));
				Button closeButton = new Button("Close");
				closeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
				});
				window.setButtonAlign(HorizontalAlignment.RIGHT);
				window.addButton(closeButton);
				window.show();
			}
		});		
	}
	
	private void configureAddMenu(final Entity entity, final EntityType entityType) {		
		// create add menu button from children
		Menu menu = new Menu();		
		int numAdded = 0;
		
		List<EntityType> children = entityType.getValidChildTypes();
		List<EntityType> skipTypes = presenter.getAddSkipTypes();		
		if(children != null) {			 
			// add child tabs in order
			for(EntityType child : DisplayUtils.orderForDisplay(children)) {
				if(skipTypes.contains(child)) continue; // skip some types
				menu.add(createAddMenuItem(child, entity));
				numAdded++;
			}
		}
			
		if(numAdded==0) {
			addButton.disable();
		}
		addButton.setMenu(menu);
	}

	private MenuItem createAddMenuItem(final EntityType childType, final Entity entity) {
		String displayName = typeProvider.getEntityDispalyName(childType);			
		MenuItem item = new MenuItem(displayName);				
		item.setIcon(AbstractImagePrototype.create(DisplayUtils
				.getSynapseIconForEntityType(childType, IconSize.PX16,
						iconsImageBundle)));				
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				presenter.addNewChild(childType, entity.getId());
			}
		});
		return item;
	}
	
	private void configureToolsMenu(Entity entity, EntityType entityType, boolean isAdministrator, boolean canEdit) {
		toolsButton.enable();
		
		// create drop down menu
		Menu menu = new Menu();
		int numAdded = 0;
		// add restricted items to the Tools menu
		if(canEdit && !readOnly) {
			numAdded += addCanEditToolMenuItems(menu, entity, entityType);
		}
		// add tools for logged in users		
		if(presenter.isUserLoggedIn()) {
			numAdded += addAuthenticatedToolMenuItems(menu, entity, entityType);
		}

		if(isAdministrator && !readOnly) {
			numAdded += addIsAdministratorToolMenuItems(menu, entity, entityType);
		}

		toolsButton.setMenu(menu);
		if(numAdded == 0) {
			toolsButton.disable();
		}
	}

	private int addAuthenticatedToolMenuItems(Menu menu, Entity entity,EntityType entityType) {
		int numAdded = 0;
		
		// Create shortcut
		MenuItem item = new MenuItem(DisplayConstants.LABEL_CREATE_LINK);
		item.setIcon(AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntityClassName(Link.class.getName(), IconSize.PX16, iconsImageBundle)));		
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {				
				final Window window = new Window();  

				EntityTreeBrowser tree = myEntitiesBrowser.getEntityTreeBrowser();
				tree.setMakeLinks(false);
				tree.setShowContextMenu(false);
				myEntitiesBrowser.setEntitySelectedHandler(new SelectedHandler() {					
					@Override
					public void onSelection(String selectedEntityId) {
						presenter.createLink(selectedEntityId);
						window.hide();
					}
				});
				
				window.setSize(483, 329);
				window.setPlain(true);
				window.setModal(true);
				window.setBlinkModal(true);
				window.setHeading(DisplayConstants.LABEL_WHERE_SAVE_LINK);
				window.setLayout(new FitLayout());
				window.add(myEntitiesBrowser.asWidget(), new FitData(4)); 				
				window.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
				}));
				window.setButtonAlign(HorizontalAlignment.CENTER);
				window.show();

			}
		});
		menu.add(item);
		numAdded++;		
		
		return numAdded;
	}

	/**
	 * Administrator Menu Options
	 * @param menu
	 * @param entityType 
	 */
	private int addIsAdministratorToolMenuItems(Menu menu, Entity entity, EntityType entityType) {
		int numAdded = 0;
		final String typeDisplay = typeProvider.getEntityDispalyName(entityType);
		
		// Move entity
		MenuItem itemMove = new MenuItem(DisplayConstants.LABEL_MOVE + " " + typeDisplay);
		itemMove.setIcon(AbstractImagePrototype.create(iconsImageBundle.moveButton16()));		
		itemMove.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {				
				final Window window = new Window();  

				EntityTreeBrowser tree = myEntitiesBrowser.getEntityTreeBrowser();
				tree.setMakeLinks(false);
				tree.setShowContextMenu(false);
				myEntitiesBrowser.setEntitySelectedHandler(new SelectedHandler() {					
					@Override
					public void onSelection(String selectedEntityId) {
						presenter.moveEntity(selectedEntityId);
						window.hide();
					}
				});
				
				window.setSize(483, 329);
				window.setPlain(true);
				window.setModal(true);
				window.setBlinkModal(true);
				window.setHeading(DisplayConstants.LABEL_MOVE + " " + typeDisplay);
				window.setLayout(new FitLayout());
				window.add(myEntitiesBrowser.asWidget(), new FitData(4)); 				
				window.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
				}));
				window.setButtonAlign(HorizontalAlignment.CENTER);
				window.show();

			}
		});
		menu.add(itemMove);
		numAdded++;	
		
		// Delete entity
		MenuItem itemDelete = new MenuItem(DisplayConstants.LABEL_DELETE + " " + typeDisplay);
		itemDelete.setIcon(AbstractImagePrototype.create(iconsImageBundle.deleteButton16()));
		itemDelete.addSelectionListener(new SelectionListener<MenuEvent>() {
			public void componentSelected(MenuEvent menuEvent) {
				MessageBox.confirm(DisplayConstants.LABEL_DELETE +" " + typeDisplay, DisplayConstants.PROMPT_SURE_DELETE + " " + typeDisplay +"?", new Listener<MessageBoxEvent>() {					
					@Override
					public void handleEvent(MessageBoxEvent be) { 					
						Button btn = be.getButtonClicked();
						if(Dialog.YES.equals(btn.getItemId())) {
							presenter.deleteEntity();
						}
					}
				});
			}
		});
		menu.add(itemDelete);		
		numAdded++;	
		
		return numAdded;
	}

	/**
	 * Edit menu options
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private int addCanEditToolMenuItems(Menu menu, final Entity entity, EntityType entityType) {		
		int count = 0;

		// add uploader
		if(entity instanceof Locationable) {
			MenuItem item = new MenuItem(DisplayConstants.TEXT_UPLOAD_FILE);
			item.setIcon(AbstractImagePrototype.create(iconsImageBundle.NavigateUp16()));
			final Window window = new Window();  
			locationableUploader.addPersistSuccessHandler(new EntityUpdatedHandler() {				
				@Override
				public void onPersistSuccess(EntityUpdatedEvent event) {
					window.hide();
					presenter.fireEntityUpdatedEvent();
				}
			});
			locationableUploader.addCancelHandler(new CancelHandler() {				
				@Override
				public void onCancel(CancelEvent event) {
					window.hide();
				}
			});
			item.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					window.removeAll();
					window.setSize(400, 170);
					window.setPlain(true);
					window.setModal(true);		
					window.setBlinkModal(true);
					window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE);
					window.setLayout(new FitLayout());			
					window.add(locationableUploader.asWidget(entity, true), new MarginData(5));
					window.show();
				}
			});			
			menu.add(item);
			count++;
		}
		
		return count;
	}

}
