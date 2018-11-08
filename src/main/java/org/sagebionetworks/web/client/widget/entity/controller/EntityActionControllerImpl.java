package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.CONTAINER;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.PROJECT;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModal;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageDeleteConfirmationDialog;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerImpl implements EntityActionController, ActionListener{
	
	public static final String TOOLS = " Tools";

	public static final String MOVE_PREFIX = "Move ";

	public static final String EDIT_WIKI_PREFIX = "Edit ";
	public static final String WIKI = " Wiki";
	
	public static final String THE = "The ";

	public static final String WAS_SUCCESSFULLY_DELETED = " was successfully deleted.";

	public static final String DELETED = "Deleted";

	public static final String ARE_YOU_SURE_YOU_WANT_TO_DELETE = "Are you sure you want to delete ";
	public static final String DELETE_FOLDER_EXPLANATION = " Everything contained within the Folder will also be deleted.";
	public static final String CONFIRM_DELETE_TITLE = "Confirm Delete";

	public static final String DELETE_PREFIX = "Delete ";
	
	public static final String RENAME_PREFIX = "Rename ";
	
	public static final int IS_ACT_MEMBER_MASK = 0x20;

	public static final String CREATE_DOI_FOR = "Create DOI for  ";
	public static final String UPDATE_DOI_FOR = "Update DOI for  ";

	EntityArea currentArea;
	
	EntityActionControllerView view;
	PreflightController preflightController;
	SynapseClientAsync synapseClient;
	SynapseJavascriptClient jsClient;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	AccessControlListModalWidget accessControlListModalWidget;
	RenameEntityModalWidget renameEntityModalWidget;
	EntityFinder entityFinder;
	EvaluationSubmitter submitter;
	EditFileMetadataModalWidget editFileMetadataModalWidget;
	EditProjectMetadataModalWidget editProjectMetadataModalWidget;
	EventBus eventBus;
	
	EntityBundle entityBundle;
	String wikiPageId;
	Entity entity;
	UserEntityPermissions permissions;
	String enityTypeDisplay;
	boolean isUserAuthenticated;
	boolean isCurrentVersion;
	ActionMenuWidget actionMenu;
	WikiMarkdownEditor wikiEditor;
	ProvenanceEditorWidget provenanceEditor;
	StorageLocationWidget storageLocationEditor;
	EvaluationEditorModal evalEditor;
	CookieProvider cookies;
	ChallengeClientAsync challengeClient;
	SelectTeamModal selectTeamModal;
	CreateOrUpdateDoiModal createOrUpdateDoiModal;
	ApproveUserAccessModal approveUserAccessModal;
	PortalGinInjector ginInjector;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	AddFolderDialogWidget addFolderDialogWidget;
	CreateTableViewWizard createTableViewWizard;
	boolean isShowingVersion = false;
	WizardCallback entityUpdatedWizardCallback;
	UploadTableModalWidget uploadTableModalWidget;
	AddExternalRepoModal addExternalRepoModal;
	String currentChallengeId;
	GWTWrapper gwt;
	Callback reconfigureActionsCallback;
	WikiPageDeleteConfirmationDialog wikiPageDeleteConfirmationDialog;
	@Inject
	public EntityActionControllerImpl(EntityActionControllerView view,
			PreflightController preflightController,
			PortalGinInjector ginInjector,
			AuthenticationController authenticationController,
			CookieProvider cookies,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			GWTWrapper gwt,
			EventBus eventBus) {
		super();
		this.view = view;
		this.ginInjector = ginInjector;
		this.preflightController = preflightController;
		this.authenticationController = authenticationController;
		this.cookies = cookies;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.gwt = gwt;
		this.eventBus = eventBus;
		entityUpdatedWizardCallback = new WizardCallback() {
			@Override
			public void onFinished() {
				fireEntityUpdatedEvent();
			}
			
			@Override
			public void onCanceled() {
			}
		};
		reconfigureActionsCallback = () -> {
			reconfigureActions();
		};
	}
	
	private void fireEntityUpdatedEvent() {
		eventBus.fireEvent(new EntityUpdatedEvent());
	}
	
	private WikiPageDeleteConfirmationDialog getWikiPageDeleteConfirmationDialog() {
		if (wikiPageDeleteConfirmationDialog == null) {
			wikiPageDeleteConfirmationDialog = ginInjector.getWikiPageDeleteConfirmationDialog();
			view.addWidget(wikiPageDeleteConfirmationDialog);
		}
		return wikiPageDeleteConfirmationDialog;
	}
	private AddFolderDialogWidget getAddFolderDialogWidget() {
		if (addFolderDialogWidget == null) {
			addFolderDialogWidget = ginInjector.getAddFolderDialogWidget();
		}
		return addFolderDialogWidget;
	}
	private ApproveUserAccessModal getApproveUserAccessModal() {
		if (approveUserAccessModal == null) {
			approveUserAccessModal = ginInjector.getApproveUserAccessModal();
		}
		return approveUserAccessModal;
	}
	private SelectTeamModal getSelectTeamModal() {
		if (selectTeamModal == null) {
			selectTeamModal = ginInjector.getSelectTeamModal();
			view.addWidget(selectTeamModal.asWidget());
			selectTeamModal.setTitle("Select Participant Team");
			selectTeamModal.configure(new CallbackP<String>() {
				@Override
				public void invoke(String selectedTeamId) {
					onSelectChallengeTeam(selectedTeamId);
				}
			});
		}
		return selectTeamModal;
	}
	
	private CreateOrUpdateDoiModal getCreateOrUpdateDoiModal() {
		if (createOrUpdateDoiModal == null) {
			createOrUpdateDoiModal = ginInjector.getCreateOrUpdateDoiModal();
			view.addWidget(createOrUpdateDoiModal.asWidget());
		}
		return createOrUpdateDoiModal;
	}

	private ChallengeClientAsync getChallengeClient() {
		if (challengeClient == null) {
			challengeClient = ginInjector.getChallengeClientAsync();
			fixServiceEntryPoint(challengeClient);
		}
		return challengeClient;
	}
	private SynapseClientAsync getSynapseClient() {
		if (synapseClient == null) {
			synapseClient = ginInjector.getSynapseClientAsync();
			fixServiceEntryPoint(synapseClient);
		}
		return synapseClient;
	}
	
	private SynapseJavascriptClient getSynapseJavascriptClient() {
		if (jsClient == null) {
			jsClient = ginInjector.getSynapseJavascriptClient();
		}
		return jsClient;
	}
	
	private GlobalApplicationState getGlobalApplicationState() {
		if (globalApplicationState == null) {
			globalApplicationState = ginInjector.getGlobalApplicationState();
		}
		return globalApplicationState;
	}
	
	private AccessControlListModalWidget getAccessControlListModalWidget() {
		if (accessControlListModalWidget == null) {
			accessControlListModalWidget = ginInjector.getAccessControlListModalWidget();
			this.view.addWidget(accessControlListModalWidget);
		}
		return accessControlListModalWidget;
	}
	private CreateTableViewWizard getCreateTableViewWizard() {
		if (createTableViewWizard == null) {
			createTableViewWizard = ginInjector.getCreateTableViewWizard();
			this.view.addWidget(createTableViewWizard.asWidget());
		}
		return createTableViewWizard;
	}
	private UploadTableModalWidget getUploadTableModalWidget() {
		if (uploadTableModalWidget == null) {
			uploadTableModalWidget = ginInjector.getUploadTableModalWidget();
			view.addWidget(uploadTableModalWidget);
		}
		return uploadTableModalWidget;
	}
	
	private AddExternalRepoModal getAddExternalRepoModal() {
		if (addExternalRepoModal == null) {
			addExternalRepoModal = ginInjector.getAddExternalRepoModal();
			view.addWidget(addExternalRepoModal.asWidget());
		}
		return addExternalRepoModal;
	}
	private RenameEntityModalWidget getRenameEntityModalWidget() {
		if (renameEntityModalWidget == null) {
			renameEntityModalWidget = ginInjector.getRenameEntityModalWidget();
		}
		return renameEntityModalWidget;
	}
	private EditFileMetadataModalWidget getEditFileMetadataModalWidget() {
		if (editFileMetadataModalWidget == null) {
			editFileMetadataModalWidget = ginInjector.getEditFileMetadataModalWidget();
		}
		return editFileMetadataModalWidget;
	}
	private EditProjectMetadataModalWidget getEditProjectMetadataModalWidget() {
		if (editProjectMetadataModalWidget == null) {
			editProjectMetadataModalWidget = ginInjector.getEditProjectMetadataModalWidget();
		}
		return editProjectMetadataModalWidget;
	}
	private EntityFinder getEntityFinder() {
		if (entityFinder == null) {
			entityFinder = ginInjector.getEntityFinder();
		}
		return entityFinder;
	}
	private EvaluationSubmitter getEvaluationSubmitter() {
		if (submitter == null) {
			submitter = ginInjector.getEvaluationSubmitter();
			view.addWidget(submitter.asWidget());
		}
		return submitter;
	}
	
	private UploadDialogWidget getNewUploadDialogWidget() {
		UploadDialogWidget uploadDialogWidget = ginInjector.getUploadDialogWidget();
		view.setUploadDialogWidget(uploadDialogWidget.asWidget());
		return uploadDialogWidget;
	}
	private WikiMarkdownEditor getWikiMarkdownEditor() {
		if (wikiEditor == null) {
			wikiEditor = ginInjector.getWikiMarkdownEditor();
			view.addWidget(wikiEditor.asWidget());
		}
		return wikiEditor;
	}
	private ProvenanceEditorWidget getProvenanceEditorWidget() {
		if (provenanceEditor == null) {
			provenanceEditor = ginInjector.getProvenanceEditorWidget();
			view.addWidget(provenanceEditor.asWidget());
		}
		return provenanceEditor;
	}
	private StorageLocationWidget getStorageLocationWidget() {
		if (storageLocationEditor == null) {
			storageLocationEditor = ginInjector.getStorageLocationWidget();
			view.addWidget(storageLocationEditor.asWidget());
		}
		return storageLocationEditor;
	}
	private EvaluationEditorModal getEvaluationEditorModal() {
		if (evalEditor == null) {
			evalEditor = ginInjector.getEvaluationEditorModal();
			view.addWidget(evalEditor.asWidget());
		}
		return evalEditor;
	}
	
	@Override
	public void configure(ActionMenuWidget actionMenu,
			EntityBundle entityBundle, boolean isCurrentVersion, String wikiPageId, EntityArea currentArea) {
		this.entityBundle = entityBundle;
		this.wikiPageId = wikiPageId;
		this.permissions = entityBundle.getPermissions();
		this.actionMenu = actionMenu;
		this.entity = entityBundle.getEntity();
		this.isUserAuthenticated = authenticationController.isLoggedIn();
		this.isCurrentVersion = isCurrentVersion;
		this.enityTypeDisplay = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(entityBundle.getEntity().getClass()));
		this.currentArea = currentArea;

		// hide all commands by default
		actionMenu.hideAllActions();
		gwt.scheduleExecution(reconfigureActionsCallback, 2000);
		if (!(entity instanceof Project)) {
			actionMenu.setToolsButtonIcon(enityTypeDisplay + TOOLS, IconType.GEAR);
		} else if (currentArea != null) {
			actionMenu.setToolsButtonIcon(DisplayUtils.capitalize(currentArea.name()) + TOOLS, IconType.GEAR);
		}
	}
	
	private void reconfigureActions() {
		// Setup the actions
		configureDeleteAction();
		configureShareAction();
		configureRenameAction();
		configureEditWiki();
		configureViewWikiSource();
		configureAddWikiSubpage();
		configureReorderWikiSubpages();
		configureDeleteWikiAction();
		configureMove();
		configureLink();
		configureSubmit();
		configureAnnotations();
		configureFileHistory();
		configureFileUpload();
		configureProvenance();
		configureChangeStorageLocation();
		configureCreateOrUpdateDoi();
		configureCreateDOI();
		configureEditProjectMetadataAction();
		configureEditFileMetadataAction();
		configureAddEvaluationAction();
		configureCreateChallenge();
		configureACTCommands();
		configureTableCommands();
		configureProjectLevelTableCommands();
		configureAddFolder();
		configureUploadNewFileEntity();
		configureAddExternalDockerRepo();
	}
	
	private void configureAddExternalDockerRepo() {
		if (entityBundle.getEntity() instanceof Project && EntityArea.DOCKER.equals(currentArea)) {
			actionMenu.setActionVisible(Action.CREATE_EXTERNAL_DOCKER_REPO, entityBundle.getPermissions().getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.CREATE_EXTERNAL_DOCKER_REPO, this);
		} else {
			actionMenu.setActionVisible(Action.CREATE_EXTERNAL_DOCKER_REPO, false);
		}
	}
	
	private void configureProjectLevelTableCommands() {
		if (entityBundle.getEntity() instanceof Project && EntityArea.TABLES.equals(currentArea)) {
			// show tables top level commands
			boolean canEditResults = entityBundle.getPermissions().getCanCertifiedUserEdit();
			actionMenu.setActionVisible(Action.UPLOAD_TABLE, canEditResults);
			actionMenu.setActionListener(Action.UPLOAD_TABLE, this);
			actionMenu.setActionVisible(Action.ADD_TABLE, canEditResults);
			actionMenu.setActionListener(Action.ADD_TABLE, this);
			actionMenu.setActionVisible(Action.ADD_FILE_VIEW, canEditResults);
			actionMenu.setActionListener(Action.ADD_FILE_VIEW, this);
			actionMenu.setActionVisible(Action.ADD_PROJECT_VIEW, canEditResults);
			actionMenu.setActionListener(Action.ADD_PROJECT_VIEW, this);
		} else {
			actionMenu.setActionVisible(Action.UPLOAD_TABLE, false);
			actionMenu.setActionVisible(Action.ADD_TABLE, false);
			actionMenu.setActionVisible(Action.ADD_FILE_VIEW, false);
			actionMenu.setActionVisible(Action.ADD_PROJECT_VIEW, false);
		}
	}
	
	private void configureACTCommands() {
		// TODO: remove APPROVE_USER_ACCESS command (after new ACT feature is released, where the system supports the workflow)
		actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, false);
		actionMenu.setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, false);
		actionMenu.setACTDividerVisible(false);
		//show ACT commands if this is the Project Settings tools menu, or if the entity is not a project (looking at a child entity)
		if (authenticationController.isLoggedIn() && 
				!isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
				@Override
				public void invoke(Boolean isACT) {
					if (isACT) {
						actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, true);
						actionMenu.setActionListener(Action.APPROVE_USER_ACCESS, EntityActionControllerImpl.this);
						actionMenu.setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
						actionMenu.setActionListener(Action.MANAGE_ACCESS_REQUIREMENTS, EntityActionControllerImpl.this);
						actionMenu.setACTDividerVisible(true);
					}
				}
			});
		}
	}
	
	public void onSelectChallengeTeam(String id) {
		Challenge c = new Challenge();
		c.setProjectId(entity.getId());
		c.setParticipantTeamId(id);
		getChallengeClient().createChallenge(c, new AsyncCallback<Challenge>() {
			@Override
			public void onSuccess(Challenge v) {
				view.showInfo(DisplayConstants.CHALLENGE_CREATED);
				// go to challenge tab
				Place gotoPlace = new Synapse(entity.getId(), null, EntityArea.CHALLENGE, null);
				getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	private void configureProvenance() {
		if(entityBundle.getEntity() instanceof FileEntity || entityBundle.getEntity() instanceof DockerRepository || entityBundle.getEntity() instanceof Table) {
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_PROVENANCE, this);
			actionMenu.setActionText(Action.EDIT_PROVENANCE, "Edit "+enityTypeDisplay + " Provenance");
		} else {
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, false);
		}
	}
	
	private void configureChangeStorageLocation() {
		if(entityBundle.getEntity() instanceof Folder || 
				(entityBundle.getEntity() instanceof Project && currentArea == null)){
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, permissions.getCanEdit());
			actionMenu.setActionText(Action.CHANGE_STORAGE_LOCATION, "Change "+enityTypeDisplay + " Storage Location");
			actionMenu.setActionListener(Action.CHANGE_STORAGE_LOCATION, this);
		} else {
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, false);
		}
	}
	
	private void configureCreateDOI() {
		if (!DisplayUtils.isInTestWebsite(cookies)) {
			boolean canEdit = permissions.getCanEdit();
			actionMenu.setActionVisible(Action.CREATE_DOI, false);
			if (canEdit &&
					!isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea) &&
					!(entityBundle.getEntity() instanceof EntityView)) {
				actionMenu.setActionListener(Action.CREATE_DOI, this);
				if (entityBundle.getDoi() == null) {
					// show command if not returned, thus not in existence
					actionMenu.setActionVisible(Action.CREATE_DOI, true);
					actionMenu.setActionText(Action.CREATE_DOI, CREATE_DOI_FOR + enityTypeDisplay);
				}
			}
		} else {
			actionMenu.setActionVisible(Action.CREATE_DOI, false);
		}
	}
	
	private void configureCreateChallenge() {
		currentChallengeId = null;
		actionMenu.setActionVisible(Action.CREATE_CHALLENGE, false);
		actionMenu.setActionVisible(Action.DELETE_CHALLENGE, false);
		boolean canEdit = permissions.getCanEdit();
		if(entityBundle.getEntity() instanceof Project && canEdit && 
				((DisplayUtils.isInTestWebsite(cookies) && currentArea == null)|| 
				EntityArea.CHALLENGE.equals(currentArea))) {
			actionMenu.setActionListener(Action.CREATE_CHALLENGE, this);
			actionMenu.setActionListener(Action.DELETE_CHALLENGE, this);
			
			//find out if this project has a challenge
			getChallengeClient().getChallengeForProject(entity.getId(), new AsyncCallback<Challenge>() {
				@Override
				public void onSuccess(Challenge result) {
					// challenge found
					currentChallengeId = result.getId();
					actionMenu.setActionVisible(Action.DELETE_CHALLENGE, EntityArea.CHALLENGE.equals(currentArea));
				}
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof NotFoundException) {
						actionMenu.setActionVisible(Action.CREATE_CHALLENGE, true);
					} else {
						//unexpected error
						view.showErrorMessage(caught.getMessage());
					}
				}
			});
		}
	}
	
	private Long getVersion(){
		Long version = null;
		Entity entity = entityBundle.getEntity();
		if (entity instanceof Versionable) {
			version = ((Versionable)entity).getVersionNumber();
		}
		return version;
	}
	
	private void onCreateDOI() {
		getSynapseClient().createDoi(entity.getId(), getVersion(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void v) {
				view.showInfo(DisplayConstants.DOI_REQUEST_SENT_TITLE + DisplayConstants.DOI_REQUEST_SENT_MESSAGE);
				fireEntityUpdatedEvent();
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage("DOI creation is under maintenance and is temporarily unavailable.");

			}
		});
	}

	private void configureCreateOrUpdateDoi() {
		if (DisplayUtils.isInTestWebsite(cookies)) {
			boolean canEdit = permissions.getCanEdit();
			actionMenu.setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
			if (canEdit &&
					!isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea) &&
					!(entityBundle.getEntity() instanceof EntityView)) {
				actionMenu.setActionListener(Action.CREATE_OR_UPDATE_DOI, this);
				actionMenu.setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
				if (entityBundle.getDoiAssociation() == null) {
					// show command if not returned, thus not in existence
					actionMenu.setActionText(Action.CREATE_OR_UPDATE_DOI, CREATE_DOI_FOR + enityTypeDisplay);
				} else {
					actionMenu.setActionText(Action.CREATE_OR_UPDATE_DOI, UPDATE_DOI_FOR + enityTypeDisplay);
				}
			}
		} else {
			actionMenu.setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
		}
	}

	private void onCreateOrUpdateDoi() {
		getCreateOrUpdateDoiModal().configureAndShow(entity, getVersion(), authenticationController.getCurrentUserProfile());
	}

	private void onCreateChallenge() {
		getSelectTeamModal().show();
	}
	
	private void onDeleteChallenge() {
		// Confirm the delete with the user.
		view.showConfirmDeleteDialog(DisplayConstants.CONFIRM_DELETE_CHALLENGE, () -> {
			postConfirmedDeleteChallenge();
		});
	}

	/**
	 * Called after the user has confirmed the delete of the challenge.
	 */
	public void postConfirmedDeleteChallenge() {
		// The user has confirmed the delete, the next step is the preflight check.
		preflightController.checkDeleteEntity(this.entityBundle, () -> {
			postCheckDeleteChallenge();
		});
	}
	
	public void postCheckDeleteChallenge() {
		getChallengeClient().deleteChallenge(currentChallengeId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(THE + "challenge" + WAS_SUCCESSFULLY_DELETED);
				fireEntityUpdatedEvent();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
		
	private void configureTableCommands() {
		if(entityBundle.getEntity() instanceof Table ) {
			boolean canEditResults = entityBundle.getPermissions().getCanCertifiedUserEdit();
			actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, canEditResults);
			actionMenu.setActionText(Action.UPLOAD_TABLE_DATA, "Upload Data to " + enityTypeDisplay);
			actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, canEditResults);
			actionMenu.setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
			actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, true);
			actionMenu.setActionVisible(Action.SHOW_VIEW_SCOPE, !(entityBundle.getEntity() instanceof TableEntity));
		} else {
			actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, false);
			actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, false);
			actionMenu.setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, false);
			actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
			actionMenu.setActionVisible(Action.SHOW_VIEW_SCOPE, false);
		}
	}
	
	private void configureFileUpload() {
		if(entityBundle.getEntity() instanceof FileEntity ){
			actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.UPLOAD_NEW_FILE, this);
		}else{
			actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, false);
		}
	}

	private void configureUploadNewFileEntity() {
		if(isContainerOnFilesTab(entityBundle.getEntity(), currentArea)){
			actionMenu.setActionVisible(Action.UPLOAD_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.UPLOAD_FILE, this);
		}else{
			actionMenu.setActionVisible(Action.UPLOAD_FILE, false);
		}
	}
	
	private void configureAddFolder() {
		if(isContainerOnFilesTab(entityBundle.getEntity(), currentArea)){
			actionMenu.setActionVisible(Action.CREATE_FOLDER, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.CREATE_FOLDER, this);
		}else{
			actionMenu.setActionVisible(Action.CREATE_FOLDER, false);
		}
	}
	
	private void configureEditWiki(){
		if(isWikiableConfig(entityBundle.getEntity(), currentArea)){
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_WIKI_PAGE, this);
			actionMenu.setActionText(Action.EDIT_WIKI_PAGE, EDIT_WIKI_PREFIX+enityTypeDisplay+WIKI);
		}else{
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, false);
		}
	}
	
	private void configureViewWikiSource(){
		//only visible if entity may have a wiki, and user can't Edit the wiki
		if(isWikiableConfig(entityBundle.getEntity(), currentArea)){
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, !permissions.getCanEdit());
			actionMenu.setActionListener(Action.VIEW_WIKI_SOURCE, this);
		}else{
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, false);
		}
	}

	private void configureReorderWikiSubpages() {
		if(isWikiableConfig(entityBundle.getEntity(), currentArea) && 
				entityBundle.getEntity() instanceof Project &&
				permissions.getCanEdit()){
			// shown if there's more than one page
			getSynapseClient().getV2WikiHeaderTree(entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), new AsyncCallback<List<V2WikiHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
				}
				public void onSuccess(List<V2WikiHeader> wikiHeaders) {
					boolean isMoreThanOne = wikiHeaders.size() > 1;
					actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, isMoreThanOne);
				};
			});
		} else{
			actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
		}
	}
	
	private void configureAddWikiSubpage(){
		if(entityBundle.getEntity() instanceof Project && isWikiableConfig(entityBundle.getEntity(), currentArea) && wikiPageId != null){
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.ADD_WIKI_SUBPAGE, this);
		}else{
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
		}
	}
	
	private void configureMove(){
		if(isMovableType(entityBundle.getEntity()) ){
			actionMenu.setActionVisible(Action.MOVE_ENTITY, permissions.getCanEdit());
			actionMenu.setActionText(Action.MOVE_ENTITY, MOVE_PREFIX+enityTypeDisplay);
			actionMenu.setActionListener(Action.MOVE_ENTITY, this);
		}else{
			actionMenu.setActionVisible(Action.MOVE_ENTITY, false);
		}
	}
	
	private void configureLink(){
		if(isLinkType(entityBundle.getEntity()) && 
				!isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)){
			actionMenu.setActionVisible(Action.CREATE_LINK, true);
			actionMenu.setActionListener(Action.CREATE_LINK, this);
			actionMenu.setActionText(Action.CREATE_LINK, "Save Link to "+enityTypeDisplay);
		}else{
			actionMenu.setActionVisible(Action.CREATE_LINK, false);
		}
	}
	
	private void configureAnnotations(){
		if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.SHOW_ANNOTATIONS, false);
		} else {
			actionMenu.setActionVisible(Action.SHOW_ANNOTATIONS, true);
		}
	}
	
	private void configureFileHistory(){
		if(entityBundle.getEntity() instanceof FileEntity){
			actionMenu.setActionVisible(Action.SHOW_FILE_HISTORY, true);
		}else{
			actionMenu.setActionVisible(Action.SHOW_FILE_HISTORY, false);
		}
	}
	
	private void configureSubmit(){
		if(isSubmittableType(entityBundle.getEntity())){
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, true);
			actionMenu.setActionListener(Action.SUBMIT_TO_CHALLENGE, this);
			actionMenu.setActionText(Action.SUBMIT_TO_CHALLENGE, "Submit "+enityTypeDisplay+" to Challenge");
		}else{
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, false);
		}
	}
	
	private void configureEditProjectMetadataAction(){
		if(entityBundle.getEntity() instanceof Project && currentArea == null){
			actionMenu.setActionVisible(Action.EDIT_PROJECT_METADATA, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_PROJECT_METADATA, this);
		}else{
			actionMenu.setActionVisible(Action.EDIT_PROJECT_METADATA, false);
		}
	}
	
	private void configureAddEvaluationAction(){
		if(entityBundle.getEntity() instanceof Project && currentArea == EntityArea.CHALLENGE){
			actionMenu.setActionVisible(Action.ADD_EVALUATION_QUEUE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.ADD_EVALUATION_QUEUE, this);
		}else{
			actionMenu.setActionVisible(Action.ADD_EVALUATION_QUEUE, false);
		}
	}
	
	private void configureEditFileMetadataAction(){
		if(entityBundle.getEntity() instanceof FileEntity){
			actionMenu.setActionVisible(Action.EDIT_FILE_METADATA, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_FILE_METADATA, this);
		} else{
			actionMenu.setActionVisible(Action.EDIT_FILE_METADATA, false);
		}
	}
	
	private void configureRenameAction(){
		if(isRenameOnly(entityBundle.getEntity()) && !(entityBundle.getEntity() instanceof DockerRepository)) {
			actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, permissions.getCanEdit());
			actionMenu.setActionText(Action.CHANGE_ENTITY_NAME, RENAME_PREFIX+enityTypeDisplay);
			actionMenu.setActionListener(Action.CHANGE_ENTITY_NAME, this);
		} else {
			actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, false);
		}
	}
	
	private void configureDeleteAction(){
		if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.DELETE_ENTITY, false);
		} else {
			actionMenu.setActionVisible(Action.DELETE_ENTITY, permissions.getCanDelete());
			actionMenu.setActionText(Action.DELETE_ENTITY, DELETE_PREFIX+enityTypeDisplay);
			actionMenu.setActionListener(Action.DELETE_ENTITY, this);
		}
	}
	private void configureDeleteWikiAction(){
		if(isWikiableConfig(entityBundle.getEntity(), currentArea) && 
				entityBundle.getEntity() instanceof Project){
			actionMenu.setActionVisible(Action.DELETE_WIKI_PAGE, permissions.getCanDelete());
			actionMenu.setActionListener(Action.DELETE_WIKI_PAGE, this);
		} else {
			actionMenu.setActionVisible(Action.DELETE_WIKI_PAGE, false);
		}
	}
	
	private void configureShareAction(){
		if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.SHARE, false);
		} else {
			actionMenu.setActionVisible(Action.SHARE, true);
			actionMenu.setActionListener(Action.SHARE, this);
			actionMenu.setActionText(Action.SHARE, enityTypeDisplay + " Sharing Settings");
			if(PublicPrivateBadge.isPublic(entityBundle.getBenefactorAcl(), ginInjector.getSynapseProperties().getPublicPrincipalIds())){
				actionMenu.setActionIcon(Action.SHARE, IconType.GLOBE);
			}else{
				actionMenu.setActionIcon(Action.SHARE, IconType.LOCK);
			}	
		}
	}
	
	public boolean isTopLevelProjectToolsMenu(Entity entity, EntityArea area) {
		return entity instanceof Project && area != null;
	}
	
	public boolean isWikiableConfig(Entity entity, EntityArea area) {
		if (entity instanceof Project) {
			return EntityArea.WIKI.equals(area);
		} 
		return isWikiableType(entity);
	}
	
	public boolean isContainerOnFilesTab(Entity entity, EntityArea area) {
		return (entity instanceof Folder || 
				(entity instanceof Project && EntityArea.FILES.equals(area)));
	}
	
	/**
	 * Can an entity of this type be moved?
	 * @param entity
	 * @return
	 */
	public boolean isMovableType(Entity entity){
		if(entity instanceof Project || entity instanceof DockerRepository){
			return false;
		}
		return true;
	}
	
	/**
	 * Can an entity of this type have a wiki?
	 * @param entity
	 * @return
	 */
	public boolean isWikiableType(Entity entity){
		if(entity instanceof Table || entity instanceof Link){
			return false;
		}
		return true;
	}
	
	/**
	 * Can a link to this type be created?
	 * @param entity
	 * @return
	 */
	public boolean isLinkType(Entity entity){
		if(entity instanceof Link){
			return false;
		}
		return true;
	}
	
	/**
	 * Can an entity of this type be submitted to a challenge?
	 * @param entity
	 * @return
	 */
	public boolean isSubmittableType(Entity entity){
		if(entity instanceof Table){
			return false;
		}
		return entity instanceof Versionable || entity instanceof DockerRepository;
	}


	/**
	 * Can this entity be renamed (File and Project will have additional editable fields)?
	 * @param entity
	 * @return
	 */
	public boolean isRenameOnly(Entity entity){
		if(entity instanceof FileEntity){
			return false;
		} else if(entity instanceof Project){
			return false;
		}
		return true;
	}
	
	@Override
	public void onAction(Action action) {
		switch(action){
		case DELETE_ENTITY:
			onDeleteEntity();
			break;
		case SHARE:
			onShare();
			break;
		case CHANGE_ENTITY_NAME:
			onRename();
			break;
		case EDIT_FILE_METADATA:
			onEditFileMetadata();
			break;
		case EDIT_PROJECT_METADATA:
			onEditProjectMetadata();
			break;
		case EDIT_WIKI_PAGE:
			onEditWiki();
			break;
		case VIEW_WIKI_SOURCE:
			onViewWikiSource();
			break;
		case DELETE_WIKI_PAGE:
			onDeleteWiki();
			break;
		case ADD_WIKI_SUBPAGE:
			onAddWikiSubpage();
			break;
		case MOVE_ENTITY:
			onMove();
			break;
		case CREATE_LINK:
			onLink();
			break;
		case SUBMIT_TO_CHALLENGE:
			onSubmit();
			break;
		case UPLOAD_NEW_FILE:
			onUploadFile();
			break;
		case EDIT_PROVENANCE:
			onEditProvenance();
			break;
		case CHANGE_STORAGE_LOCATION:
			onChangeStorageLocation();
			break;
		case CREATE_DOI:
			onCreateDOI();
			break;
		case CREATE_OR_UPDATE_DOI:
			onCreateOrUpdateDoi();
			break;
		case ADD_EVALUATION_QUEUE:
			onAddEvaluationQueue();
			break;
		case CREATE_CHALLENGE:
			onCreateChallenge();
			break;
		case DELETE_CHALLENGE:
			onDeleteChallenge();
			break;
		case APPROVE_USER_ACCESS:
			onApproveUserAccess();
			break;
		case MANAGE_ACCESS_REQUIREMENTS:
			onManageAccessRequirements();
			break;
		case UPLOAD_FILE :
			onUploadNewFileEntity();
			break;
		case CREATE_FOLDER :
			onCreateFolder();
			break;
		case UPLOAD_TABLE :
			onUploadTable();
			break;
		case ADD_TABLE :
			onAddTable();
			break;
		case ADD_FILE_VIEW :
			onAddFileView();
			break;
		case ADD_PROJECT_VIEW :
			onAddProjectView();
			break;
		case CREATE_EXTERNAL_DOCKER_REPO :
			onCreateExternalDockerRepo();
			break;
		default:
			break;
		}
	}
	public void onCreateExternalDockerRepo() {
		// This operation creates an entity and uploads data to the entity so both checks must pass.
		preflightController.checkCreateEntity(entityBundle, DockerRepository.class.getName(), new Callback() {
			@Override
			public void invoke() {
				postCheckCreateExternalDockerRepo();
			}
		});
	}
	private void postCheckCreateExternalDockerRepo(){
		getAddExternalRepoModal().configuration(entityBundle.getEntity().getId(), () -> {
			fireEntityUpdatedEvent();
		});
		getAddExternalRepoModal().show();
	}
	
	public void onUploadTable() {
		// This operation creates an entity and uploads data to the entity so both checks must pass.
		preflightController.checkCreateEntityAndUpload(entityBundle, TableEntity.class.getName(), () -> {
			postCheckUploadTable();
		});
	}
	private void postCheckUploadTable(){
		getUploadTableModalWidget().configure(entityBundle.getEntity().getId(), null);
		getUploadTableModalWidget().showModal(entityUpdatedWizardCallback);
	}
	
	public void onAddFileView() {
		preflightController.checkCreateEntity(entityBundle, EntityView.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.files);
		});
	}
	
	public void onAddProjectView() {
		preflightController.checkCreateEntity(entityBundle, EntityView.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.projects);
		});
	}
	
	public void onAddTable() {
		preflightController.checkCreateEntity(entityBundle, TableEntity.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.table);
		});
	}
	
	private void postCheckCreateTableOrView(TableType type) {
		getCreateTableViewWizard().configure(entityBundle.getEntity().getId(), type);
		getCreateTableViewWizard().showModal(entityUpdatedWizardCallback);
	}

	private void onUploadNewFileEntity() {
		preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				UploadDialogWidget uploader = getNewUploadDialogWidget();
				uploader.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, null,
						entityBundle.getEntity().getId(), null, true);
				uploader.setUploaderLinkNameVisible(true);
				uploader.show();		
			}
		});
	}
	
	private void onCreateFolder() {
		preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				AddFolderDialogWidget w = getAddFolderDialogWidget();
				w.show(entityBundle.getEntity().getId());
			}
		});

	}
	
	private void onApproveUserAccess() {
		getApproveUserAccessModal().configure(entityBundle);
		getApproveUserAccessModal().show();
	}
	
	private void onAddEvaluationQueue() {
		getEvaluationEditorModal().configure(entity.getId(), new Callback() {
			@Override
			public void invoke() {
				Place gotoPlace = new Synapse(entity.getId(), null, EntityArea.CHALLENGE, null);
				getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
			}
		});
		getEvaluationEditorModal().show();
	}

	private void onManageAccessRequirements() {
		AccessRequirementsPlace place = new AccessRequirementsPlace(AccessRequirementsPlace.ID_PARAM + "=" + entity.getId() + "&" + AccessRequirementsPlace.TYPE_PARAM + "=" + RestrictableObjectType.ENTITY.toString());
		getGlobalApplicationState().getPlaceChanger().goTo(place);
	}
	
	private void onChangeStorageLocation() {
		preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postChangeStorageLocation();
			}
		});
	}
	
	private void postChangeStorageLocation() {
		getStorageLocationWidget().configure(this.entityBundle);
		getStorageLocationWidget().show();
	}
	
	private void onEditProvenance() {
		getProvenanceEditorWidget().configure(this.entityBundle);
		getProvenanceEditorWidget().show();
	}
	
	private void onUploadFile() {
		// Validate the user can upload to this entity.
		preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckUploadFile();
			}
		});
	}
	
	private void postCheckUploadFile(){
		UploadDialogWidget uploadDialogWidget = getNewUploadDialogWidget();
		uploadDialogWidget.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, entityBundle.getEntity(), null, null, true);
		uploadDialogWidget.disableMultipleFileUploads();
		uploadDialogWidget.setUploaderLinkNameVisible(false);
		uploadDialogWidget.show();
	}

	private void onSubmit() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postOnSubmit();
			}
		});
	}
	
	private void postOnSubmit(){
		getEvaluationSubmitter().configure(this.entityBundle.getEntity(), null);
	}
	
	private void onLink() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckLink();
			}
		});
	}
	
	private void postCheckLink(){
		getEntityFinder().configure(CONTAINER, false, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				createLink(selected.getTargetId());
				getEntityFinder().hide();
			}
		});
		getEntityFinder().show();
	}
	
	/**
	 * Create a link with the given target as a parent.
	 * @param target
	 */
	public void createLink(String target){
		Link link = new Link();
		link.setParentId(target);
		Reference ref = new Reference();
		ref.setTargetId(entityBundle.getEntity().getId());
		Long targetVersionNumber = null;
		if (isShowingVersion && entityBundle.getEntity() instanceof Versionable) {
			targetVersionNumber = ((Versionable)entityBundle.getEntity()).getVersionNumber();
		}
		ref.setTargetVersionNumber(targetVersionNumber);
		link.setLinksTo(ref); // links to this entity
		link.setLinksToClassName(entityBundle.getEntity().getEntityType());
		link.setName(entityBundle.getEntity().getName()); // copy name of this entity as default
		link.setEntityType(Link.class.getName());
		getSynapseClient().createEntity(link, new AsyncCallback<Entity>() {
			
			@Override
			public void onSuccess(Entity result) {
				view.showInfo(DisplayConstants.TEXT_LINK_SAVED);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof BadRequestException) {
					view.showErrorMessage(DisplayConstants.ERROR_CANT_MOVE_HERE);
					return;
				}
				if(caught instanceof NotFoundException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
					return;
				}
				if (caught instanceof UnauthorizedException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_AUTHORIZED);
					return;
				}
				view.showErrorMessage(caught.getMessage());
				
			}
		});
	}
	
	private void onMove() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckMove();
			}
		});
	}

	private void postCheckMove() {
		EntityFilter filter = entityBundle.getEntity() instanceof Table ? PROJECT : CONTAINER;
		getEntityFinder().configure(filter, false, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				moveEntity(selected.getTargetId());
				getEntityFinder().hide();
			}
		});
		getEntityFinder().show();
	}
	
	/**
	 * Move the entity to the given target.
	 * @param target
	 */
	private void moveEntity(String target){
		String entityId = entityBundle.getEntity().getId();
		getSynapseClient().moveEntity(entityId, target, new AsyncCallback<Entity>() {
			
			@Override
			public void onSuccess(Entity result) {
				fireEntityUpdatedEvent();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	private void onViewWikiSource() {
		WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		getSynapseJavascriptClient().getV2WikiPageAsV1(key, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage page) {
				view.showInfoDialog("Wiki Source", page.getMarkdown());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});	
	}
	
	private void onEditWiki() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckEditWiki();
			}
		});
	}
	
	private void postCheckEditWiki(){
		//markdown editor will create a wiki if it does not already exist
		WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		getWikiMarkdownEditor().configure(key, wikiPage -> {
			fireEntityUpdatedEvent();
		});
	}

	private void onAddWikiSubpage() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckAddWikiSubpage();
			}
		});
	}
	
	private void postCheckAddWikiSubpage(){
		if (entityBundle.getRootWikiId() == null) {
			createWikiPage("Root");
		} else {
			view.showPromptDialog(DisplayConstants.ENTER_PAGE_TITLE, new PromptCallback() {
				@Override
				public void callback(String result) {
					createWikiPage(result);
				}
			});
		}
	}
	
	public void createWikiPage(final String name) {
		if (DisplayUtils.isDefined(name)) {
			WikiPage page = new WikiPage();
			page.setParentWikiId(wikiPageId);
			page.setTitle(name);
			getSynapseClient().createV2WikiPageWithV1(entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), page, new AsyncCallback<WikiPage>() {
	            @Override
	            public void onSuccess(WikiPage result) {
	                view.showInfo("'" + name + "' Page Added");
	                Synapse newPlace = new Synapse(entityBundle.getEntity().getId(), getVersion(), EntityArea.WIKI, result.getId());
	                getGlobalApplicationState().getPlaceChanger().goTo(newPlace);
	            }
	            @Override
	            public void onFailure(Throwable caught) {
	            	view.showErrorMessage(DisplayConstants.ERROR_PAGE_CREATION_FAILED + ": " + caught.getMessage());
	            }
	        });
		}
	}
	
	private void onRename() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckRename();
			}
		});
	}
	
	/**
	 * Called if the preflight check for a rename passes.
	 */
	private void postCheckRename(){
		getRenameEntityModalWidget().onRename(this.entity, () -> {
			fireEntityUpdatedEvent();
		});
	}

	private void onEditFileMetadata() {
		// Can only edit file metadata of the current file version
		if (isCurrentVersion) {
			// Validate the user can update this entity.
			preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
				@Override
				public void invoke() {
					postCheckEditFileMetadata();
				}
			});
		} else {
			view.showErrorMessage("Can only edit the metadata of the most recent file version.");
		}
		
	}
	
	/**
	 * Called if the preflight check for edit file metadata passes.
	 */
	private void postCheckEditFileMetadata() {
		FileHandle originalFileHandle = DisplayUtils.getFileHandle(entityBundle);
		getEditFileMetadataModalWidget().configure((FileEntity)entityBundle.getEntity(), originalFileHandle, () -> {
			fireEntityUpdatedEvent();
		});
	}
	
	private void onEditProjectMetadata() {
		// Validate the user can update this entity.
		preflightController.checkUpdateEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckEditProjectMetadata();
			}
		});
	}
	
	/**
	 * Called if the preflight check for a edit project metadata passes.
	 */
	private void postCheckEditProjectMetadata(){
		Boolean canChangeSettings = permissions.getCanChangeSettings();
		if (canChangeSettings == null) {
			canChangeSettings = false;
		}
		getEditProjectMetadataModalWidget().configure((Project)entityBundle.getEntity(), canChangeSettings, () -> {
			fireEntityUpdatedEvent();
		});
	}
	
	public void onDeleteWiki() {
		final WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		getWikiPageDeleteConfirmationDialog().show(key, parentWikiId -> {
			getGlobalApplicationState().getPlaceChanger().goTo(new Synapse(entityBundle.getEntity().getId(), null, EntityArea.WIKI, parentWikiId));	
		});
	}
	
	@Override
	public void onDeleteEntity() {
		// Confirm the delete with the user. Mention that everything inside folder will also be deleted if this is a folder entity.
		String display = ARE_YOU_SURE_YOU_WANT_TO_DELETE+this.enityTypeDisplay+" \""+this.entity.getName()+"\"?";
		if (this.entity instanceof Folder) {
			display += DELETE_FOLDER_EXPLANATION;
		}
		
		view.showConfirmDeleteDialog(display, new Callback() {
			@Override
			public void invoke() {
				postConfirmedDeleteEntity();
			}
		});
	}

	/**
	 * Called after the user has confirmed the delete of the entity.
	 */
	public void postConfirmedDeleteEntity() {
		// The user has confirmed the delete, the next step is the preflight check.
		preflightController.checkDeleteEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				postCheckDeleteEntity();
			}
		});
		
	}

	/**
	 * After all checks have been made we can do the actual entity delete.
	 */
	public void postCheckDeleteEntity() {
		final String entityId = this.entityBundle.getEntity().getId();
		getSynapseJavascriptClient().deleteEntityById(entityId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(THE + enityTypeDisplay + WAS_SUCCESSFULLY_DELETED); 
				// Go to entity's parent
				Place gotoPlace = createDeletePlace();
				getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);			
			}
		});
	}

	/**
	 * Create the delete place
	 * @return
	 */
	public Place createDeletePlace(){
		String parentId = entityBundle.getEntity().getParentId();
		Place gotoPlace = null;
		if(parentId != null && !(entityBundle.getEntity() instanceof Project)) {					
			if(entityBundle.getEntity() instanceof Table) gotoPlace = new Synapse(parentId, null, EntityArea.TABLES, null);
			else if(entityBundle.getEntity() instanceof DockerRepository) gotoPlace = new Synapse(parentId, null, EntityArea.DOCKER, null);
			else if(entityBundle.getEntity() instanceof FileEntity || entityBundle.getEntity() instanceof Folder) gotoPlace = new Synapse(parentId, null, EntityArea.FILES, null);
			else gotoPlace = new Synapse(parentId);
		} else {
			gotoPlace = new Profile(authenticationController.getCurrentUserPrincipalId());
		}
		return gotoPlace;
	}
	
	@Override
	public void onShare() {
		getAccessControlListModalWidget().configure(entity, permissions.getCanChangePermissions());
		this.getAccessControlListModalWidget().showSharing(() -> {
			fireEntityUpdatedEvent();
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setIsShowingVersion(boolean isShowingVersion) {
		this.isShowingVersion = isShowingVersion;
	}
}
