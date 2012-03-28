package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.editpanels.NodeEditor;
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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ActionMenuViewImpl extends HorizontalPanel implements ActionMenuView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private NodeEditor nodeEditor;
	private AccessMenuButton accessMenuButton;
	private AccessControlListEditor accessControlListEditor;
	private LocationableUploader locationableUploader;
	private LicensedDownloader licensedDownloader;
	private Widget downloadButton = null;
	
	private Button editButton;
	private Button shareButton;
	private Button addButton;
	private Button toolsButton;
	
		
	@Inject
	public ActionMenuViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, NodeEditor nodeEditor,
			AccessMenuButton accessMenuButton,
			AccessControlListEditor accessControlListEditor,
			LocationableUploader locationableUploader, LicensedDownloader licensedDownloader) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.nodeEditor = nodeEditor;
		this.accessMenuButton = accessMenuButton;	
		this.accessControlListEditor = accessControlListEditor;
		this.locationableUploader = locationableUploader;
		this.licensedDownloader = licensedDownloader;
//		this.setLayout(new FitLayout());
		this.setHorizontalAlign(HorizontalAlignment.RIGHT);
		this.setTableWidth("100%");
	}

	@Override
	public void createMenu(Entity entity, EntityType entityType, boolean isAdministrator,
			boolean canEdit) {			
		int height = 25;
		if(downloadButton == null){
			downloadButton = licensedDownloader.asWidget(entity, false);
			downloadButton.setHeight("25px");
			add(downloadButton);
			this.add(new Html("&nbsp;"));	
		}
		// Configure the button
		licensedDownloader.configureHeadless(entity, false);

		// edit button
		if(editButton == null) {			
			editButton = new Button(DisplayConstants.BUTTON_EDIT, AbstractImagePrototype.create(iconsImageBundle.editGrey16()));
			editButton.setHeight(25);
			this.add(editButton);
			this.add(new Html("&nbsp;"));			
		}				
		if (canEdit) editButton.enable();
		else editButton.disable();
		configureEditButton(entity, entityType);	
		
		// share button
		if(shareButton == null) { 
			shareButton = new Button(DisplayConstants.BUTTON_SHARE, AbstractImagePrototype.create(iconsImageBundle.mailGrey16()));
			shareButton.setHeight(25);
			this.add(shareButton);
			this.add(new Html("&nbsp;"));
		}
		configureShareButton(entity);		
		if (isAdministrator) shareButton.enable();
		else shareButton.disable();

		// add Button
		if(addButton == null) {
			addButton = new Button(DisplayConstants.BUTTON_ADD, AbstractImagePrototype.create(iconsImageBundle.addGrey16()));
			addButton.setHeight(25);
			this.add(addButton);
			this.add(new Html("&nbsp;"));
		}
		configureAddMenu(entity, entityType);
		if (canEdit) addButton.enable();
		else addButton.disable();

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
		final String typeDisplay = DisplayUtils.uppercaseFirstLetter(entityType.getName());
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
		accessControlListEditor.setPlaceChanger(presenter.getPlaceChanger());
		accessControlListEditor.setResource(entity);
		shareButton.removeAllListeners();		
		shareButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Window window = new Window();  
				window.setSize(550, 440);
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
		MenuItem item;
		int numAdded = 0;
		
		List<EntityType> children = entityType.getValidChildTypes();
		if(children != null) {
			// TODO : sort children?
			List<EntityType> skipTypes = presenter.getAddSkipTypes();
			for(final EntityType childType : children) {
				if(skipTypes.contains(childType)) continue; // skip some types
				
				String displayName = DisplayUtils.uppercaseFirstLetter(childType.getName());			
				item = new MenuItem(displayName);
				// TODO : replace icon with entity type icon
				item.setIcon(AbstractImagePrototype.create(iconsImageBundle.documentAdd16()));
				item.addSelectionListener(new SelectionListener<MenuEvent>() {
					public void componentSelected(MenuEvent menuEvent) {
						presenter.addNewChild(childType, entity.getId());
					}
				});
				menu.add(item);
				numAdded++;
			}
		}
			
		if(numAdded==0) {
			addButton.disable();
		}
		addButton.setMenu(menu);
	}

	private void configureToolsMenu(Entity entity, EntityType entityType, boolean isAdministrator, boolean canEdit) {				
		// create drop down menu
		Menu menu = new Menu();
		int numAdded = 0;
		// add restricted items to the Tools menu
		if(canEdit) {
			numAdded += addCanEditToolMenuItems(menu, entity, entityType);
		}
		if(isAdministrator) {
			numAdded += addIsAdministratorToolMenuItems(menu, entity, entityType);
		}
		// add tools for logged in users		
		if(presenter.isUserLoggedIn()) {
			numAdded += addAuthenticatedToolMenuItems(menu, entity, entityType);
		}

		toolsButton.setMenu(menu);
		if(numAdded == 0) {
			toolsButton.disable();
		}
	}

	private int addAuthenticatedToolMenuItems(Menu menu, Entity entity,EntityType entityType) {
		int numAdded = 0;
		
		// Create shortcut
		MenuItem item = new MenuItem(DisplayConstants.LABEL_CREATE_SHORTCUT);
		item.setIcon(AbstractImagePrototype.create(iconsImageBundle.documentArrow16()));
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				// TODO : make this better. have it show you projects
				final MessageBox box = MessageBox.prompt(DisplayConstants.LABEL_CREATE_SHORTCUT,"Where would you like to save a shortcut to this page? Enter Synapse ID: ");
				box.addCallback(new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if(be.getValue() != null && !"".equals(be.getValue())) {
							presenter.createShortcut(be.getValue());
						}				
					}
				});
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
		final String typeDisplay = DisplayUtils.uppercaseFirstLetter(entityType.getName());
		MenuItem item = new MenuItem(DisplayConstants.LABEL_DELETE + " " + typeDisplay);
		item.setIcon(AbstractImagePrototype.create(iconsImageBundle.deleteButton16()));
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
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
		menu.add(item);		
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
