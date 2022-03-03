package org.sagebionetworks.web.client.widget.entity.controller;

import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.EntityTypeUtils.getFriendlyEntityTypeName;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFuture;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.CONTAINER;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.PROJECT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.SnapshotRequest;
import org.sagebionetworks.repo.model.table.SnapshotResponse;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionResponse;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.jsinterop.ToastMessageOptions;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModal;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageDeleteConfirmationDialog;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionListener;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.MaterializedViewEditor;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerImpl implements EntityActionController, ActionListener {

	public static final String AVAILABLE_IN_VERSION_HISTORY = "This will be available within your version history.";

	public static final String SNAPSHOT = "Snapshot";
	public static final String STABLE_VERSION = "Stable Version";

	public static final String CREATE_SNAPSHOT = "Create " + SNAPSHOT;
	public static final String CREATE_STABLE_VERSION = "Create " + STABLE_VERSION;

	public static final String SNAPSHOT_CREATED = SNAPSHOT + " Created";
	public static final String STABLE_VERSION_CREATED = STABLE_VERSION + " Created";

	public static final String STABLE_VERSION_CREATED_DETAILS = "You created a " + STABLE_VERSION + " of this Dataset";
	public static final String SNAPSHOT_CREATED_DETAILS_VIEW = "You created a " + SNAPSHOT + " of this View.";
	public static final String SNAPSHOT_CREATED_DETAILS_TABLE = "You created a " + SNAPSHOT + " of this Table.";

	public static final String CREATE_NEW_VIEW_VERSION_PROMPT_BODY = "You're about to create a " + SNAPSHOT + " of this View. " + AVAILABLE_IN_VERSION_HISTORY;
	public static final String CREATE_NEW_DATASET_VERSION_PROMPT_BODY = "You're about to create a " + STABLE_VERSION + " of this Dataset. " + AVAILABLE_IN_VERSION_HISTORY;
	public static final String CREATE_NEW_TABLE_ENTITY_VERSION_PROMPT_BODY = "You're about to create a "  + SNAPSHOT + " of this Table. " + AVAILABLE_IN_VERSION_HISTORY;

	public static final String CREATING_A_NEW_VIEW_VERSION_MESSAGE = "Creating a new View " + SNAPSHOT + "...";
	public static final String CREATING_A_NEW_DATASET_VERSION_MESSAGE = "Creating a new Dataset " + STABLE_VERSION + "...";

	public static final String VERSIONING_HELP_MARKDOWN = "This will create an immutable version, which will be available in your version history.";
	public static final String VERSIONING_HELP_HREF = "https://help.synapse.org/docs/Versioning.2003730726.html";

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
	public static final String EDIT_NAME_AND_DESCRIPTION = "Edit Name and Description";

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
	EntityFinderWidget.Builder entityFinderBuilder;
	EvaluationSubmitter submitter;
	EditFileMetadataModalWidget editFileMetadataModalWidget;
	EditProjectMetadataModalWidget editProjectMetadataModalWidget;
	EventBus eventBus;
	JobTrackingWidget jobTrackingWidget;
	EntityBundle entityBundle;
	String wikiPageId;
	Entity entity;
	UserEntityPermissions permissions;
	String entityTypeDisplay;
	boolean isUserAuthenticated;
	boolean isCurrentVersion;
	ActionMenuWidget actionMenu;
	WikiMarkdownEditor wikiEditor;
	ProvenanceEditorWidget provenanceEditor;
	StorageLocationWidget storageLocationEditor;
	CookieProvider cookies;
	ChallengeClientAsync challengeClient;
	SelectTeamModal selectTeamModal;
	CreateOrUpdateDoiModal createOrUpdateDoiModal;
	ApproveUserAccessModal approveUserAccessModal;
	PortalGinInjector ginInjector;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	AddFolderDialogWidget addFolderDialogWidget;
	CreateTableViewWizard createTableViewWizard;
	MaterializedViewEditor materializedViewEditor;
	boolean isShowingVersion = false;
	WizardCallback entityUpdatedWizardCallback;
	UploadTableModalWidget uploadTableModalWidget;
	AddExternalRepoModal addExternalRepoModal;
	String currentChallengeId;
	GWTWrapper gwt;
	WikiPageDeleteConfirmationDialog wikiPageDeleteConfirmationDialog;
	StatisticsPlotWidget statisticsPlotWidget;
	ChallengeTab challengeTab;
	PopupUtilsView popupUtils;

	@Inject
	public EntityActionControllerImpl(EntityActionControllerView view, PreflightController preflightController, PortalGinInjector ginInjector, AuthenticationController authenticationController, CookieProvider cookies, IsACTMemberAsyncHandler isACTMemberAsyncHandler, GWTWrapper gwt, EventBus eventBus, PopupUtilsView popupUtilsView) {
		super();
		this.view = view;
		this.ginInjector = ginInjector;
		this.preflightController = preflightController;
		this.authenticationController = authenticationController;
		this.cookies = cookies;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.gwt = gwt;
		this.eventBus = eventBus;
		this.popupUtils = popupUtilsView;
		entityUpdatedWizardCallback = new WizardCallback() {
			@Override
			public void onFinished() {
				fireEntityUpdatedEvent();
			}

			@Override
			public void onCanceled() {}
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

	private JobTrackingWidget getJobTrackingWidget() {
		if (jobTrackingWidget == null) {
			jobTrackingWidget = ginInjector.creatNewAsynchronousProgressWidget();
			view.setCreateVersionDialogJobTrackingWidget(jobTrackingWidget);
		}
		return jobTrackingWidget;
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

	private StatisticsPlotWidget getStatisticsPlotWidget() {
		if (statisticsPlotWidget == null) {
			statisticsPlotWidget = ginInjector.getStatisticsPlotWidget();
			view.addWidget(statisticsPlotWidget.asWidget());
		}
		return statisticsPlotWidget;
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

	
	private MaterializedViewEditor getMaterializedViewEditor() {
		if (materializedViewEditor == null) {
			materializedViewEditor = ginInjector.getMaterializedViewEditor();
			this.view.addWidget(materializedViewEditor.asWidget());
		}
		return materializedViewEditor;
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

	private EntityFinderWidget.Builder getEntityFinderBuilder() {
		if (entityFinderBuilder == null) {
			entityFinderBuilder = ginInjector.getEntityFinderBuilder();
		}
		return entityFinderBuilder;
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

	private PromptForValuesModalView.Configuration.Builder getPromptForValuesModalConfigBuilder() {
			return ginInjector.getPromptForValuesModalConfigurationBuilder();
	}

	@Override
	public void configure(ActionMenuWidget actionMenu, EntityBundle entityBundle, boolean isCurrentVersion, String wikiPageId, EntityArea currentArea) {
		this.entityBundle = entityBundle;
		this.wikiPageId = wikiPageId;
		this.permissions = entityBundle.getPermissions();
		this.actionMenu = actionMenu;
		this.entity = entityBundle.getEntity();
		this.isUserAuthenticated = authenticationController.isLoggedIn();
		this.isCurrentVersion = isCurrentVersion;
		this.entityTypeDisplay = getFriendlyEntityTypeName(entityBundle.getEntity());
		this.currentArea = currentArea;

		reconfigureActions();

		if (!(entity instanceof Project)) {
			actionMenu.setToolsButtonIcon(entityTypeDisplay + TOOLS, IconType.GEAR);
		} else if (currentArea != null) {
			actionMenu.setToolsButtonIcon(DisplayUtils.capitalize(currentArea.name()) + TOOLS, IconType.GEAR);
		}
	}

	private void reconfigureActions() {
		// make the button a skeleton while we determine what we can show
		actionMenu.setIsLoading(true);

		// hide all commands by default
		actionMenu.hideAllActions();

		// Setup the actions
		configureDeleteAction();
		configureShareAction();
		configureRenameAction();
		configureEditWiki();
		configureViewWikiSource();
		configureAddWikiSubpage();
		configureCreateTableViewSnapshot();
		configureDeleteWikiAction();
		configureMove();
		configureLink();
		configureSubmit();
		configureAnnotations();
		configureVersionHistory();
		configureFileUpload();
		configureProvenance();
		configureChangeStorageLocation();
		configureCreateOrUpdateDoi();
		configureEditProjectMetadataAction();
		configureEditFileMetadataAction();
		configureTableCommands();
		configureProjectLevelTableCommands();
		configureProjectLevelDatasetCommands();
		configureAddFolder();
		configureUploadNewFileEntity();
		configureAddExternalDockerRepo();
		configureStatisticsPlotAction();
		configureFullTextSearch();

		// These configuration methods are asynchronous
		FluentFuture challengeFuture = configureCreateChallenge();
		FluentFuture actFuture = configureACTCommands();
		FluentFuture reorderWikiSubpagesFuture = configureReorderWikiSubpages();

		// Show the button
		FluentFuture.from(whenAllComplete(challengeFuture, actFuture, reorderWikiSubpagesFuture).call(() -> {
			actionMenu.setIsLoading(false);
			return null;
		}, directExecutor()));
	}

	private void configureStatisticsPlotAction() {
		if (entityBundle.getEntity() instanceof Project && currentArea == null) {
			// is a project, if the current user can view then show the command
			boolean canView = entityBundle.getPermissions().getCanView();
			actionMenu.setActionVisible(Action.SHOW_PROJECT_STATS, canView);
			actionMenu.setActionListener(Action.SHOW_PROJECT_STATS, this);
		} else {
			actionMenu.setActionVisible(Action.SHOW_PROJECT_STATS, false);
		}
	}

	private void configureFullTextSearch() {
		if (entityBundle.getEntity() instanceof TableEntity && DisplayUtils.isInTestWebsite(cookies)) {
			TableEntity tableEntity = (TableEntity) entityBundle.getEntity();
			actionMenu.setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, true);
			actionMenu.setActionListener(Action.TOGGLE_FULL_TEXT_SEARCH, this);
			boolean isFTSEnabled = tableEntity.getIsSearchEnabled() == null ? false : tableEntity.getIsSearchEnabled();
			String actionTextPrefix = isFTSEnabled ? "Disable" : "Enable";
			actionMenu.setActionText(Action.TOGGLE_FULL_TEXT_SEARCH, actionTextPrefix + " Full Text Search");
			IconType icon = isFTSEnabled ? IconType.SEARCH_MINUS : IconType.SEARCH_PLUS;
			actionMenu.setActionIcon(Action.TOGGLE_FULL_TEXT_SEARCH, icon);
		} else {
			actionMenu.setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, false);
		}
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
			actionMenu.setActionVisible(Action.ADD_SUBMISSION_VIEW, canEditResults);
			actionMenu.setActionListener(Action.ADD_SUBMISSION_VIEW, this);
			actionMenu.setActionVisible(Action.ADD_MATERIALIZED_VIEW, canEditResults && DisplayUtils.isInTestWebsite(cookies));
			actionMenu.setActionListener(Action.ADD_MATERIALIZED_VIEW, this);

		} else {
			actionMenu.setActionVisible(Action.UPLOAD_TABLE, false);
			actionMenu.setActionVisible(Action.ADD_TABLE, false);
			actionMenu.setActionVisible(Action.ADD_FILE_VIEW, false);
			actionMenu.setActionVisible(Action.ADD_PROJECT_VIEW, false);
			actionMenu.setActionVisible(Action.ADD_SUBMISSION_VIEW, false);
			actionMenu.setActionVisible(Action.ADD_MATERIALIZED_VIEW, false);
		}
	}

	private void configureProjectLevelDatasetCommands() {
		if (entityBundle.getEntity() instanceof Project && EntityArea.DATASETS.equals(currentArea)) {
			// show tables top level commands
			boolean canEditResults = entityBundle.getPermissions().getCanCertifiedUserEdit();
			actionMenu.setActionVisible(Action.ADD_DATASET, canEditResults);
			actionMenu.setActionListener(Action.ADD_DATASET, this);
		} else {
			actionMenu.setActionVisible(Action.ADD_DATASET, false);
		}
	}


	private FluentFuture configureACTCommands() {
		// TODO: remove APPROVE_USER_ACCESS command (after new ACT feature is released, where the system
		// supports the workflow)
		actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, false);
		actionMenu.setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, false);
		actionMenu.setACTDividerVisible(false);
		// show ACT commands if this is the Project Settings tools menu, or if the entity is not a project
		// (looking at a child entity)
		if (authenticationController.isLoggedIn() && !isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			FluentFuture future = isACTMemberAsyncHandler.isACTActionAvailable();
			future.addCallback(new FutureCallback<Boolean>() {
				@Override
				public void onSuccess(@NullableDecl Boolean isACT) {
					if (isACT) {
						actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, true);
						actionMenu.setActionListener(Action.APPROVE_USER_ACCESS, EntityActionControllerImpl.this);
						actionMenu.setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
						actionMenu.setActionListener(Action.MANAGE_ACCESS_REQUIREMENTS, EntityActionControllerImpl.this);
						actionMenu.setACTDividerVisible(true);
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			}, directExecutor());
			return future;
		}
		return getDoneFuture(null);
	}

	public void onSelectChallengeTeam(String id) {
		Challenge c = new Challenge();
		c.setProjectId(entity.getId());
		c.setParticipantTeamId(id);
		getChallengeClient().createChallenge(c, new AsyncCallback<Challenge>() {
			@Override
			public void onSuccess(Challenge v) {
				view.showSuccess(DisplayConstants.CHALLENGE_CREATED);
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
		if (entityBundle.getEntity() instanceof FileEntity || entityBundle.getEntity() instanceof DockerRepository || entityBundle.getEntity() instanceof Table) {
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_PROVENANCE, this);
			actionMenu.setActionText(Action.EDIT_PROVENANCE, "Edit " + entityTypeDisplay + " Provenance");
		} else {
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, false);
		}
	}

	private void configureChangeStorageLocation() {
		if (entityBundle.getEntity() instanceof Folder || (entityBundle.getEntity() instanceof Project && currentArea == null)) {
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, permissions.getCanEdit());
			actionMenu.setActionText(Action.CHANGE_STORAGE_LOCATION, "Change " + entityTypeDisplay + " Storage Location");
			actionMenu.setActionListener(Action.CHANGE_STORAGE_LOCATION, this);
		} else {
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, false);
		}
	}

	private FluentFuture configureCreateChallenge() {
		currentChallengeId = null;
		actionMenu.setActionVisible(Action.CREATE_CHALLENGE, false);
		actionMenu.setActionVisible(Action.DELETE_CHALLENGE, false);
		boolean canEdit = permissions.getCanEdit();
		if (entityBundle.getEntity() instanceof Project && canEdit && ((DisplayUtils.isInTestWebsite(cookies) && currentArea == null) || EntityArea.CHALLENGE.equals(currentArea))) {
			actionMenu.setActionListener(Action.CREATE_CHALLENGE, this);
			actionMenu.setActionListener(Action.DELETE_CHALLENGE, this);

			// find out if this project has a challenge
			FluentFuture<Challenge> future = getSynapseJavascriptClient().getChallengeForProject(entity.getId());
			future.addCallback(new FutureCallback<Challenge>() {
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
						// unexpected error
						view.showErrorMessage(caught.getMessage());
					}
				}
			}, directExecutor());
			return future;
		} else {
			// No need to make a request
			return getDoneFuture(null);
		}
	}

	/**
	 * Retrieves the version of the entity, if it's not the latest version
	 * <p>
	 * For versionable entities, the result depends on if the entity is the latest version.
	 * If the entity is not the latest version, the optional will contain the version number.
	 * If the entity is the latest version, the optional will be empty.
	 * <p>
	 * If the entity is not versionable, it returns an empty optional.
	 */
	private Optional<Long> getVersionIfNotLatest() {
		Long version = null;
		Entity entity = entityBundle.getEntity();
		if (entity instanceof VersionableEntity) {
			VersionableEntity versionableEntity = ((VersionableEntity) entity);
			if (versionableEntity.getIsLatestVersion()) {
				return Optional.empty();
			}
			version = versionableEntity.getVersionNumber();
		}
		return Optional.ofNullable(version);
	}

	private void configureCreateOrUpdateDoi() {
		boolean canEdit = permissions.getCanEdit();
		actionMenu.setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
		if (canEdit && !isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionListener(Action.CREATE_OR_UPDATE_DOI, this);
			actionMenu.setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
			if (entityBundle.getDoiAssociation() == null) {
				// show command if not returned, thus not in existence
				actionMenu.setActionText(Action.CREATE_OR_UPDATE_DOI, CREATE_DOI_FOR + entityTypeDisplay);
			} else {
				actionMenu.setActionText(Action.CREATE_OR_UPDATE_DOI, UPDATE_DOI_FOR + entityTypeDisplay);
			}
		}
	}

	private void onCreateOrUpdateDoi() {
		getCreateOrUpdateDoiModal().configureAndShow(entity, getVersionIfNotLatest(), authenticationController.getCurrentUserProfile());
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
		if (entityBundle.getEntity() instanceof Table) {
			boolean isDataset = entityBundle.getEntity() instanceof Dataset;
			boolean isMaterializedView = entityBundle.getEntity() instanceof MaterializedView;
			// For UX reasons, datasets are not editable, even if the user has permissions (SWC-5870, SWC-5903)
			boolean canEditResults = entityBundle.getPermissions().getCanCertifiedUserEdit() && !isDataset && !isMaterializedView;
			actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, canEditResults);
			actionMenu.setActionText(Action.UPLOAD_TABLE_DATA, "Upload Data to " + entityTypeDisplay);
			actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, canEditResults);
			actionMenu.setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
			actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, true);
			actionMenu.setActionVisible(Action.SHOW_VIEW_SCOPE, !(entityBundle.getEntity() instanceof TableEntity) && !isDataset && !isMaterializedView);
			actionMenu.setActionVisible(Action.EDIT_DATASET_ITEMS, isDataset && isCurrentVersion);
			actionMenu.setActionVisible(Action.EDIT_DEFINING_SQL, isMaterializedView && isCurrentVersion);
			actionMenu.setActionListener(Action.EDIT_DEFINING_SQL, this);
		} else {
			actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, false);
			actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, false);
			actionMenu.setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, false);
			actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
			actionMenu.setActionVisible(Action.SHOW_VIEW_SCOPE, false);
			actionMenu.setActionVisible(Action.EDIT_DATASET_ITEMS, false);
			actionMenu.setActionVisible(Action.EDIT_DEFINING_SQL, false);
		}
	}

	private void configureFileUpload() {
		if (entityBundle.getEntity() instanceof FileEntity) {
			actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.UPLOAD_NEW_FILE, this);
		} else {
			actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, false);
		}
	}

	private void configureUploadNewFileEntity() {
		if (isContainerOnFilesTab(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.UPLOAD_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.UPLOAD_FILE, this);
		} else {
			actionMenu.setActionVisible(Action.UPLOAD_FILE, false);
		}
	}

	private void configureAddFolder() {
		if (isContainerOnFilesTab(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.CREATE_FOLDER, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.CREATE_FOLDER, this);
		} else {
			actionMenu.setActionVisible(Action.CREATE_FOLDER, false);
		}
	}

	private void configureEditWiki() {
		if (isWikiableConfig(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_WIKI_PAGE, this);
			actionMenu.setActionText(Action.EDIT_WIKI_PAGE, EDIT_WIKI_PREFIX + entityTypeDisplay + WIKI);
		} else {
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, false);
		}
	}

	private void configureViewWikiSource() {
		// only visible if entity may have a wiki
		if (isWikiableConfig(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, true);
			actionMenu.setActionListener(Action.VIEW_WIKI_SOURCE, this);
		} else {
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, false);
		}
	}

	private FluentFuture configureReorderWikiSubpages() {
		if (isWikiableConfig(entityBundle.getEntity(), currentArea) && entityBundle.getEntity() instanceof Project && permissions.getCanEdit()) {
			// shown if there's more than one page
			FluentFuture<List<V2WikiHeader>> future = getFuture(cb -> getSynapseJavascriptClient().getV2WikiHeaderTree(entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), cb));
			future.addCallback(new FutureCallback<List<V2WikiHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
				}

				@Override
				public void onSuccess(List<V2WikiHeader> wikiHeaders) {
					boolean isMoreThanOne = wikiHeaders.size() > 1;
					actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, isMoreThanOne);
				};
			}, directExecutor());
			return future;
		} else {
			actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
		}
		return getDoneFuture(null);
	}

	private void configureCreateTableViewSnapshot() {
		if (entityBundle.getEntity() instanceof Table && EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity(), cookies)) {
			if (entityBundle.getEntity() instanceof Dataset) {
				// "Stable Version" for datasets (SWC-5919)
				actionMenu.setActionText(Action.CREATE_TABLE_VERSION, "Create a Stable " + entityTypeDisplay + " Version");
			} else {
				actionMenu.setActionText(Action.CREATE_TABLE_VERSION, "Create a New " + entityTypeDisplay + " Version");
			}
			actionMenu.setActionVisible(Action.CREATE_TABLE_VERSION, permissions.getCanEdit());
			actionMenu.setActionListener(Action.CREATE_TABLE_VERSION, this);

		} else {
			actionMenu.setActionVisible(Action.CREATE_TABLE_VERSION, false);
		}
	}

	private void configureAddWikiSubpage() {
		if (entityBundle.getEntity() instanceof Project && isWikiableConfig(entityBundle.getEntity(), currentArea) && wikiPageId != null) {
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.ADD_WIKI_SUBPAGE, this);
		} else {
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
		}
	}

	private void configureMove() {
		if (isMovableType(entityBundle.getEntity())) {
			actionMenu.setActionVisible(Action.MOVE_ENTITY, permissions.getCanEdit());
			actionMenu.setActionText(Action.MOVE_ENTITY, MOVE_PREFIX + entityTypeDisplay);
			actionMenu.setActionListener(Action.MOVE_ENTITY, this);
		} else {
			actionMenu.setActionVisible(Action.MOVE_ENTITY, false);
		}
	}

	private void configureLink() {
		if (isLinkType(entityBundle.getEntity()) && !isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.CREATE_LINK, true);
			actionMenu.setActionListener(Action.CREATE_LINK, this);
			actionMenu.setActionText(Action.CREATE_LINK, "Save Link to " + entityTypeDisplay);
		} else {
			actionMenu.setActionVisible(Action.CREATE_LINK, false);
		}
	}

	private void configureAnnotations() {
		if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.SHOW_ANNOTATIONS, false);
		} else {
			actionMenu.setActionVisible(Action.SHOW_ANNOTATIONS, true);
		}
	}

	private void configureVersionHistory() {
		boolean isVersionHistoryAvailable = EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity(), cookies);
		actionMenu.setActionVisible(Action.SHOW_VERSION_HISTORY, isVersionHistoryAvailable);
	}

	private void configureSubmit() {
		if (isSubmittableType(entityBundle.getEntity())) {
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, true);
			actionMenu.setActionListener(Action.SUBMIT_TO_CHALLENGE, this);
			actionMenu.setActionText(Action.SUBMIT_TO_CHALLENGE, "Submit " + entityTypeDisplay + " to Challenge");
		} else {
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, false);
		}
	}

	private void configureEditProjectMetadataAction() {
		if (entityBundle.getEntity() instanceof Project && currentArea == null) {
			actionMenu.setActionVisible(Action.EDIT_PROJECT_METADATA, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_PROJECT_METADATA, this);
		} else {
			actionMenu.setActionVisible(Action.EDIT_PROJECT_METADATA, false);
		}
	}

	private void configureEditFileMetadataAction() {
		if (entityBundle.getEntity() instanceof FileEntity) {
			actionMenu.setActionVisible(Action.EDIT_FILE_METADATA, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_FILE_METADATA, this);
		} else {
			actionMenu.setActionVisible(Action.EDIT_FILE_METADATA, false);
		}
	}

	private void configureRenameAction() {
		if (!hasCustomRenameEditor(entityBundle.getEntity()) && !(entityBundle.getEntity() instanceof DockerRepository)) {
			actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, permissions.getCanEdit());
			String text = RENAME_PREFIX + entityTypeDisplay;
			if (entityBundle.getEntity() instanceof Table && DisplayUtils.isInTestWebsite(cookies)) {
				text = EDIT_NAME_AND_DESCRIPTION;
			}
			actionMenu.setActionText(Action.CHANGE_ENTITY_NAME, text);
			actionMenu.setActionListener(Action.CHANGE_ENTITY_NAME, this);
		} else {
			actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, false);
		}
	}

	private void configureDeleteAction() {
		if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.DELETE_ENTITY, false);
		} else {
			actionMenu.setActionVisible(Action.DELETE_ENTITY, permissions.getCanDelete());
			actionMenu.setActionText(Action.DELETE_ENTITY, DELETE_PREFIX + entityTypeDisplay);
			actionMenu.setActionListener(Action.DELETE_ENTITY, this);
		}
	}

	private void configureDeleteWikiAction() {
		if (isWikiableConfig(entityBundle.getEntity(), currentArea) && entityBundle.getEntity() instanceof Project) {
			actionMenu.setActionVisible(Action.DELETE_WIKI_PAGE, permissions.getCanDelete());
			actionMenu.setActionListener(Action.DELETE_WIKI_PAGE, this);
		} else {
			actionMenu.setActionVisible(Action.DELETE_WIKI_PAGE, false);
		}
	}

	private void configureShareAction() {
		if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
			actionMenu.setActionVisible(Action.SHARE, false);
		} else {
			actionMenu.setActionVisible(Action.SHARE, true);
			actionMenu.setActionListener(Action.SHARE, this);
			actionMenu.setActionText(Action.SHARE, entityTypeDisplay + " Sharing Settings");
			if (PublicPrivateBadge.isPublic(entityBundle.getBenefactorAcl(), ginInjector.getSynapseProperties().getPublicPrincipalIds())) {
				actionMenu.setActionIcon(Action.SHARE, IconType.GLOBE);
			} else {
				actionMenu.setActionIcon(Action.SHARE, IconType.LOCK);
			}
		}
	}

	public static boolean isVersionSupported(Entity entity, CookieProvider cookies) {
		return entity instanceof Versionable;
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
		return (entity instanceof Folder || (entity instanceof Project && EntityArea.FILES.equals(area)));
	}

	/**
	 * Can an entity of this type be moved?
	 *
	 * @param entity
	 * @return
	 */
	public boolean isMovableType(Entity entity) {
		if (entity instanceof Project || entity instanceof DockerRepository) {
			return false;
		}
		return true;
	}

	/**
	 * Can an entity of this type have a wiki?
	 *
	 * @param entity
	 * @return
	 */
	public boolean isWikiableType(Entity entity) {
		if (entity instanceof Link) {
			return false;
		}
		return true;
	}

	/**
	 * Can a link to this type be created?
	 *
	 * @param entity
	 * @return
	 */
	public boolean isLinkType(Entity entity) {
		if (entity instanceof Link) {
			return false;
		}
		return true;
	}

	/**
	 * Can an entity of this type be submitted to a challenge?
	 *
	 * @param entity
	 * @return
	 */
	public boolean isSubmittableType(Entity entity) {
		if (entity instanceof Table) {
			return false;
		}
		return entity instanceof Versionable || entity instanceof DockerRepository;
	}


	/**
	 * Can this entity be renamed (File and Project will have additional editable fields)?
	 *
	 * @param entity
	 * @return
	 */
	public boolean hasCustomRenameEditor(Entity entity) {
		if (entity instanceof FileEntity) {
			return true;
		} else if (entity instanceof Project) {
			return true;
		}
		return false;
	}

	@Override
	public void onAction(Action action) {
		switch (action) {
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
			case CREATE_TABLE_VERSION:
				onCreateTableViewSnapshot();
				break;
			case EDIT_DEFINING_SQL:
				onEditDefiningSql();
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
			case CREATE_OR_UPDATE_DOI:
				onCreateOrUpdateDoi();
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
			case UPLOAD_FILE:
				onUploadNewFileEntity();
				break;
			case CREATE_FOLDER:
				onCreateFolder();
				break;
			case UPLOAD_TABLE:
				onUploadTable();
				break;
			case ADD_TABLE:
				onAddTable();
				break;
			case ADD_FILE_VIEW:
				onAddFileView();
				break;
			case ADD_DATASET:
				onAddDataset();
				break;
			case ADD_PROJECT_VIEW:
				onAddProjectView();
				break;
			case ADD_SUBMISSION_VIEW:
				onAddSubmissionView();
				break;
			case ADD_MATERIALIZED_VIEW:
				onAddMaterializedView();
				break;
			case CREATE_EXTERNAL_DOCKER_REPO:
				onCreateExternalDockerRepo();
				break;
			case SHOW_PROJECT_STATS:
				onShowProjectStats();
				break;
			case TOGGLE_FULL_TEXT_SEARCH:
				onToggleFullTextSearch();
				break;
			default:
				break;
		}
	}

	public void onToggleFullTextSearch() {
		checkUpdateEntity(() -> {
			postCheckToggleFullTextSearch();
		});
	}

	private void postCheckToggleFullTextSearch() {
		TableEntity tableEntity = (TableEntity)entityBundle.getEntity();
		boolean newIsSearchEnabledValue = tableEntity.getIsSearchEnabled() == null ? true : !tableEntity.getIsSearchEnabled();
		tableEntity.setIsSearchEnabled(newIsSearchEnabledValue);
		getSynapseJavascriptClient().updateEntity(tableEntity, null, null, new AsyncCallback<Entity>() {
			@Override
			public void onFailure(Throwable caught) {
				// undo change, as we were unable to update the entity
				tableEntity.setIsSearchEnabled(!newIsSearchEnabledValue);
				view.showErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(Entity result) {
				fireEntityUpdatedEvent();
				String successTextPrefix = newIsSearchEnabledValue ? "Enabled" : "Disabled";
				view.showSuccess(successTextPrefix + " full text search for this table.");
			}
		});
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

	public void onShowProjectStats() {
		checkUpdateEntity(() -> {
			getStatisticsPlotWidget().configureAndShow(entityBundle.getEntity().getId());
		});
	}

	private void postCheckCreateExternalDockerRepo() {
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

	private void postCheckUploadTable() {
		getUploadTableModalWidget().configure(entityBundle.getEntity().getId(), null);
		getUploadTableModalWidget().showModal(entityUpdatedWizardCallback);
	}

	public void onAddFileView() {
		preflightController.checkCreateEntity(entityBundle, EntityView.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.file_view);
		});
	}

	public void onAddDataset() {
		preflightController.checkCreateEntity(entityBundle, Dataset.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.dataset);
		});
	}


	public void onAddProjectView() {
		preflightController.checkCreateEntity(entityBundle, EntityView.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.project_view);
		});
	}

	public void onAddTable() {
		preflightController.checkCreateEntity(entityBundle, TableEntity.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.table);
		});
	}
	
	public void onAddMaterializedView() {
		preflightController.checkCreateEntity(entityBundle, MaterializedView.class.getName(), () -> {
			// to create a MaterializedView, we need to know the definingSQL
			getMaterializedViewEditor().configure(entityBundle.getEntity().getId()).show();
		});
	}

	private void postCheckCreateTableOrView(TableType type) {
		getCreateTableViewWizard().configure(entityBundle.getEntity().getId(), type);
		getCreateTableViewWizard().showModal(new WizardCallback() {
			@Override
			public void onFinished() {
				// Intentionally empty
				// The modal will redirect us to the appropriate Place, which will trigger a full page update
			}

			@Override
			public void onCanceled() {}
		});
	}

	public void onAddSubmissionView() {
		preflightController.checkCreateEntity(entityBundle, SubmissionView.class.getName(), () -> {
			postCheckCreateTableOrView(TableType.submission_view);
		});
	}

	private void onUploadNewFileEntity() {
		checkUploadEntity(() -> {
			UploadDialogWidget uploader = getNewUploadDialogWidget();
			uploader.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, null, entityBundle.getEntity().getId(), null, true);
			uploader.setUploaderLinkNameVisible(true);
			uploader.show();
		});
	}

	private void onCreateFolder() {
		checkUploadEntity(() -> {
			AddFolderDialogWidget w = getAddFolderDialogWidget();
			w.show(entityBundle.getEntity().getId());
		});

	}

	private void onApproveUserAccess() {
		getApproveUserAccessModal().configure(entityBundle);
		getApproveUserAccessModal().show();
	}

	private void onManageAccessRequirements() {
		AccessRequirementsPlace place = new AccessRequirementsPlace(AccessRequirementsPlace.ID_PARAM + "=" + entity.getId() + "&" + AccessRequirementsPlace.TYPE_PARAM + "=" + RestrictableObjectType.ENTITY.toString());
		getGlobalApplicationState().getPlaceChanger().goTo(place);
	}

	private void onChangeStorageLocation() {
		checkUploadEntity(() -> {
			postChangeStorageLocation();
		});
	}

	private void postChangeStorageLocation() {
		getStorageLocationWidget().configure(this.entityBundle);
		getStorageLocationWidget().show();
	}

	private void onEditProvenance() {
		if (isCurrentVersion) {
			checkUpdateEntity(() -> {
				postEditProvenance();
			});
		} else {
			view.showErrorMessage("Can only edit the provenance of the most recent version.");
		}
	}

	private void postEditProvenance() {
		getProvenanceEditorWidget().configure(this.entityBundle);
		getProvenanceEditorWidget().show();
	}

	private void onUploadFile() {
		checkUploadEntity(() -> {
			postCheckUploadFile();
		});
	}

	private void postCheckUploadFile() {
		UploadDialogWidget uploadDialogWidget = getNewUploadDialogWidget();
		uploadDialogWidget.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, entityBundle.getEntity(), null, null, true);
		uploadDialogWidget.disableMultipleFileUploads();
		uploadDialogWidget.setUploaderLinkNameVisible(false);
		uploadDialogWidget.show();
	}

	private void onSubmit() {
		checkUpdateEntity(() -> {
			postOnSubmit();
		});
	}

	private void postOnSubmit() {
		getEvaluationSubmitter().configure(this.entityBundle.getEntity(), null, null);
	}

	private void onLink() {
		checkUpdateEntity(() -> {
			postCheckLink();
		});
	}

	private void postCheckLink() {
		getEntityFinderBuilder()
				.setInitialScope(EntityFinderScope.ALL_PROJECTS)
				.setInitialContainer(EntityFinderWidget.InitialContainer.NONE)
				.setSelectableTypes(CONTAINER)
				.setShowVersions(false)
				.setSelectedHandler((selected, entityFinder) -> {
					createLink(selected.getTargetId(), entityFinder);
				})
				.setTreeOnly(true)
				.setModalTitle("Create Link to " + entityTypeDisplay)
				.setHelpMarkdown("Search or Browse to find a Project or Folder that you have access to, and place a symbolic link for easy access")
				.setPromptCopy("Find a destination and place a link to <b>" + SafeHtmlUtils.fromString(entity.getName()).asString() + "</b> (" + entity.getId() + ")")
				.setSelectedCopy((count) -> "Destination")
				.setConfirmButtonCopy("Create Link")
				.build()
				.show();
	}

	/**
	 * Create a link with the given target as a parent.
	 *
	 * @param target
	 */
	public void createLink(String target, EntityFinderWidget finder) {
		Link link = new Link();
		link.setParentId(target);
		Reference ref = new Reference();
		ref.setTargetId(entityBundle.getEntity().getId());
		Long targetVersionNumber = null;
		if (isShowingVersion && entityBundle.getEntity() instanceof Versionable) {
			targetVersionNumber = ((Versionable) entityBundle.getEntity()).getVersionNumber();
		}
		ref.setTargetVersionNumber(targetVersionNumber);
		link.setLinksTo(ref); // links to this entity
		link.setLinksToClassName(entityBundle.getEntity().getClass().getName());
		link.setName(entityBundle.getEntity().getName()); // copy name of this entity as default
		getSynapseJavascriptClient().createEntity(link, new AsyncCallback<Entity>() {

			@Override
			public void onSuccess(Entity result) {
				view.showSuccess(DisplayConstants.TEXT_LINK_SAVED);
				finder.hide();
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof BadRequestException) {
					finder.showError(DisplayConstants.ERROR_CANT_MOVE_HERE);
					return;
				}
				if (caught instanceof NotFoundException) {
					finder.showError(DisplayConstants.ERROR_NOT_FOUND);
					return;
				}
				if (caught instanceof UnauthorizedException) {
					finder.showError(DisplayConstants.ERROR_NOT_AUTHORIZED);
					return;
				}
				finder.showError(caught.getMessage());

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
		EntityFinderWidget.Builder builder = getEntityFinderBuilder()
				.setModalTitle("Move " + entityTypeDisplay)
				.setHelpMarkdown("Search or Browse Synapse to find a destination to move this " + entityTypeDisplay)
				.setPromptCopy("Find a destination to move <b>" + SafeHtmlUtils.fromString(entity.getName()).asString() + "</b> (" + entity.getId() + ")")
				.setSelectedCopy((count) -> "Destination")
				.setConfirmButtonCopy("Move")
				.setShowVersions(false)
				.setTreeOnly(true)
				.setSelectedHandler((selected, finder) -> {
					String entityId = entityBundle.getEntity().getId();
					getSynapseClient().moveEntity(entityId, selected.getTargetId(), new AsyncCallback<Entity>() {

						@Override
						public void onSuccess(Entity result) {
							finder.hide();
							fireEntityUpdatedEvent();
							view.showSuccess(result.getId() + " successfully moved");
						}

						@Override
						public void onFailure(Throwable caught) {
							finder.showError(caught.getMessage());
						}
					});
				});

		if (entityBundle.getEntity() instanceof Table) {
			builder.setInitialScope(EntityFinderScope.ALL_PROJECTS)
					.setInitialContainer(EntityFinderWidget.InitialContainer.NONE)
					.setVisibleTypesInTree(PROJECT)
					.setSelectableTypes(PROJECT);
		} else {
			builder.setInitialScope(EntityFinderScope.CURRENT_PROJECT)
					.setInitialContainer(EntityFinderWidget.InitialContainer.PARENT)
					.setVisibleTypesInTree(CONTAINER)
					.setSelectableTypes(CONTAINER);
		}

		builder.build().show();
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

	private void checkUploadEntity(Callback cb) {
		preflightController.checkUploadToEntity(this.entityBundle, cb);
	}

	private void checkUpdateEntity(Callback cb) {
		preflightController.checkUpdateEntity(this.entityBundle, cb);
	}

	private void onEditWiki() {
		checkUpdateEntity(() -> {
			postCheckEditWiki();
		});
	}

	private void postCheckEditWiki() {
		// markdown editor will create a wiki if it does not already exist
		WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		getWikiMarkdownEditor().configure(key, wikiPage -> {
			fireEntityUpdatedEvent();
		});
	}

	private void onCreateTableViewSnapshot() {
		if (isCurrentVersion) {
			checkUpdateEntity(() -> {
				postCheckCreateTableViewSnapshot();
			});
		} else {
			view.showErrorMessage("Can only create a new version from the current table, not an old version.");
		}
	}
	
	private void onEditDefiningSql() {
		if (isCurrentVersion) {
			checkUpdateEntity(() -> {
				postCheckEditDefiningSql();
			});
		}
	}

	private PromptCallback getUpdateDefiningSqlCallback() {
		return value -> {
			((MaterializedView)entity).setDefiningSQL(value);
			getSynapseJavascriptClient().updateEntity(entity, null, false, new AsyncCallback<Entity>() {
				@Override
				public void onSuccess(Entity result) {
					fireEntityUpdatedEvent();
					view.showSuccess("Updated the Synapse SQL query that defines this Materialized View.");
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		};
	}
	
	private CallbackP<List<String>> getCreateSnapshotCallback(Entity entity) {
		if (entity instanceof TableEntity) {
			return values -> {
				String entityId = entityBundle.getEntity().getId();
				String label = values.get(0);
				String comment = values.get(1);
				String activityId = null;
				view.hideMultiplePromptDialog();
				view.showCreateVersionDialog();
				getSynapseJavascriptClient().createSnapshot(entityId, comment, label, activityId, new AsyncCallback<SnapshotResponse>() {
					@Override
					public void onSuccess(SnapshotResponse result) {
						view.hideCreateVersionDialog();
						fireEntityUpdatedEvent();

						Synapse newVersionPlace = new Synapse(entity.getId(), result.getSnapshotVersionNumber(), EntityArea.TABLES, null);
						ToastMessageOptions.Builder messageBuilder = new ToastMessageOptions.Builder();
						messageBuilder.setTitle(SNAPSHOT_CREATED);
						popupUtils.notify(SNAPSHOT_CREATED_DETAILS_TABLE, DisplayUtils.NotificationVariant.SUCCESS, messageBuilder.build());
						getGlobalApplicationState().getPlaceChanger().goTo(newVersionPlace);
					}

					@Override
					public void onFailure(Throwable caught) {
						view.hideCreateVersionDialog();
						view.showErrorMessage(caught.getMessage());
					}
				});
			};
		} else if (entity instanceof EntityView || entity instanceof Dataset) {
			return values -> {
				String entityId = entityBundle.getEntity().getId();
				String label = values.get(0);
				String comment = values.get(1);
				// create the version via an update table transaction
				// Start the job.
				TableUpdateTransactionRequest transactionRequest = new TableUpdateTransactionRequest();
				transactionRequest.setEntityId(entityId);
				SnapshotRequest snapshotRequest = new SnapshotRequest();
				snapshotRequest.setSnapshotLabel(label);
				snapshotRequest.setSnapshotComment(comment);
				transactionRequest.setSnapshotOptions(snapshotRequest);
				transactionRequest.setChanges(new ArrayList<>());
				transactionRequest.setCreateSnapshot(true);
				view.hideMultiplePromptDialog();
				view.showCreateVersionDialog();
				String message = entity instanceof EntityView ? CREATING_A_NEW_VIEW_VERSION_MESSAGE : CREATING_A_NEW_DATASET_VERSION_MESSAGE;
				getJobTrackingWidget().startAndTrackJob(message, false, AsynchType.TableTransaction, transactionRequest, new AsynchronousProgressHandler<TableUpdateTransactionResponse>() {

					@Override
					public void onFailure(Throwable failure) {
						view.hideCreateVersionDialog();
						view.showErrorMessage(failure.getMessage());
					}

					@Override
					public void onComplete(TableUpdateTransactionResponse response) {
						view.hideCreateVersionDialog();
						String errors = QueryResultEditorWidget.getEntityUpdateResultsFailures(response);
						if (!errors.isEmpty()) {
							view.showErrorMessage(errors);
						} else {
							fireEntityUpdatedEvent();

							ToastMessageOptions.Builder messageBuilder = new ToastMessageOptions.Builder();
							EntityArea newVersionArea;
							String toastMsg;
							Long newVersionNumber = response.getSnapshotVersionNumber();

							if (entity instanceof EntityView) {
								newVersionArea = EntityArea.TABLES;
								messageBuilder.setTitle(SNAPSHOT_CREATED);
								toastMsg = SNAPSHOT_CREATED_DETAILS_VIEW;
							} else {
								newVersionArea = EntityArea.DATASETS;
								messageBuilder.setTitle(STABLE_VERSION_CREATED);
								toastMsg = STABLE_VERSION_CREATED_DETAILS;

							}

							popupUtils.notify(toastMsg, DisplayUtils.NotificationVariant.SUCCESS, messageBuilder.build());
							getGlobalApplicationState().getPlaceChanger().goTo(new Synapse(entity.getId(), newVersionNumber, newVersionArea, null));

						}
					}

					@Override
					public void onCancel() {
						view.hideCreateVersionDialog();
					}
				});
			};
		} else {
			throw new IllegalArgumentException("A snapshot cannot be made of an entity with type: " + entity.getClass().getName());
		}
	}

	private String getCreateSnapshotTitleCopy(Entity entity) {
		if (entity instanceof TableEntity || entity instanceof EntityView) {
			return CREATE_SNAPSHOT;
		} else if (entity instanceof Dataset) {
			return CREATE_STABLE_VERSION;
		} else {
			throw new IllegalArgumentException("A snapshot cannot be made of an entity with type: " + entity.getClass().getName());
		}
	}

	private String getCreateSnapshotBodyCopy(Entity entity) {
		if (entity instanceof TableEntity) {
			return CREATE_NEW_TABLE_ENTITY_VERSION_PROMPT_BODY;
		} else if (entity instanceof EntityView) {
			return CREATE_NEW_VIEW_VERSION_PROMPT_BODY;
		} else if (entity instanceof Dataset) {
			return CREATE_NEW_DATASET_VERSION_PROMPT_BODY;
		} else {
			throw new IllegalArgumentException("A snapshot cannot be made of an entity with type: " + entity.getClass().getName());
		}
	}

	private void postCheckCreateTableViewSnapshot() {
		// prompt for new version label and comment
		List<String> prompts = new ArrayList<>();
		prompts.add("Label");
		prompts.add("Comment");
		PromptForValuesModalView.Configuration.Builder configBuilder = getPromptForValuesModalConfigBuilder();
		configBuilder.setTitle(getCreateSnapshotTitleCopy(entity))
				.addPrompt("Label", "")
				.addPrompt("Comment", "")
				.setBodyCopy(getCreateSnapshotBodyCopy(entity))
				.setCallback(getCreateSnapshotCallback(entity))
				.addHelpWidget(VERSIONING_HELP_MARKDOWN, VERSIONING_HELP_HREF);
		view.showMultiplePromptDialog(configBuilder.buildConfiguration());
	}

	private void onAddWikiSubpage() {
		checkUpdateEntity(() -> {
			postCheckAddWikiSubpage();
		});
	}

	private void postCheckAddWikiSubpage() {
		if (entityBundle.getRootWikiId() == null) {
			createWikiPage("Root");
		} else {
			view.showPromptDialog(DisplayConstants.ENTER_PAGE_TITLE, "", new PromptCallback() {
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
					view.showSuccess("'" + name + "' Page Added");
					Synapse newPlace = new Synapse(entityBundle.getEntity().getId(), getVersionIfNotLatest().orElse(null), EntityArea.WIKI, result.getId());
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
		checkUpdateEntity(() -> {
			postCheckRename();
		});
	}

	/**
	 * Called if the preflight check for a rename passes.
	 */
	private void postCheckRename() {
		getRenameEntityModalWidget().onRename(this.entity, () -> {
			fireEntityUpdatedEvent();
		});
	}

	private void onEditFileMetadata() {
		// Can only edit file metadata of the current file version
		if (isCurrentVersion) {
			// Validate the user can update this entity.
			checkUpdateEntity(() -> {
				postCheckEditFileMetadata();
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
		getEditFileMetadataModalWidget().configure((FileEntity) entityBundle.getEntity(), originalFileHandle, () -> {
			fireEntityUpdatedEvent();
		});
	}

	private void onEditProjectMetadata() {
		checkUpdateEntity(() -> {
			postCheckEditProjectMetadata();
		});
	}

	/**
	 * Called if the preflight check for a edit project metadata passes.
	 */
	private void postCheckEditProjectMetadata() {
		Boolean canChangeSettings = permissions.getCanChangeSettings();
		if (canChangeSettings == null) {
			canChangeSettings = false;
		}
		getEditProjectMetadataModalWidget().configure((Project) entityBundle.getEntity(), canChangeSettings, () -> {
			fireEntityUpdatedEvent();
		});
	}
	
	private void postCheckEditDefiningSql() {
		// prompt for defining sql
		view.showPromptDialog("Update SQL", ((MaterializedView)entity).getDefiningSQL(), getUpdateDefiningSqlCallback());
	}

	public void onDeleteWiki() {
		final WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		getWikiPageDeleteConfirmationDialog().show(key, parentWikiId -> {
			getGlobalApplicationState().getPlaceChanger().goTo(new Synapse(entityBundle.getEntity().getId(), null, EntityArea.WIKI, parentWikiId));
		});
	}

	@Override
	public void onDeleteEntity() {
		// Confirm the delete with the user. Mention that everything inside folder will also be deleted if
		// this is a folder entity.
		String display = ARE_YOU_SURE_YOU_WANT_TO_DELETE + this.entityTypeDisplay + " \"" + this.entity.getName() + "\"?";
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
				view.showInfo(THE + entityTypeDisplay + WAS_SUCCESSFULLY_DELETED);
				// Go to entity's parent
				Place gotoPlace = createDeletePlace();
				getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE + caught.getMessage());
			}
		});
	}

	/**
	 * Create the delete place
	 *
	 * @return
	 */
	public Place createDeletePlace() {
		String parentId = entityBundle.getEntity().getParentId();
		Place gotoPlace = null;
		if (parentId != null && !(entityBundle.getEntity() instanceof Project)) {
			if (entityBundle.getEntity() instanceof Dataset)
				gotoPlace = new Synapse(parentId, null, EntityArea.DATASETS, null);
			else if (entityBundle.getEntity() instanceof Table)
				gotoPlace = new Synapse(parentId, null, EntityArea.TABLES, null);
			else if (entityBundle.getEntity() instanceof DockerRepository)
				gotoPlace = new Synapse(parentId, null, EntityArea.DOCKER, null);
			else if (entityBundle.getEntity() instanceof FileEntity || entityBundle.getEntity() instanceof Folder)
				gotoPlace = new Synapse(parentId, null, EntityArea.FILES, null);
			else
				gotoPlace = new Synapse(parentId);
		} else {
			gotoPlace = new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.PROJECTS);
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
