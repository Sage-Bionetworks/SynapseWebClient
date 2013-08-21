package org.sagebionetworks.web.client.widget.entity.menu;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.EvaluationList;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.EntityType;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ActionMenuViewImpl extends FlowPanel implements ActionMenuView {

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private AccessControlListEditor accessControlListEditor;
	private Uploader uploader;
	private EntityTypeProvider typeProvider;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityFinder entityFinder;
	
	private Long versionNumber;
	private Button editButton;
	
	private Button toolsButton;
	private Button deleteButton;
	private boolean isInTestMode;
	private EntityBundle entityBundle;
	private MenuItem doiItem;
	
	@Inject
	public ActionMenuViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			AccessMenuButton accessMenuButton,
			AccessControlListEditor accessControlListEditor,
			Uploader locationableUploader, 
			EntityTypeProvider typeProvider,
			SynapseJSNIUtils synapseJSNIUtils,
			EntityFinder entityFinder,
			EvaluationList evaluationList) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.accessControlListEditor = accessControlListEditor;
		this.uploader = locationableUploader;
		this.typeProvider = typeProvider;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.entityFinder = entityFinder;
	}

	@Override
	public void createMenu(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController,
			boolean isAdministrator,
			boolean canEdit, 
			Long versionNumber,
			boolean isInTestMode) {
		this.versionNumber = versionNumber;
		this.entityBundle = entityBundle;
		this.isInTestMode = isInTestMode;
		Entity entity = entityBundle.getEntity();
		
		if(deleteButton == null) {
			deleteButton = getDeleteButton(entityType);
			this.add(deleteButton);
		}
		
		if(toolsButton == null) {
			toolsButton = new Button(DisplayConstants.BUTTON_TOOLS_MENU, AbstractImagePrototype.create(iconsImageBundle.adminToolsGrey16()));
			toolsButton.setHeight(25);
			toolsButton.addStyleName("floatright margin-left-5");
			this.add(toolsButton);	
			//this.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
		}
		
		// edit button
		if(editButton == null) {			
			editButton = new Button(DisplayConstants.BUTTON_EDIT, AbstractImagePrototype.create(iconsImageBundle.editGrey16()));
			editButton.setId(DisplayConstants.ID_BTN_EDIT);
			editButton.setHeight(25);
			editButton.addStyleName("floatright margin-left-5");
			this.add(editButton);
			//this.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));			
		}	
		
		if (canEdit) editButton.enable();
		else editButton.disable();
		configureEditButton(entity, entityType);	
		
		if (isAdministrator)
			deleteButton.enable();
		else deleteButton.disable();
		configureDeleteButton(entityType);
		
		configureToolsMenu(entityBundle, entityType, isAdministrator, canEdit);
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
	public void enableDoiCreation(boolean enable) {
		doiItem.setEnabled(enable);
	}
	
	@Override
	public void clear() {
		if(editButton != null) editButton.removeAllListeners();
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

	private void configureDeleteButton(EntityType entityType) {
		final String typeDisplay = typeProvider.getEntityDispalyName(entityType);
		deleteButton.removeAllListeners();
		deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				handleDeleteClick(typeDisplay);
			}
		});
	}
	
	private void configureToolsMenu(EntityBundle entityBundle, EntityType entityType, boolean isAdministrator, boolean canEdit) {
		toolsButton.enable();
		
		boolean authenticated = presenter.isUserLoggedIn();
		// disable edit/admin items if in read-only mode
		
		// create drop down menu
		Menu menu = new Menu();		
		
		Entity entity = entityBundle.getEntity();
		
		// upload
		if(canEdit) {
			addUploadItem(menu, entityBundle, entityType);
		}
		
		if (canEdit && entity instanceof Versionable) {
			addSubmitToEvaluationItem(menu, entity, entityType);
		} 
		
		// create link
		if(authenticated) {
			addCreateShortcutItem(menu, entity, entityType);
		}
		// move
		if (canEdit) {
			addMoveItem(menu, entity, entityType);
		}

		if (canEdit) {
			addCreateDoiItem(menu, entity, entityType);
		}
		
		if(entity instanceof Locationable || entity instanceof FileEntity) {
			addUploadToGenomeSpace(menu, entityBundle);
		}
		
		toolsButton.setMenu(menu);
		if(menu.getItemCount() == 0) {
			toolsButton.disable();
		}
	}

	/**
	 * 'Delete Entity' item
	 * @param entityType 
	 */
	private Button getDeleteButton(EntityType entityType) {
		Button deleteButton = new Button("", AbstractImagePrototype.create(iconsImageBundle.trash16()));
		deleteButton.setHeight(25);
		deleteButton.addStyleName("floatright margin-left-5");
		DisplayUtils.addTooltip(synapseJSNIUtils, deleteButton, DisplayConstants.LABEL_DELETE, TOOLTIP_POSITION.BOTTOM);
		return deleteButton;
	}
	
	private void handleDeleteClick(final String typeDisplay) {
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
	
	/**
	 * 'Upload File' item
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private void addUploadItem(Menu menu, final EntityBundle entityBundle, EntityType entityType) {
		//if this is a FileEntity, then only show the upload item if we're in the test website
		boolean isFileEntity = entityBundle.getEntity() instanceof FileEntity;
		if(isFileEntity || entityBundle.getEntity() instanceof Locationable) {
			MenuItem item = new MenuItem(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
			item.setIcon(AbstractImagePrototype.create(iconsImageBundle.NavigateUp16()));
			final Window window = new Window();
			uploader.clearHandlers();
			uploader.addPersistSuccessHandler(new EntityUpdatedHandler() {				
				@Override
				public void onPersistSuccess(EntityUpdatedEvent event) {
					window.hide();
					presenter.fireEntityUpdatedEvent();
				}
			});
			uploader.addCancelHandler(new CancelHandler() {				
				@Override
				public void onCancel(CancelEvent event) {
					window.hide();
				}
			});
			item.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {
					window.removeAll();
					window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
					window.setPlain(true);
					window.setModal(true);		
					window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
					window.setLayout(new FitLayout());			
					window.add(uploader.asWidget(entityBundle.getEntity(), entityBundle.getAccessRequirements()), new MarginData(5));
					window.show();
				}
			});			
			menu.add(item);
		}
	}
		
	/**
	 * 'Create Link' item
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private void addCreateShortcutItem(Menu menu, Entity entity,EntityType entityType) {	
		// Create shortcut
		MenuItem item = new MenuItem(DisplayConstants.LABEL_CREATE_LINK);
		item.setIcon(AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntityClassName(Link.class.getName(), IconSize.PX16, iconsImageBundle)));		
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {				
				entityFinder.configure(false);				
				final Window window = new Window();
				DisplayUtils.configureAndShowEntityFinderWindow(entityFinder, window, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {
							presenter.createLink(selected.getTargetId());
							window.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});					
			}
		});
		menu.add(item);
	}

	private void addSubmitToEvaluationItem(Menu menu, Entity entity,EntityType entityType) {
		MenuItem item = new MenuItem(DisplayConstants.LABEL_SUBMIT_TO_EVALUATION);
		item.setIcon(AbstractImagePrototype.create(iconsImageBundle.synapseStep16()));
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				//ask the presenter to query for all available evaluations, and it may call the view back for the user to select evaluation(s) to submit to
				presenter.showAvailableEvaluations();
			}
		});
		menu.add(item);
	}
	
	/**
	 * 'Move Entity' item
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private void addMoveItem(Menu menu, final Entity entity, EntityType entityType) {
		final String typeDisplay = typeProvider.getEntityDispalyName(entityType);
		MenuItem itemMove = new MenuItem(DisplayConstants.LABEL_MOVE + " " + typeDisplay);
		itemMove.setIcon(AbstractImagePrototype.create(iconsImageBundle.moveButton16()));		
		itemMove.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {				
				entityFinder.configure(false);				
				final Window window = new Window();
				DisplayUtils.configureAndShowEntityFinderWindow(entityFinder, window, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {
							presenter.moveEntity(selected.getTargetId());
							window.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});				
			}
		});
		menu.add(itemMove);
	}

	/**
	 * 'Create DOI' item
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private void addCreateDoiItem(Menu menu, final Entity entity, EntityType entityType) {
		if (doiItem == null) {
			doiItem = new MenuItem(DisplayConstants.LABEL_CREATE_DOI);
			doiItem.addSelectionListener(new SelectionListener<MenuEvent>() {
				@Override
				public void componentSelected(MenuEvent ce) {				
					presenter.createDoi();		
				}
			});
		}
		menu.add(doiItem);
		doiItem.setEnabled(false);
	}
	
	private void addUploadToGenomeSpace(final Menu menu, final EntityBundle bundle) {
		MenuItem item = new MenuItem("Upload to " + AbstractImagePrototype.create(sageImageBundle.genomeSpaceLogoTitle16()).getHTML());		
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				presenter.uploadToGenomespace();
			}
		});
		menu.add(item);
	}
}