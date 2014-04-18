package org.sagebionetworks.web.client.widget.entity.menu;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
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
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.DropdownButton;
import org.sagebionetworks.web.client.widget.entity.EvaluationList;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import org.sagebionetworks.web.shared.EntityType;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
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
	private EntityBundle entityBundle;
	private Long versionNumber;
	private boolean isInTestMode;
	
	private Button shareButton;	
	private DropdownButton toolsButton;
	private PublicPrivateBadge publicPrivateBadge;
	private String typeDisplay;
	
	@Inject
	public ActionMenuViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			AccessControlListEditor accessControlListEditor,
			Uploader locationableUploader, 
			EntityTypeProvider typeProvider,
			SynapseJSNIUtils synapseJSNIUtils,
			EntityFinder entityFinder,
			EvaluationList evaluationList,
			PublicPrivateBadge publicPrivateBadge) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.accessControlListEditor = accessControlListEditor;
		this.uploader = locationableUploader;
		this.typeProvider = typeProvider;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.entityFinder = entityFinder;
		this.publicPrivateBadge = publicPrivateBadge;
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
		if(toolsButton != null) this.remove(toolsButton);
		if(shareButton != null) this.remove(shareButton);
		this.versionNumber = versionNumber;
		this.entityBundle = entityBundle;
		this.isInTestMode = isInTestMode;
		Entity entity = entityBundle.getEntity();
		typeDisplay = typeProvider.getEntityDispalyName(entityType);
				
		// Share
		shareButton = DisplayUtils.createIconButton(DisplayConstants.BUTTON_SHARE, ButtonType.DEFAULT, "glyphicon-lock");
		shareButton.getElement().setId(DisplayConstants.ID_BTN_SHARE);
		shareButton.addStyleName("pull-right margin-left-5");
		configureShareButton(entity, isAdministrator);				
		
		// Tools
		toolsButton = new DropdownButton(DisplayConstants.BUTTON_TOOLS_MENU, ButtonType.DEFAULT, "glyphicon-cog");
		toolsButton.addStyleName("pull-right margin-left-5");
		configureToolsMenu(entityBundle, entityType, isAdministrator, canEdit);

		this.add(toolsButton);	
		this.add(shareButton);
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
	}
	
	/*
	 * Private Methods
	 */	
	private void configureShareButton(Entity entity, final boolean isAdministrator) { 
		publicPrivateBadge.configure(entity, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isPublic) {
				if(isPublic) {
					DisplayUtils.relabelIconButton(shareButton, DisplayConstants.BUTTON_SHARE, "glyphicon-globe");
				} else {
					DisplayUtils.relabelIconButton(shareButton, DisplayConstants.BUTTON_SHARE, "glyphicon-lock");
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.relabelIconButton(shareButton, DisplayConstants.BUTTON_SHARE, null);
			}
		});
		
		accessControlListEditor.setResource(entity, isAdministrator);  
		shareButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showSharingDialog(accessControlListEditor, isAdministrator, new Callback() {
					@Override
					public void invoke() {
						presenter.fireEntityUpdatedEvent();
					}
				});
			}
		});
	}
	
	private void configureToolsMenu(EntityBundle entityBundle,
			EntityType entityType, boolean isAdministrator, boolean canEdit) {
		boolean authenticated = presenter.isUserLoggedIn();
		Entity entity = entityBundle.getEntity();
		
		// upload
		if(canEdit) {
			addRenameItem(toolsButton);
			addUploadItem(toolsButton, entityBundle, entityType);
		}
		
		if (canEdit && entity instanceof Versionable) {
			addSubmitToEvaluationItem(toolsButton, entity, entityType);
		} 
		
		// create link
		if(authenticated) {
			addCreateShortcutItem(toolsButton, entity, entityType);
		}
		// move
		if (canEdit && !(entityBundle.getEntity() instanceof Project)) {
			addMoveItem(toolsButton, entity, entityType);
		}

		if(entity instanceof Locationable || entity instanceof FileEntity) {
			addUploadToGenomeSpace(toolsButton, entityBundle);
		}
		
		// put delete last
		if(canEdit) {
			addDeleteItem(toolsButton, typeDisplay);
		}
		
		toolsButton.setVisible(toolsButton.getCount() > 0);
	}

	/**
	 * 'Delete Entity' item
	 * @param entityType 
	 */	
	private void addDeleteItem(DropdownButton menuBtn, final String typeDisplay) {
		Anchor a = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIcon("glyphicon-trash") + " "
						+ DisplayConstants.LABEL_DELETE + " " + typeDisplay));
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				MessageBox.confirm(DisplayConstants.LABEL_DELETE +" " + typeDisplay, DisplayConstants.PROMPT_SURE_DELETE + " " + typeDisplay +"?", new Listener<MessageBoxEvent>() {					
					@Override
					public void handleEvent(MessageBoxEvent be) { 					
						com.extjs.gxt.ui.client.widget.button.Button btn = be.getButtonClicked();
						if(Dialog.YES.equals(btn.getItemId())) {
							presenter.deleteEntity();
						}
					}
				});
			}
		});
		menuBtn.addMenuItem(a);
	}
	
	private void addRenameItem(DropdownButton menuBtn) {
		Anchor a = new Anchor(DisplayConstants.BUTTON_EDIT);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEdit();
			}
		});
		menuBtn.addMenuItem(a);
	}
	
	/**
	 * 'Upload File' item
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private void addUploadItem(DropdownButton menuBtn, final EntityBundle entityBundle, EntityType entityType) {
		//if this is a FileEntity, then only show the upload item if we're in the test website
		boolean isFileEntity = entityBundle.getEntity() instanceof FileEntity;
		if(isFileEntity || entityBundle.getEntity() instanceof Locationable) {
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
			Anchor a = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIcon("glyphicon-arrow-up") + " " + DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK));
			a.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					window.removeAll();
					window.setPlain(true);
					window.setModal(true);		
					window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
					window.setLayout(new FitLayout());			
					window.add(uploader.asWidget(entityBundle.getEntity(), entityBundle.getAccessRequirements()), new MarginData(5));
					window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
					window.show();
				}
			});
			menuBtn.addMenuItem(a);
		}
	}
		
	/**
	 * 'Create Link' item
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private void addCreateShortcutItem(DropdownButton menuBtn, Entity entity,EntityType entityType) {	
		// Create shortcut
//		item.setIcon(AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntityClassName(Link.class.getName(), IconSize.PX16, iconsImageBundle)));		
		Anchor a = new Anchor(DisplayConstants.LABEL_CREATE_LINK);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
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
		menuBtn.addMenuItem(a);		
	}

	private void addSubmitToEvaluationItem(DropdownButton menuBtn, Entity entity,EntityType entityType) {
//		item.setIcon(AbstractImagePrototype.create(iconsImageBundle.synapseStep16()));
		Anchor a = new Anchor(DisplayConstants.LABEL_SUBMIT_TO_EVALUATION);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				//ask the presenter to query for all available evaluations, and it may call the view back for the user to select evaluation(s) to submit to
				presenter.showAvailableEvaluations();
			}
		});
		menuBtn.addMenuItem(a);
	}
	
	/**
	 * 'Move Entity' item
	 * @param menu
	 * @param entity 
	 * @param entityType 
	 */
	private void addMoveItem(DropdownButton menuBtn, final Entity entity, EntityType entityType) {		
//		itemMove.setIcon(AbstractImagePrototype.create(iconsImageBundle.moveButton16()));		
		Anchor a = new Anchor(DisplayConstants.LABEL_MOVE + " " + typeDisplay);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
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
		menuBtn.addMenuItem(a);
	}

	private void addUploadToGenomeSpace(final DropdownButton menuBtn, final EntityBundle bundle) {
		Anchor a = new Anchor(SafeHtmlUtils.fromSafeConstant("Upload to " + AbstractImagePrototype.create(sageImageBundle.genomeSpaceLogoTitle16()).getHTML()));
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.uploadToGenomespace();
			}
		});
		menuBtn.addMenuItem(a);	}
}




