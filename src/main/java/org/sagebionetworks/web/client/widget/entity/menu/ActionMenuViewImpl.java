package org.sagebionetworks.web.client.widget.entity.menu;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.DropdownButton;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidget;
import org.sagebionetworks.web.client.widget.entity.EvaluationList;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import org.sagebionetworks.web.shared.EntityType;

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
	private UploadDialogWidget uploader;
	private EntityTypeProvider typeProvider;
	private EntityFinder entityFinder;
	private QuizInfoDialog quizInfoDialog;
	private EntityAccessRequirementsWidget accessRequirementsWidget;
	private SynapseClientAsync synapseClient;
	private CookieProvider cookies;
	private AuthenticationController authenticationController;
	
	private Button shareButton;	
	private DropdownButton toolsButton;
	private PublicPrivateBadge publicPrivateBadge;
	private String typeDisplay;
	private Anchor addDescriptionCommand;
	private Callback addDescriptionCallback;
	private EntityBundle entityBundle;
	private AccessControlListModalWidget accessControlListModalWidget;
	
	@Inject
	public ActionMenuViewImpl(SageImageBundle sageImageBundle,
			UploadDialogWidget locationableUploader, 
			EntityTypeProvider typeProvider,
			EntityFinder entityFinder,
			EvaluationList evaluationList,
			PublicPrivateBadge publicPrivateBadge,
			QuizInfoDialog quizInfoDialog,
			EntityAccessRequirementsWidget accessRequirementsWidget,
			SynapseClientAsync synapseClient,
			CookieProvider cookies,
			AuthenticationController authenticationController,
			AccessControlListModalWidget accessControlListModalWidget) {
		this.sageImageBundle = sageImageBundle;
		this.uploader = locationableUploader;
		locationableUploader.disableMultipleFileUploads();
		this.typeProvider = typeProvider;
		this.entityFinder = entityFinder;
		this.publicPrivateBadge = publicPrivateBadge;
		this.quizInfoDialog = quizInfoDialog;
		this.accessRequirementsWidget = accessRequirementsWidget;
		this.synapseClient = synapseClient;
		this.cookies = cookies;
		this.authenticationController = authenticationController;
		this.accessControlListModalWidget = accessControlListModalWidget;
		add(uploader.asWidget()); //add uploader dialog to page
	}
	@Override
	public void createMenu(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController,
			Long versionNumber,
			boolean isInTestMode) {
		if(toolsButton != null) this.remove(toolsButton);
		if(shareButton != null) this.remove(shareButton);
		Entity entity = entityBundle.getEntity();
		typeDisplay = typeProvider.getEntityDispalyName(entityType);
				
		// Share
		shareButton = DisplayUtils.createIconButton(DisplayConstants.BUTTON_SHARE, ButtonType.DEFAULT, "glyphicon-lock");
		shareButton.getElement().setId(DisplayConstants.ID_BTN_SHARE);
		shareButton.addStyleName("pull-right margin-left-5");
		configureShareButton(entity, entityBundle.getPermissions().getCanChangePermissions());				
		
		// Tools
		toolsButton = new DropdownButton(DisplayConstants.BUTTON_TOOLS_MENU, ButtonType.DEFAULT, "glyphicon-cog");
		toolsButton.addStyleName("pull-right margin-left-5");
		configureToolsMenu(entityBundle, entityType);

		this.add(toolsButton);	
		this.add(shareButton);
		
		//add quiz info dialog to the DOM
		toolsButton.add(quizInfoDialog.asWidget());
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
		final String shareButtonText = isAdministrator ? DisplayConstants.BUTTON_SHARE : DisplayConstants.BUTTON_SHARING;
		publicPrivateBadge.configure(entity, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isPublic) {
				if(isPublic) {
					DisplayUtils.relabelIconButton(shareButton, shareButtonText, "glyphicon-globe");
				} else {
					DisplayUtils.relabelIconButton(shareButton, shareButtonText, "glyphicon-lock");
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.relabelIconButton(shareButton, shareButtonText, null);
			}
		});
		
		accessControlListModalWidget.configure(entity, isAdministrator);
		shareButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				accessControlListModalWidget.showSharing(new Callback() {
					@Override
					public void invoke() {
						presenter.fireEntityUpdatedEvent();
					}
				});
			}
		});
	}
	
	private void configureToolsMenu(EntityBundle entityBundle,
			EntityType entityType) {
		boolean authenticated = presenter.isUserLoggedIn();
		Entity entity = entityBundle.getEntity();
		UserEntityPermissions permissions = entityBundle.getPermissions();
		// upload
		if (permissions.getCanCertifiedUserEdit()) {
			addRenameItem(toolsButton);
		}
		
		if(permissions.getCanCertifiedUserAddChild()) {
			initAddDescriptionItem(toolsButton);
			addUploadItem(toolsButton, entityBundle, entityType);
		}
		
		if (permissions.getCanCertifiedUserAddChild() && entity instanceof Versionable) {
			addSubmitToEvaluationItem(toolsButton, entity, entityType);
		} 
		
		// create link
		if(authenticated) {
			addCreateShortcutItem(toolsButton, entity, entityType);
		}
		// move
		if (permissions.getCanCertifiedUserAddChild() && !(entityBundle.getEntity() instanceof Project)) {
			addMoveItem(toolsButton, entity, entityType);
		}

		if(entity instanceof Locationable || entity instanceof FileEntity) {
			addUploadToGenomeSpace(toolsButton, entityBundle);
		}
		
		// put delete last
		if(permissions.getCanDelete()) {
			addDeleteItem(toolsButton, entity, typeDisplay);
		}
		
		toolsButton.setVisible(toolsButton.getCount() > 0);
	}

	/**
	 * 'Delete Entity' item
	 * @param entityType 
	 */	
	private void addDeleteItem(DropdownButton menuBtn, final Entity entity, final String typeDisplay) {
		Anchor a = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIcon("glyphicon-trash") + " "
						+ DisplayConstants.LABEL_DELETE + " " + typeDisplay));
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog(DisplayConstants.LABEL_DELETE +" " + typeDisplay, DisplayConstants.PROMPT_SURE_DELETE + " " + typeDisplay + " \"" + entity.getName() + "\"?", new Callback() {
					
					@Override
					public void invoke() {
						presenter.callbackIfCertifiedIfEnabled(new Callback() {
							@Override
							public void invoke() {
								presenter.deleteEntity();
							}
						});
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
		this.entityBundle = entityBundle;
		boolean isFileEntity = entityBundle.getEntity() instanceof FileEntity;
		if(isFileEntity || entityBundle.getEntity() instanceof Locationable) {
			Anchor a = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIcon("glyphicon-arrow-up") + " " + DisplayConstants.TEXT_UPLOAD_NEW_VERSION_FILE_OR_LINK));
			a.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					presenter.callbackIfCertifiedIfEnabled(new Callback() {
						@Override
						public void invoke() {
							UserEntityPermissions permissions = entityBundle.getPermissions();
							FilesBrowser.uploadButtonClickedStep1(accessRequirementsWidget, entityBundle.getEntity().getId(), ActionMenuViewImpl.this, synapseClient, authenticationController, permissions.getIsCertifiedUser());
						}
					});
				}
			});
			menuBtn.addMenuItem(a);
		}
	}
	
	@Override
	public void showUploadDialog(String entityId) {
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {				
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		};
		uploader.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, entityBundle.getEntity(), null, handler, null, true);
		uploader.disableMultipleFileUploads();
		uploader.show();
	}
	
	@Override
	public void showQuizInfoDialog() {
		quizInfoDialog.show();
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
				presenter.callbackIfCertifiedIfEnabled(new Callback() {
					@Override
					public void invoke() {
						createShortcut();
					}
				});
			}
		});
		menuBtn.addMenuItem(a);		
	}
	
	private void createShortcut() {
		entityFinder.configure(false, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				if(selected.getTargetId() != null) {
					presenter.createLink(selected.getTargetId());
					entityFinder.hide();
				} else {
					showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
				}
			}
		});					
		entityFinder.show();
	}
	
	private void addSubmitToEvaluationItem(DropdownButton menuBtn, Entity entity,EntityType entityType) {
//		item.setIcon(AbstractImagePrototype.create(iconsImageBundle.synapseStep16()));
		Anchor a = new Anchor(DisplayConstants.LABEL_SUBMIT_TO_EVALUATION);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.callbackIfCertifiedIfEnabled(new Callback() {
					@Override
					public void invoke() {
						//ask the presenter to query for all available evaluations, and it may call the view back for the user to select evaluation(s) to submit to
						presenter.showAvailableEvaluations();
					}
				});
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
				//only if certified
				presenter.callbackIfCertifiedIfEnabled(new Callback() {
					@Override
					public void invoke() {
						moveItem();
					}
				});
			}
		});
		menuBtn.addMenuItem(a);
	}

	private void moveItem() {
		entityFinder.configure(false, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				if(selected.getTargetId() != null) {
					presenter.moveEntity(selected.getTargetId());
					entityFinder.hide();
				} else {
					showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
				}
			}
		});
		entityFinder.show();
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

	@Override
	public void showAddDescriptionCommand(Callback onClick) {
		addDescriptionCallback = onClick;
		if (addDescriptionCommand != null)
			addDescriptionCommand.setVisible(true);
	}
	
	@Override
	public void hideAddDescriptionCommand() {
		if (addDescriptionCommand != null)
			addDescriptionCommand.setVisible(false);
	}

	private void initAddDescriptionItem(DropdownButton menuBtn) {
		addDescriptionCommand = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIcon("glyphicon-plus") + " "
				+ DisplayConstants.ADD_DESCRIPTION));

		hideAddDescriptionCommand();
		addDescriptionCallback = null;
		addDescriptionCommand.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.callbackIfCertifiedIfEnabled(new Callback() {
					@Override
					public void invoke() {
						if (addDescriptionCallback != null)
							addDescriptionCallback.invoke();
					}
				});
			}
		});
		menuBtn.addMenuItem(addDescriptionCommand);
	}

	/**
	 * Add the evaluation submitter widget to the page
	 */
	@Override
	public void setEvaluationSubmitterWidget(Widget widget) {
		add(widget);
	}

}




