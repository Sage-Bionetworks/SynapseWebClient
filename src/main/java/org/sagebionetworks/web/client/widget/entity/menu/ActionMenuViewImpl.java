package org.sagebionetworks.web.client.widget.entity.menu;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
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
import org.sagebionetworks.web.client.widget.entity.ComboValue;
import org.sagebionetworks.web.client.widget.entity.EvaluationList;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
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
	private EvaluationList evaluationList;
	private Long versionNumber;
	private Button submitButton;
	private SimplePanel submitButtonPanel;
	private Button editButton;
	private Button shareButton;
	private Button toolsButton;
	private Button deleteButton;
	private boolean isInTestMode;
	private EntityBundle entityBundle;
	private ComboBox<ComboValue> submitterCombo;
	
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
		this.evaluationList = evaluationList;
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
		
		// share button
		if(shareButton == null) { 
			shareButton = new Button(DisplayConstants.BUTTON_SHARE, AbstractImagePrototype.create(iconsImageBundle.mailGrey16()));
			shareButton.setId(DisplayConstants.ID_BTN_SHARE);
			shareButton.setHeight(25);
			shareButton.addStyleName("floatright margin-left-5");
			this.add(shareButton);
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
		
		if(submitButton == null) {
			submitButtonPanel = new SimplePanel();
			submitButton = new Button(DisplayConstants.LABEL_SUBMIT_TO_EVALUATION, AbstractImagePrototype.create(iconsImageBundle.synapseStep16()));
			submitButton.setId(DisplayConstants.ID_BTN_SUBMIT_TO_EVALUATION);
			submitButton.setHeight(25);
			submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

				@Override
				public void componentSelected(ButtonEvent ce) {
					//ask the presenter to query for all available evaluations, and it may call the view back for the user to select evaluation(s) to submit to
					presenter.showAvailableEvaluations();
				}
			});
			submitButtonPanel.addStyleName("floatright margin-left-5");
			this.add(submitButtonPanel);
			//this.add(new HTML(SafeHtmlUtils.fromSafeConstant("&nbsp;")));
		}
		
		submitButtonPanel.clear();
		if (canEdit && entity instanceof Versionable) {
			//kick off the query (asynchronously) to determine if the current user is signed up for any challenges
			//will call back to the view to set submit button visibility
			presenter.isSubmitButtonVisible();
		}
		
		if (canEdit) editButton.enable();
		else editButton.disable();
		configureEditButton(entity, entityType);	
		
		if (isAdministrator) shareButton.enable();
		else shareButton.disable();
		configureShareButton(entity);		
		
		
		if (isAdministrator)
			deleteButton.enable();
		else deleteButton.disable();
		configureDeleteButton(entityType);
		
		configureToolsMenu(entityBundle, entityType, isAdministrator, canEdit);
	}
	
	@Override
	public void showSubmitToChallengeButton() {
		submitButtonPanel.add(submitButton);
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
	private void configureShareButton(Entity entity) {		
		accessControlListEditor.setResource(entity);
		shareButton.removeAllListeners();		
		shareButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Dialog window = new Dialog();
				
				// configure layout
				window.setSize(560, 465);
				window.setPlain(true);
				window.setModal(true);
				window.setHeading(DisplayConstants.TITLE_SHARING_PANEL);
				window.setLayout(new FitLayout());
				window.add(accessControlListEditor.asWidget(), new FitData(4));			    
			    
				// configure buttons
				window.okText = "Save";
				window.cancelText = "Cancel";
			    window.setButtons(Dialog.OKCANCEL);
			    window.setButtonAlign(HorizontalAlignment.RIGHT);
			    window.setHideOnButtonClick(false);
				window.setResizable(false);
				
				// "Apply" button
				// TODO: Disable the "Apply" button if ACLEditor has no unsaved changes
				Button applyButton = window.getButtonById(Dialog.OK);
				applyButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						// confirm close action if there are unsaved changes
						if (accessControlListEditor.hasUnsavedChanges()) {
							accessControlListEditor.pushChangesToSynapse(false, new AsyncCallback<EntityWrapper>() {
								@Override
								public void onSuccess(EntityWrapper result) {
									presenter.fireEntityUpdatedEvent();
								}
								@Override
								public void onFailure(Throwable caught) {
									//failure notification is handled by the acl editor view.
								}
							});
						}
						window.hide();
					}
			    });
				
				// "Close" button				
				Button closeButton = window.getButtonById(Dialog.CANCEL);
			    closeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
			    });
				
				window.show();
			}
		});		
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
		
		// create link
		if(authenticated) {
			addCreateShortcutItem(menu, entity, entityType);
		}
		// move
		if (canEdit) {
			addMoveItem(menu, entity, entityType);
		}

		if(isInTestMode && (entity instanceof Locationable || entity instanceof FileEntity)) {
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
			window.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					window.hide();
				}
			}));
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
	
	@Override
	public void popupEvaluationSelector(List<Evaluation> list, List<String> submitterAliases) {
		final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(450, 265);
        window.setPlain(true); 
        window.setModal(true); 
        
        window.setHeading("Evaluation Selection"); 
        window.setButtons(Dialog.OKCANCEL);
        window.setHideOnButtonClick(false);

        window.setLayout(new FitLayout());
        
        evaluationList.configure(list);
        submitterCombo = getSubmitterAliasComboBox(submitterAliases);
        //ok button submits if valid
        Button okButton = window.getButtonById(Dialog.OK);	    
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (submitterCombo.isValid()) {
					window.hide();
					presenter.submitToEvaluations(evaluationList.getSelectedEvaluationIds(), submitterCombo.getRawValue());
				} else {
					showErrorMessage(submitterCombo.getErrorMessage());
				}
					
			}
	    });
        
        //cancel button simply hides
        Button cancelButton = window.getButtonById(Dialog.CANCEL);	    
	    cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
	    });
	    
        FlowPanel panel = new FlowPanel();
        panel.add(new HTML("<h6 class=\"margin-top-left-10\">Select the Evaluations below that you would like to submit to:</h6>"));
        panel.add(evaluationList.asWidget());
        panel.add(new HTML("<h6 class=\"margin-top-left-10\">Set "+DisplayConstants.SUBMITTER_ALIAS+" to be shown in the public leaderboard:</h6>"));
        panel.add(submitterCombo);
	    window.add(panel);
	    window.show();
	}
	
	public ComboBox<ComboValue> getSubmitterAliasComboBox(List<String> submitterAliases) {
		// build the list store from the enum
		ListStore<ComboValue> store = new ListStore<ComboValue>();
		for (String value : submitterAliases) {
			ComboValue comboValue = new ComboValue(value);
			store.add(comboValue);
		}
		final ComboBox<ComboValue> combo = new ComboBox<ComboValue>();
		combo.setDisplayField(ComboValue.VALUE_KEY);
		combo.setWidth(400);
		combo.addStyleName("margin-left-10");
		combo.getMessages().setBlankText("Please set " + DisplayConstants.SUBMITTER_ALIAS);
		combo.setStore(store);
		combo.setEditable(true);
		combo.setEmptyText("Set "+DisplayConstants.SUBMITTER_ALIAS+"...");
		combo.setAllowBlank(false);
		if(store.getCount() > 0)
			combo.setValue(store.getAt(0));
		combo.setTriggerAction(TriggerAction.ALL);
		return combo;
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