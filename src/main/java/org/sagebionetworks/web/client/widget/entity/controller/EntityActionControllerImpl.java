package org.sagebionetworks.web.client.widget.entity.controller;

import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.repo.model.EntityType.dataset;
import static org.sagebionetworks.web.client.EntityTypeUtils.DATASET_COLLECTION_DISPLAY_NAME;
import static org.sagebionetworks.web.client.EntityTypeUtils.getFriendlyEntityTypeName;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFuture;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.CONTAINER;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.PROJECT;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_COLLECTOR_URL;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_1;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_2;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_PRIORITY;
import static org.sagebionetworks.web.shared.WebConstants.REVIEW_DATA_REQUEST_COMPONENT_ID;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.download.ActionRequiredList;
import org.sagebionetworks.repo.model.download.AddBatchOfFilesToDownloadListResponse;
import org.sagebionetworks.repo.model.download.EnableTwoFa;
import org.sagebionetworks.repo.model.download.MeetAccessRequirement;
import org.sagebionetworks.repo.model.download.RequestDownload;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetCollection;
import org.sagebionetworks.repo.model.table.EntityRefCollectionView;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.HasDefiningSql;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.SnapshotRequest;
import org.sagebionetworks.repo.model.table.SnapshotResponse;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionResponse;
import org.sagebionetworks.repo.model.table.VirtualTable;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.NotificationVariant;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.context.KeyFactoryProvider;
import org.sagebionetworks.web.client.context.QueryClientProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.AlertButtonConfig;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.jsinterop.KeyFactory;
import org.sagebionetworks.web.client.jsinterop.ReactMouseEvent;
import org.sagebionetworks.web.client.jsinterop.ToastMessageOptions;
import org.sagebionetworks.web.client.jsinterop.reactquery.InvalidateQueryFilters;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.LoginPlace;
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
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModal;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.SqlDefinedEditorModalWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageDeleteConfirmationDialog;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.ActionListener;
import org.sagebionetworks.web.client.widget.entity.menu.v3.DefaultEntityActionMenuLayoutUtil;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

public class EntityActionControllerImpl
  implements EntityActionController, ActionListener {

  public static final String AVAILABLE_IN_VERSION_HISTORY =
    "This will be available within your version history.";

  public static final String SNAPSHOT = "Snapshot";
  public static final String STABLE_VERSION = "Stable Version";

  public static final String CREATE_SNAPSHOT = "Create " + SNAPSHOT;
  public static final String CREATE_STABLE_VERSION = "Create " + STABLE_VERSION;

  public static final String SNAPSHOT_CREATED = SNAPSHOT + " Created";
  public static final String STABLE_VERSION_CREATED =
    STABLE_VERSION + " Created";

  public static final String STABLE_VERSION_CREATED_DETAILS =
    "You created a " + STABLE_VERSION + " of this Dataset";
  public static final String SNAPSHOT_CREATED_DETAILS_VIEW =
    "You created a " + SNAPSHOT + " of this View.";
  public static final String SNAPSHOT_CREATED_DETAILS_TABLE =
    "You created a " + SNAPSHOT + " of this Table.";

  public static final String CREATE_NEW_VIEW_VERSION_PROMPT_BODY =
    "You're about to create a " +
    SNAPSHOT +
    " of this View. " +
    AVAILABLE_IN_VERSION_HISTORY;

  public static final String CREATE_NEW_DATASET_COLLECTION_VERSION_PROMPT_BODY =
    "You're about to create a " +
    SNAPSHOT +
    " of this " +
    DATASET_COLLECTION_DISPLAY_NAME +
    ". " +
    AVAILABLE_IN_VERSION_HISTORY;
  public static final String CREATE_NEW_DATASET_VERSION_PROMPT_BODY =
    "You're about to create a " +
    STABLE_VERSION +
    " of this Dataset. " +
    AVAILABLE_IN_VERSION_HISTORY;
  public static final String CREATE_NEW_TABLE_ENTITY_VERSION_PROMPT_BODY =
    "You're about to create a " +
    SNAPSHOT +
    " of this Table. " +
    AVAILABLE_IN_VERSION_HISTORY;

  public static final String CREATING_A_NEW_VIEW_VERSION_MESSAGE =
    "Creating a new View " + SNAPSHOT + "...";
  public static final String CREATING_A_NEW_DATASET_VERSION_MESSAGE =
    "Creating a new Dataset " + STABLE_VERSION + "...";

  public static final String VERSIONING_HELP_MARKDOWN =
    "This will create an immutable version, which will be available in your version history.";
  public static final String VERSIONING_HELP_HREF =
    "https://help.synapse.org/docs/Versioning.2003730726.html";

  public static final String TOOLS = " Tools";

  public static final String MOVE_PREFIX = "Move ";

  public static final String EDIT_WIKI_PREFIX = "Edit ";
  public static final String WIKI = " Wiki";

  public static final String THE = "The ";

  public static final String WAS_SUCCESSFULLY_DELETED =
    " was successfully deleted.";

  public static final String DELETED = "Deleted";

  public static final String ARE_YOU_SURE_YOU_WANT_TO_DELETE =
    "Are you sure you want to delete ";
  public static final String DELETE_FOLDER_EXPLANATION =
    " Everything contained within the Folder will also be deleted.";
  public static final String CONFIRM_DELETE_TITLE = "Confirm Delete";

  public static final String DELETE_PREFIX = "Delete ";

  public static final String RENAME_PREFIX = "Rename ";
  public static final String EDIT_NAME_AND_DESCRIPTION =
    "Edit Name and Description";

  public static final int IS_ACT_MEMBER_MASK = 0x20;

  public static final String CREATE_DOI_FOR = "Create DOI for  ";
  public static final String UPDATE_DOI_FOR = "Update DOI for  ";
  public static final String REQUEST_DOWNLOAD_GUIDANCE =
    "Request access from an administrator, shown under File Tools ➔ File Sharing Settings.";
  public static final String ACCESS_REQUIREMENT_GUIDANCE =
    "This controlled data has additional requirements. Click \"Request Access\" underneath the SynID and follow the instructions.";
  public static final String ENABLE_2FA_GUIDANCE =
    "You must enable two-factor authentication to download this file.";
  public static final String NO_PERMISSION_TO_DOWNLOAD =
    "You don't have permission to download this file.";

  EntityArea currentArea;

  EntityActionControllerView view;
  PreflightController preflightController;
  SynapseClientAsync synapseClient;
  SynapseJavascriptClient jsClient;
  GlobalApplicationState globalApplicationState;
  FileClientsHelp fileClientsHelp;
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
  EntityActionMenu actionMenu;
  WikiMarkdownEditor wikiEditor;
  ProvenanceEditorWidget provenanceEditor;
  StorageLocationWidget storageLocationEditor;
  AddToDownloadListV2 addToDownloadListWidget;
  CookieProvider cookies;
  ChallengeClientAsync challengeClient;
  SelectTeamModal selectTeamModal;
  CreateOrUpdateDoiModal createOrUpdateDoiModal;
  ApproveUserAccessModal approveUserAccessModal;
  PortalGinInjector ginInjector;
  IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  AddFolderDialogWidget addFolderDialogWidget;
  CreateTableViewWizard createTableViewWizard;
  CreateDatasetOrCollection createDatasetOrCollection;
  SqlDefinedEditorModalWidget sqlDefinedEditorModalWidget;
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
  ContainerClientsHelp containerClientsHelp;
  QueryClient queryClient;
  KeyFactoryProvider keyFactoryProvider;

  @Inject
  public EntityActionControllerImpl(
    EntityActionControllerView view,
    PreflightController preflightController,
    PortalGinInjector ginInjector,
    AuthenticationController authenticationController,
    CookieProvider cookies,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler,
    GWTWrapper gwt,
    EventBus eventBus,
    PopupUtilsView popupUtilsView,
    QueryClientProvider queryClientProvider,
    KeyFactoryProvider keyFactoryProvider
  ) {
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
    this.queryClient = queryClientProvider.getQueryClient();
    this.keyFactoryProvider = keyFactoryProvider;
    entityUpdatedWizardCallback =
      new WizardCallback() {
        @Override
        public void onFinished() {
          fireEntityUpdatedEvent();
        }

        @Override
        public void onCanceled() {}
      };
  }

  private void fireEntityUpdatedEvent() {
    eventBus.fireEvent(new EntityUpdatedEvent(entity.getId()));
  }

  private WikiPageDeleteConfirmationDialog getWikiPageDeleteConfirmationDialog() {
    if (wikiPageDeleteConfirmationDialog == null) {
      wikiPageDeleteConfirmationDialog =
        ginInjector.getWikiPageDeleteConfirmationDialog();
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
      selectTeamModal.setPrimaryButtonText("Create Challenge");
      selectTeamModal.configure(
        new CallbackP<String>() {
          @Override
          public void invoke(String selectedTeamId) {
            onSelectChallengeTeam(selectedTeamId);
          }
        }
      );
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

  private FileClientsHelp getFileClientsHelp() {
    if (fileClientsHelp == null) {
      fileClientsHelp = ginInjector.getFileClientsHelp();
    }
    return fileClientsHelp;
  }

  private AccessControlListModalWidget getAccessControlListModalWidget() {
    if (accessControlListModalWidget == null) {
      accessControlListModalWidget =
        ginInjector.getAccessControlListModalWidget();
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

  private CreateDatasetOrCollection getCreateDatasetOrCollection() {
    if (createDatasetOrCollection == null) {
      createDatasetOrCollection = ginInjector.getCreateDatasetOrCollection();
      this.view.addWidget(createDatasetOrCollection.asWidget());
    }
    return createDatasetOrCollection;
  }

  private UploadTableModalWidget getUploadTableModalWidget() {
    if (uploadTableModalWidget == null) {
      uploadTableModalWidget = ginInjector.getUploadTableModalWidget();
      view.addWidget(uploadTableModalWidget);
    }
    return uploadTableModalWidget;
  }

  private SqlDefinedEditorModalWidget getSqlDefinedEditorModalWidget() {
    if (sqlDefinedEditorModalWidget == null) {
      sqlDefinedEditorModalWidget =
        ginInjector.getSqlDefinedEditorModalWidget();
      view.addWidget(sqlDefinedEditorModalWidget);
    }
    return sqlDefinedEditorModalWidget;
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
      editFileMetadataModalWidget =
        ginInjector.getEditFileMetadataModalWidget();
    }
    return editFileMetadataModalWidget;
  }

  private EditProjectMetadataModalWidget getEditProjectMetadataModalWidget() {
    if (editProjectMetadataModalWidget == null) {
      editProjectMetadataModalWidget =
        ginInjector.getEditProjectMetadataModalWidget();
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

  private AddToDownloadListV2 getAddToDownloadListWidget() {
    return addToDownloadListWidget;
  }

  private ContainerClientsHelp getContainerClientsHelp() {
    if (containerClientsHelp == null) {
      containerClientsHelp = ginInjector.getContainerClientsHelp();
    }
    return containerClientsHelp;
  }

  private PromptForValuesModalView.Configuration.Builder getPromptForValuesModalConfigBuilder() {
    return ginInjector.getPromptForValuesModalConfigurationBuilder();
  }

  @Override
  public void configure(
    EntityActionMenu actionMenu,
    EntityBundle entityBundle,
    boolean isCurrentVersion,
    String wikiPageId,
    EntityArea currentArea,
    AddToDownloadListV2 addToDownloadListWidget
  ) {
    this.entityBundle = entityBundle;
    this.wikiPageId = wikiPageId;
    this.permissions = entityBundle.getPermissions();
    this.actionMenu = actionMenu;
    this.entity = entityBundle.getEntity();
    this.isUserAuthenticated = authenticationController.isLoggedIn();
    this.isCurrentVersion = isCurrentVersion;
    this.entityTypeDisplay =
      getFriendlyEntityTypeName(entityBundle.getEntity());
    this.currentArea = currentArea;
    this.addToDownloadListWidget = addToDownloadListWidget;

    reconfigureActions();
  }

  private void reconfigureActions() {
    // make the button a skeleton while we determine what we can show
    actionMenu.setIsLoading(true);

    // hide all commands by default
    actionMenu.hideAllActions();

    // Set up the action menu layout
    configureActionMenuLayout();

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
    configureProjectHelpAction();
    configureEditFileMetadataAction();
    configureTableCommands();
    configureProjectLevelTableCommands();
    configureProjectLevelDatasetCommands();
    configureAddFolder();
    configureUploadNewFileEntity();
    configureAddExternalDockerRepo();
    configureStatisticsPlotAction();
    configureFullTextSearch();
    configureReportViolation();

    // These configuration methods are asynchronous
    FluentFuture fileDownloadFuture = configureFileDownload();
    FluentFuture containerDownloadFuture = configureContainerDownload();
    FluentFuture challengeFuture = configureCreateChallenge();
    FluentFuture actFuture = configureACTCommands();
    FluentFuture reorderWikiSubpagesFuture = configureReorderWikiSubpages();

    // Show the button
    FluentFuture.from(
      whenAllComplete(
        fileDownloadFuture,
        containerDownloadFuture,
        challengeFuture,
        actFuture,
        reorderWikiSubpagesFuture
      )
        .call(
          () -> {
            actionMenu.setIsLoading(false);
            return null;
          },
          directExecutor()
        )
    );
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

  private void configureReportViolation() {
    actionMenu.setActionVisible(
      Action.REPORT_VIOLATION,
      !(entityBundle.getEntity() instanceof Project)
    );
    actionMenu.setActionListener(
      Action.REPORT_VIOLATION,
      (action, event) -> {
        // report abuse via Jira issue collector
        String userId = WebConstants.ANONYMOUS, email =
          WebConstants.ANONYMOUS, displayName = WebConstants.ANONYMOUS, synId =
          entity.getId();
        UserProfile userProfile =
          authenticationController.getCurrentUserProfile();
        if (userProfile != null) {
          userId = userProfile.getOwnerId();
          displayName = DisplayUtils.getDisplayName(userProfile);
          email = DisplayUtils.getPrimaryEmail(userProfile);
        }

        ginInjector
          .getSynapseJSNIUtils()
          .showJiraIssueCollector(
            "", // summary
            FLAG_ISSUE_DESCRIPTION_PART_1 +
            gwt.getCurrentURL() +
            FLAG_ISSUE_DESCRIPTION_PART_2,
            FLAG_ISSUE_COLLECTOR_URL,
            userId,
            displayName,
            email,
            synId, // Synapse data object ID
            REVIEW_DATA_REQUEST_COMPONENT_ID,
            null, // AR ID
            FLAG_ISSUE_PRIORITY
          );
      }
    );
  }

  private void configureFullTextSearch() {
    if (
      entityBundle.getEntity() instanceof Table &&
      entityBundle.getPermissions().getCanCertifiedUserEdit()
    ) {
      Table tableEntity = (Table) entityBundle.getEntity();
      actionMenu.setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, true);
      actionMenu.setActionListener(Action.TOGGLE_FULL_TEXT_SEARCH, this);
      boolean isFTSEnabled = tableEntity.getIsSearchEnabled() == null
        ? false
        : tableEntity.getIsSearchEnabled();
      String actionTextPrefix = isFTSEnabled ? "Disable" : "Enable";
      actionMenu.setActionText(
        Action.TOGGLE_FULL_TEXT_SEARCH,
        actionTextPrefix + " Full Text Search"
      );
    } else {
      actionMenu.setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, false);
    }
  }

  private FluentFuture configureFileDownload() {
    FluentFuture<RestrictionInformationResponse> restrictionInformationFuture =
      getDoneFuture(null);

    if (entity instanceof FileEntity) {
      boolean canDownload = entityBundle.getPermissions().getCanDownload();
      actionMenu.setDownloadMenuEnabled(canDownload);
      if (canDownload) {
        actionMenu.setDownloadMenuTooltipText("");
      } else {
        if (!authenticationController.isLoggedIn()) {
          actionMenu.setDownloadMenuTooltipText(
            "You need to log in to download this file."
          );
        } else {
          actionMenu.setDownloadMenuTooltipText(NO_PERMISSION_TO_DOWNLOAD);
          // Queue up request to identify reasons why the file cannot be downloaded, and update the tooltip when the request finishes.
          getSynapseJavascriptClient()
            .getActionsRequiredForEntityDownload(entity.getId())
            .addCallback(
              new FutureCallback<ActionRequiredList>() {
                @Override
                public void onSuccess(@Nullable ActionRequiredList result) {
                  if (result != null) {
                    StringBuilder downloadMenuTooltipText = new StringBuilder(
                      NO_PERMISSION_TO_DOWNLOAD
                    );
                    // There may be multiple actions of the same class, but we only want to show one message for each type
                    // Get the unique set of action classes.
                    Set<
                      Class<
                        ? extends org.sagebionetworks.repo.model.download.Action
                      >
                    > uniqueClasses = result
                      .getActions()
                      .stream()
                      .map(
                        org.sagebionetworks.repo.model.download.Action::getClass
                      )
                      .collect(Collectors.toSet());

                    for (Class<
                      ? extends org.sagebionetworks.repo.model.download.Action
                    > clazz : uniqueClasses) {
                      downloadMenuTooltipText
                        .append("\n\n")
                        .append(
                          getTooltipTextForRequiredActionForDownload(clazz)
                        );
                    }
                    actionMenu.setDownloadMenuTooltipText(
                      downloadMenuTooltipText.toString()
                    );
                  }
                }

                @Override
                public void onFailure(Throwable caught) {
                  view.showErrorMessage(caught.getMessage());
                }
              },
              directExecutor()
            );
        }
      }
      actionMenu.setActionVisible(Action.DOWNLOAD_FILE, true);

      actionMenu.setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
      actionMenu.setActionListener(
        Action.ADD_TO_DOWNLOAD_CART,
        (action, e) -> {
          if (!authenticationController.isLoggedIn()) {
            view.showErrorMessage(
              "You will need to sign in to add a file to the Download List."
            );
            getGlobalApplicationState()
              .getPlaceChanger()
              .goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
          } else {
            FileEntity entity = (FileEntity) entityBundle.getEntity();

            getSynapseJavascriptClient()
              .addFileToDownloadListV2(
                entity.getId(),
                entity.getVersionNumber(),
                new AsyncCallback<AddBatchOfFilesToDownloadListResponse>() {
                  @Override
                  public void onFailure(Throwable caught) {
                    view.showErrorMessage(caught.getMessage());
                  }

                  public void onSuccess(
                    AddBatchOfFilesToDownloadListResponse result
                  ) {
                    String href = "#!DownloadCart:0";
                    popupUtils.showInfo(
                      entity.getName() + EntityBadge.ADDED_TO_DOWNLOAD_LIST,
                      href,
                      DisplayConstants.VIEW_DOWNLOAD_LIST
                    );
                    eventBus.fireEvent(new DownloadListUpdatedEvent());
                  }
                }
              );
          }
        }
      );

      actionMenu.setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);
      actionMenu.setActionListener(
        Action.SHOW_PROGRAMMATIC_OPTIONS,
        (action, e) ->
          getFileClientsHelp()
            .configureAndShow(
              entity.getId(),
              ((FileEntity) entity).getVersionNumber()
            )
      );

      restrictionInformationFuture =
        getDoneFuture(entityBundle.getRestrictionInformation());
      if (entityBundle.getRestrictionInformation() == null) {
        restrictionInformationFuture =
          getSynapseJavascriptClient()
            .getRestrictionInformation(
              entity.getId(),
              RestrictableObjectType.ENTITY
            );
      }
      restrictionInformationFuture.addCallback(
        new FutureCallback<RestrictionInformationResponse>() {
          @Override
          public void onSuccess(
            @Nullable RestrictionInformationResponse restrictionInformation
          ) {
            ginInjector
              .getFileDownloadHandlerWidget()
              .configure(actionMenu, entityBundle, restrictionInformation);
          }

          @Override
          public void onFailure(Throwable t) {
            popupUtils.showErrorMessage(t.getMessage());
          }
        },
        directExecutor()
      );
    }
    return restrictionInformationFuture;
  }

  private FluentFuture configureContainerDownload() {
    FluentFuture<EntityChildrenResponse> future = getDoneFuture(null);

    if (
      (entity instanceof Project && EntityArea.FILES.equals(currentArea)) ||
      entity instanceof Folder
    ) {
      actionMenu.setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
      actionMenu.setActionListener(
        Action.ADD_TO_DOWNLOAD_CART,
        (action, e) -> getAddToDownloadListWidget().configure(entity.getId())
      );
      actionMenu.setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);
      actionMenu.setActionListener(
        Action.SHOW_PROGRAMMATIC_OPTIONS,
        (action, e) ->
          getContainerClientsHelp().configureAndShow(entity.getId())
      );

      if (Boolean.TRUE.equals(entityBundle.getHasChildren())) {
        actionMenu.setDownloadMenuEnabled(true);
        actionMenu.setDownloadMenuTooltipText(null);
        // Check if the container has any files
        EntityChildrenRequest filesRequest = new EntityChildrenRequest();
        filesRequest.setParentId(entity.getId());
        filesRequest.setIncludeSumFileSizes(false);
        filesRequest.setIncludeTotalChildCount(false);
        filesRequest.setIncludeTypes(
          Collections.singletonList(EntityType.file)
        );
        future = getSynapseJavascriptClient().getEntityChildren(filesRequest);
        future.addCallback(
          new FutureCallback<EntityChildrenResponse>() {
            @Override
            public void onSuccess(EntityChildrenResponse result) {
              if (result.getPage().isEmpty()) {
                actionMenu.setActionEnabled(Action.ADD_TO_DOWNLOAD_CART, false);
                actionMenu.setActionTooltipText(
                  Action.ADD_TO_DOWNLOAD_CART,
                  "There are no files in this folder."
                );
              } else {
                actionMenu.setActionEnabled(Action.ADD_TO_DOWNLOAD_CART, true);
                actionMenu.setActionTooltipText(
                  Action.ADD_TO_DOWNLOAD_CART,
                  null
                );
              }
            }

            @Override
            public void onFailure(Throwable t) {
              view.showErrorMessage(t.getMessage());
            }
          },
          directExecutor()
        );
      } else {
        actionMenu.setDownloadMenuEnabled(false);
        actionMenu.setDownloadMenuTooltipText(
          "There are no downloadable items in this folder."
        );
      }
    }
    return future;
  }

  private void configureAddExternalDockerRepo() {
    if (
      entityBundle.getEntity() instanceof Project &&
      EntityArea.DOCKER.equals(currentArea)
    ) {
      actionMenu.setActionVisible(
        Action.CREATE_EXTERNAL_DOCKER_REPO,
        entityBundle.getPermissions().getCanCertifiedUserEdit()
      );
      actionMenu.setActionListener(Action.CREATE_EXTERNAL_DOCKER_REPO, this);
    } else {
      actionMenu.setActionVisible(Action.CREATE_EXTERNAL_DOCKER_REPO, false);
    }
  }

  private void configureProjectLevelTableCommands() {
    if (
      entityBundle.getEntity() instanceof Project &&
      EntityArea.TABLES.equals(currentArea)
    ) {
      // show tables top level commands
      boolean canEditResults = entityBundle
        .getPermissions()
        .getCanCertifiedUserEdit();
      actionMenu.setActionVisible(Action.UPLOAD_TABLE, canEditResults);
      actionMenu.setActionListener(Action.UPLOAD_TABLE, this);
      actionMenu.setActionVisible(Action.ADD_TABLE, canEditResults);
      actionMenu.setActionListener(Action.ADD_TABLE, this);
    } else {
      actionMenu.setActionVisible(Action.UPLOAD_TABLE, false);
      actionMenu.setActionVisible(Action.ADD_TABLE, false);
    }
  }

  private void configureProjectLevelDatasetCommands() {
    if (
      entityBundle.getEntity() instanceof Project &&
      EntityArea.DATASETS.equals(currentArea)
    ) {
      // show tables top level commands
      boolean canEditResults = entityBundle
        .getPermissions()
        .getCanCertifiedUserEdit();
      actionMenu.setActionVisible(Action.ADD_DATASET, canEditResults);
      actionMenu.setActionListener(Action.ADD_DATASET, this);
      actionMenu.setActionVisible(
        Action.ADD_DATASET_COLLECTION,
        canEditResults
      );
      actionMenu.setActionListener(Action.ADD_DATASET_COLLECTION, this);
    } else {
      actionMenu.setActionVisible(Action.ADD_DATASET, false);
      actionMenu.setActionVisible(Action.ADD_DATASET_COLLECTION, false);
    }
  }

  private FluentFuture configureACTCommands() {
    // TODO: remove APPROVE_USER_ACCESS command (after new ACT feature is released, where the system
    // supports the workflow)
    actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, false);
    actionMenu.setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, false);
    // show ACT commands if this is the Project Settings tools menu, or if the entity is not a project
    // (looking at a child entity)
    if (
      authenticationController.isLoggedIn() &&
      !isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)
    ) {
      FluentFuture future = isACTMemberAsyncHandler.isACTActionAvailable();
      future.addCallback(
        new FutureCallback<Boolean>() {
          @Override
          public void onSuccess(@Nullable Boolean isACT) {
            if (isACT) {
              actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, true);
              actionMenu.setActionListener(
                Action.APPROVE_USER_ACCESS,
                EntityActionControllerImpl.this
              );
              actionMenu.setActionVisible(
                Action.MANAGE_ACCESS_REQUIREMENTS,
                true
              );
              actionMenu.setActionListener(
                Action.MANAGE_ACCESS_REQUIREMENTS,
                EntityActionControllerImpl.this
              );
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            view.showErrorMessage(caught.getMessage());
          }
        },
        directExecutor()
      );
      return future;
    }
    return getDoneFuture(null);
  }

  public void onSelectChallengeTeam(String id) {
    Challenge c = new Challenge();
    c.setProjectId(entity.getId());
    c.setParticipantTeamId(id);
    getChallengeClient()
      .createChallenge(
        c,
        new AsyncCallback<Challenge>() {
          @Override
          public void onSuccess(Challenge v) {
            view.showSuccess(DisplayConstants.CHALLENGE_CREATED);
            // go to challenge tab
            Place gotoPlace = new Synapse(
              entity.getId(),
              null,
              EntityArea.CHALLENGE,
              null
            );
            getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
          }

          @Override
          public void onFailure(Throwable caught) {
            view.showErrorMessage(caught.getMessage());
          }
        }
      );
  }

  private void configureProvenance() {
    if (
      entityBundle.getEntity() instanceof FileEntity ||
      entityBundle.getEntity() instanceof DockerRepository ||
      entityBundle.getEntity() instanceof Table
    ) {
      actionMenu.setActionVisible(
        Action.EDIT_PROVENANCE,
        permissions.getCanCertifiedUserEdit()
      );
      actionMenu.setActionListener(Action.EDIT_PROVENANCE, this);
      actionMenu.setActionText(
        Action.EDIT_PROVENANCE,
        "Edit " + entityTypeDisplay + " Provenance"
      );
    } else {
      actionMenu.setActionVisible(Action.EDIT_PROVENANCE, false);
    }
  }

  private void configureChangeStorageLocation() {
    if (
      entityBundle.getEntity() instanceof Folder ||
      (entityBundle.getEntity() instanceof Project && currentArea == null)
    ) {
      actionMenu.setActionVisible(
        Action.CHANGE_STORAGE_LOCATION,
        permissions.getCanCertifiedUserEdit()
      );
      actionMenu.setActionText(
        Action.CHANGE_STORAGE_LOCATION,
        "Change " + entityTypeDisplay + " Storage Location"
      );
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
    if (
      entityBundle.getEntity() instanceof Project &&
      canEdit &&
      (currentArea == null || EntityArea.CHALLENGE.equals(currentArea))
    ) {
      actionMenu.setActionListener(Action.CREATE_CHALLENGE, this);
      actionMenu.setActionListener(Action.DELETE_CHALLENGE, this);

      // find out if this project has a challenge
      FluentFuture<Challenge> future = getSynapseJavascriptClient()
        .getChallengeForProject(entity.getId());
      future.addCallback(
        new FutureCallback<Challenge>() {
          @Override
          public void onSuccess(Challenge result) {
            // challenge found
            currentChallengeId = result.getId();
            actionMenu.setActionVisible(
              Action.DELETE_CHALLENGE,
              EntityArea.CHALLENGE.equals(currentArea)
            );
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
        },
        directExecutor()
      );
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
    if (
      canEdit &&
      !isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)
    ) {
      actionMenu.setActionListener(Action.CREATE_OR_UPDATE_DOI, this);
      actionMenu.setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
      if (entityBundle.getDoiAssociation() == null) {
        // show command if not returned, thus not in existence
        actionMenu.setActionText(
          Action.CREATE_OR_UPDATE_DOI,
          CREATE_DOI_FOR + entityTypeDisplay
        );
      } else {
        actionMenu.setActionText(
          Action.CREATE_OR_UPDATE_DOI,
          UPDATE_DOI_FOR + entityTypeDisplay
        );
      }
    }
  }

  private void onCreateOrUpdateDoi() {
    getCreateOrUpdateDoiModal()
      .configureAndShow(
        entity,
        getVersionIfNotLatest(),
        authenticationController.getCurrentUserProfile()
      );
  }

  private void onCreateChallenge() {
    getSelectTeamModal().show();
  }

  private void onDeleteChallenge() {
    // Confirm the delete with the user.
    view.showConfirmDeleteDialog(
      DisplayConstants.CONFIRM_DELETE_CHALLENGE,
      () -> {
        postConfirmedDeleteChallenge();
      }
    );
  }

  /**
   * Called after the user has confirmed the delete of the challenge.
   */
  public void postConfirmedDeleteChallenge() {
    // The user has confirmed the delete, the next step is the preflight check.
    preflightController.checkDeleteEntity(
      this.entityBundle,
      () -> {
        postCheckDeleteChallenge();
      }
    );
  }

  public void postCheckDeleteChallenge() {
    getChallengeClient()
      .deleteChallenge(
        currentChallengeId,
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            view.showInfo(THE + "challenge" + WAS_SUCCESSFULLY_DELETED);
            fireEntityUpdatedEvent();
          }

          @Override
          public void onFailure(Throwable caught) {
            view.showErrorMessage(caught.getMessage());
          }
        }
      );
  }

  private void configureTableCommands() {
    if (entityBundle.getEntity() instanceof Table) {
      boolean isEntityRefCollectionView = entityBundle.getEntity() instanceof
      EntityRefCollectionView;
      boolean canEditResults =
        permissions.getCanCertifiedUserEdit() &&
        isEditCellValuesSupported(entityBundle.getEntity());

      actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, canEditResults);
      actionMenu.setActionText(
        Action.UPLOAD_TABLE_DATA,
        "Upload Data to " + entityTypeDisplay
      );
      actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, canEditResults);
      actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, true);
      actionMenu.setActionVisible(
        Action.SHOW_VIEW_SCOPE,
        isDefinedByScope(entityBundle.getEntity())
      );
      actionMenu.setActionVisible(
        Action.EDIT_ENTITYREF_COLLECTION_ITEMS,
        permissions.getCanCertifiedUserEdit() &&
        isEntityRefCollectionView &&
        isCurrentVersion
      );
      actionMenu.setActionVisible(
        Action.EDIT_DEFINING_SQL,
        permissions.getCanCertifiedUserEdit() &&
        isDefinedBySql(entityBundle.getEntity()) &&
        isCurrentVersion
      );
      actionMenu.setActionListener(Action.EDIT_DEFINING_SQL, this);

      actionMenu.setActionVisible(
        Action.VIEW_DEFINING_SQL,
        !permissions.getCanCertifiedUserEdit() &&
        isDefinedBySql(entityBundle.getEntity()) &&
        isCurrentVersion
      );
      actionMenu.setActionListener(Action.VIEW_DEFINING_SQL, this);
    } else {
      actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, false);
      actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, false);
      actionMenu.setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
      actionMenu.setActionVisible(Action.SHOW_VIEW_SCOPE, false);
      actionMenu.setActionVisible(
        Action.EDIT_ENTITYREF_COLLECTION_ITEMS,
        false
      );
      actionMenu.setActionVisible(Action.EDIT_DEFINING_SQL, false);
    }
  }

  private void configureFileUpload() {
    if (entityBundle.getEntity() instanceof FileEntity) {
      actionMenu.setActionVisible(
        Action.UPLOAD_NEW_FILE,
        permissions.getCanCertifiedUserEdit()
      );
      actionMenu.setActionListener(Action.UPLOAD_NEW_FILE, this);
    } else {
      actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, false);
    }
  }

  private void configureUploadNewFileEntity() {
    if (isContainerOnFilesTab(entityBundle.getEntity(), currentArea)) {
      actionMenu.setActionVisible(
        Action.UPLOAD_FILE,
        permissions.getCanCertifiedUserEdit()
      );
      actionMenu.setActionListener(Action.UPLOAD_FILE, this);
    } else {
      actionMenu.setActionVisible(Action.UPLOAD_FILE, false);
    }
  }

  private void configureAddFolder() {
    if (isContainerOnFilesTab(entityBundle.getEntity(), currentArea)) {
      actionMenu.setActionVisible(
        Action.CREATE_FOLDER,
        permissions.getCanCertifiedUserEdit()
      );
      actionMenu.setActionListener(Action.CREATE_FOLDER, this);
    } else {
      actionMenu.setActionVisible(Action.CREATE_FOLDER, false);
    }
  }

  private void configureEditWiki() {
    if (isWikiableConfig(entityBundle.getEntity(), currentArea)) {
      actionMenu.setActionVisible(
        Action.EDIT_WIKI_PAGE,
        permissions.getCanCertifiedUserEdit()
      );
      actionMenu.setActionListener(Action.EDIT_WIKI_PAGE, this);
      actionMenu.setActionText(
        Action.EDIT_WIKI_PAGE,
        EDIT_WIKI_PREFIX + entityTypeDisplay + WIKI
      );
    } else {
      actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, false);
    }
  }

  private void configureViewWikiSource() {
    // only visible if entity may have a wiki
    if (isWikiableConfig(entityBundle.getEntity(), currentArea)) {
      actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, true);
      actionMenu.setActionListener(Action.VIEW_WIKI_SOURCE, this);
      actionMenu.setActionEnabled(
        Action.VIEW_WIKI_SOURCE,
        entityBundle.getRootWikiId() != null
      );
      actionMenu.setActionTooltipText(
        Action.VIEW_WIKI_SOURCE,
        entityBundle.getRootWikiId() == null
          ? "This " + entityTypeDisplay + " has no wiki."
          : null
      );
    } else {
      actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, false);
    }
  }

  private FluentFuture configureReorderWikiSubpages() {
    if (
      isWikiableConfig(entityBundle.getEntity(), currentArea) &&
      entityBundle.getEntity() instanceof Project &&
      permissions.getCanEdit()
    ) {
      // shown if there's more than one page
      FluentFuture<List<V2WikiHeader>> future = getFuture(cb ->
        getSynapseJavascriptClient()
          .getV2WikiHeaderTree(
            entityBundle.getEntity().getId(),
            ObjectType.ENTITY.name(),
            cb
          )
      );
      future.addCallback(
        new FutureCallback<List<V2WikiHeader>>() {
          @Override
          public void onFailure(Throwable caught) {
            actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
          }

          @Override
          public void onSuccess(List<V2WikiHeader> wikiHeaders) {
            boolean isMoreThanOne = wikiHeaders.size() > 1;
            actionMenu.setActionVisible(
              Action.REORDER_WIKI_SUBPAGES,
              isMoreThanOne
            );
          }
        },
        directExecutor()
      );
      return future;
    } else {
      actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
    }
    return getDoneFuture(null);
  }

  private void configureCreateTableViewSnapshot() {
    if (
      entityBundle.getEntity() instanceof Table &&
      EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity())
    ) {
      if (entityBundle.getEntity() instanceof Dataset) {
        // "Stable Version" for datasets (SWC-5919)
        actionMenu.setActionText(
          Action.CREATE_TABLE_VERSION,
          "Create Stable Version"
        );
      } else {
        actionMenu.setActionText(
          Action.CREATE_TABLE_VERSION,
          "Create Snapshot"
        );
      }
      actionMenu.setActionVisible(
        Action.CREATE_TABLE_VERSION,
        permissions.getCanEdit()
      );
      actionMenu.setActionListener(Action.CREATE_TABLE_VERSION, this);
    } else {
      actionMenu.setActionVisible(Action.CREATE_TABLE_VERSION, false);
    }
  }

  private void configureAddWikiSubpage() {
    if (
      entityBundle.getEntity() instanceof Project &&
      isWikiableConfig(entityBundle.getEntity(), currentArea) &&
      wikiPageId != null
    ) {
      actionMenu.setActionVisible(
        Action.ADD_WIKI_SUBPAGE,
        permissions.getCanEdit()
      );
      actionMenu.setActionListener(Action.ADD_WIKI_SUBPAGE, this);
    } else {
      actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
    }
  }

  private void configureMove() {
    if (isMovableType(entityBundle.getEntity())) {
      actionMenu.setActionVisible(Action.MOVE_ENTITY, permissions.getCanMove());
      actionMenu.setActionText(
        Action.MOVE_ENTITY,
        MOVE_PREFIX + entityTypeDisplay
      );
      actionMenu.setActionListener(Action.MOVE_ENTITY, this);
    } else {
      actionMenu.setActionVisible(Action.MOVE_ENTITY, false);
    }
  }

  private void configureLink() {
    if (
      isLinkType(entityBundle.getEntity()) &&
      !isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)
    ) {
      actionMenu.setActionVisible(Action.CREATE_LINK, true);
      actionMenu.setActionListener(Action.CREATE_LINK, this);
      actionMenu.setActionText(
        Action.CREATE_LINK,
        "Save Link to " + entityTypeDisplay
      );
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
    boolean isVersionHistoryAvailable =
      EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity());
    actionMenu.setActionVisible(
      Action.SHOW_VERSION_HISTORY,
      isVersionHistoryAvailable
    );
  }

  private void configureSubmit() {
    if (isSubmittableType(entityBundle.getEntity())) {
      actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, true);
      actionMenu.setActionListener(Action.SUBMIT_TO_CHALLENGE, this);
      actionMenu.setActionText(
        Action.SUBMIT_TO_CHALLENGE,
        "Submit " + entityTypeDisplay + " to Challenge"
      );
    } else {
      actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, false);
    }
  }

  private void configureEditProjectMetadataAction() {
    if (entityBundle.getEntity() instanceof Project && currentArea == null) {
      actionMenu.setActionVisible(
        Action.EDIT_PROJECT_METADATA,
        permissions.getCanEdit()
      );
      actionMenu.setActionListener(Action.EDIT_PROJECT_METADATA, this);
    } else {
      actionMenu.setActionVisible(Action.EDIT_PROJECT_METADATA, false);
    }
  }

  private void configureProjectHelpAction() {
    boolean isProject = entityBundle.getEntity() instanceof Project;
    actionMenu.setActionVisible(Action.PROJECT_HELP, isProject);
    actionMenu.setActionHref(
      Action.PROJECT_HELP,
      "https://sagebionetworks.jira.com/servicedesk/customer/portal/9"
    );
  }

  private void configureEditFileMetadataAction() {
    if (entityBundle.getEntity() instanceof FileEntity) {
      actionMenu.setActionVisible(
        Action.EDIT_FILE_METADATA,
        permissions.getCanEdit()
      );
      actionMenu.setActionListener(Action.EDIT_FILE_METADATA, this);
    } else {
      actionMenu.setActionVisible(Action.EDIT_FILE_METADATA, false);
    }
  }

  private void configureRenameAction() {
    if (
      !hasCustomRenameEditor(entityBundle.getEntity()) &&
      !(entityBundle.getEntity() instanceof DockerRepository)
    ) {
      actionMenu.setActionVisible(
        Action.CHANGE_ENTITY_NAME,
        permissions.getCanEdit()
      );
      String text = RENAME_PREFIX + entityTypeDisplay;
      if (
        entityBundle.getEntity() instanceof Table &&
        DisplayUtils.isInTestWebsite(cookies)
      ) {
        text = EDIT_NAME_AND_DESCRIPTION;
      }
      actionMenu.setActionText(Action.CHANGE_ENTITY_NAME, text);
      actionMenu.setActionListener(Action.CHANGE_ENTITY_NAME, this);
    } else {
      actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, false);
    }
  }

  private void configureActionMenuLayout() {
    if (isTopLevelProjectToolsMenu(entity, currentArea)) {
      // use Area layout
      actionMenu.setLayout(
        DefaultEntityActionMenuLayoutUtil.getLayout(currentArea)
      );
    } else {
      // use Entity type layout
      actionMenu.setLayout(
        DefaultEntityActionMenuLayoutUtil.getLayout(
          EntityTypeUtils.getEntityType(entity)
        )
      );
    }
  }

  private void configureDeleteAction() {
    if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
      actionMenu.setActionVisible(Action.DELETE_ENTITY, false);
    } else {
      actionMenu.setActionVisible(
        Action.DELETE_ENTITY,
        permissions.getCanDelete()
      );
      actionMenu.setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX + entityTypeDisplay
      );
      actionMenu.setActionListener(Action.DELETE_ENTITY, this);
    }
  }

  private void configureDeleteWikiAction() {
    if (
      isWikiableConfig(entityBundle.getEntity(), currentArea) &&
      entityBundle.getEntity() instanceof Project
    ) {
      actionMenu.setActionVisible(
        Action.DELETE_WIKI_PAGE,
        permissions.getCanDelete()
      );
      actionMenu.setActionListener(Action.DELETE_WIKI_PAGE, this);
    } else {
      actionMenu.setActionVisible(Action.DELETE_WIKI_PAGE, false);
    }
  }

  private void configureShareAction() {
    if (isTopLevelProjectToolsMenu(entityBundle.getEntity(), currentArea)) {
      actionMenu.setActionVisible(Action.VIEW_SHARING_SETTINGS, false);
    } else {
      actionMenu.setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
      actionMenu.setActionListener(Action.VIEW_SHARING_SETTINGS, this);
      actionMenu.setActionText(
        Action.VIEW_SHARING_SETTINGS,
        entityTypeDisplay + " Sharing Settings"
      );
    }
  }

  public static boolean isVersionSupported(Entity entity) {
    // The following table types inherit Versionable, but do not actually support snapshot versioning
    boolean isMaterializedView = entity instanceof MaterializedView;
    boolean isVirtualTable = entity instanceof VirtualTable;
    boolean isSubmissionView = entity instanceof SubmissionView;
    boolean isNonSnapshottableTable =
      isMaterializedView || isVirtualTable || isSubmissionView;
    return entity instanceof Versionable && !isNonSnapshottableTable;
  }

  public static boolean isDefinedByScope(Entity entity) {
    return entity instanceof EntityView || entity instanceof SubmissionView;
  }

  public static boolean isDefinedBySql(Entity entity) {
    return entity instanceof HasDefiningSql;
  }

  public static boolean isEditCellValuesSupported(Entity entity) {
    // EntityRefCollectionView results are not editable, even if the user has permissions, because rows may reference immutable versions (SWC-5870, SWC-5903)
    return QueryResultEditorWidget.isTableTypeQueryResultEditable(
      entity.getClass()
    );
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
    return (
      entity instanceof Folder ||
      (entity instanceof Project && EntityArea.FILES.equals(area))
    );
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
  public void onAction(Action action, ReactMouseEvent event) {
    switch (action) {
      case DELETE_ENTITY:
        onDeleteEntity();
        break;
      case VIEW_SHARING_SETTINGS:
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
      case VIEW_DEFINING_SQL:
        onViewDefiningSql();
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
      case ADD_DATASET:
        onAddDataset();
        break;
      case ADD_DATASET_COLLECTION:
        onAddDatasetCollection();
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
    Table tableEntity = (Table) entityBundle.getEntity();
    boolean newIsSearchEnabledValue = tableEntity.getIsSearchEnabled() == null
      ? true
      : !tableEntity.getIsSearchEnabled();
    tableEntity.setIsSearchEnabled(newIsSearchEnabledValue);
    getSynapseJavascriptClient()
      .updateEntity(
        tableEntity,
        null,
        null,
        new AsyncCallback<Entity>() {
          @Override
          public void onFailure(Throwable caught) {
            // undo change, as we were unable to update the entity
            tableEntity.setIsSearchEnabled(!newIsSearchEnabledValue);
            view.showErrorMessage(caught.getMessage());
          }

          @Override
          public void onSuccess(Entity result) {
            fireEntityUpdatedEvent();
            String successTextPrefix = newIsSearchEnabledValue
              ? "Enabled"
              : "Disabled";
            view.showSuccess(
              successTextPrefix +
              " full text search for this " +
              entityTypeDisplay
            );
          }
        }
      );
  }

  public void onCreateExternalDockerRepo() {
    // This operation creates an entity and uploads data to the entity so both checks must pass.
    preflightController.checkCreateEntity(
      entityBundle,
      DockerRepository.class.getName(),
      new Callback() {
        @Override
        public void invoke() {
          postCheckCreateExternalDockerRepo();
        }
      }
    );
  }

  public void onShowProjectStats() {
    checkUpdateEntity(() -> {
      getStatisticsPlotWidget()
        .configureAndShow(entityBundle.getEntity().getId());
    });
  }

  private void postCheckCreateExternalDockerRepo() {
    getAddExternalRepoModal()
      .configuration(
        entityBundle.getEntity().getId(),
        () -> {
          fireEntityUpdatedEvent();
        }
      );
    getAddExternalRepoModal().show();
  }

  public void onUploadTable() {
    // This operation creates an entity and uploads data to the entity so both checks must pass.
    preflightController.checkCreateEntityAndUpload(
      entityBundle,
      TableEntity.class.getName(),
      () -> {
        postCheckUploadTable();
      }
    );
  }

  private void postCheckUploadTable() {
    getUploadTableModalWidget()
      .configure(entityBundle.getEntity().getId(), null);
    getUploadTableModalWidget().showModal(entityUpdatedWizardCallback);
  }

  public void onAddDataset() {
    preflightController.checkCreateEntity(
      entityBundle,
      Dataset.class.getName(),
      () -> {
        postCheckCreateDatasetOrCollection(TableType.dataset);
      }
    );
  }

  public void onAddDatasetCollection() {
    preflightController.checkCreateEntity(
      entityBundle,
      Dataset.class.getName(),
      () -> {
        postCheckCreateDatasetOrCollection(TableType.dataset_collection);
      }
    );
  }

  public void onAddTable() {
    preflightController.checkCreateEntity(
      entityBundle,
      TableEntity.class.getName(),
      () -> {
        postCheckCreateTableOrView(TableType.table);
      }
    );
  }

  private void postCheckCreateDatasetOrCollection(TableType type) {
    CreateDatasetOrCollection dialog = getCreateDatasetOrCollection();
    dialog.configure(entityBundle.getEntity().getId(), type);
  }

  private void postCheckCreateTableOrView(TableType table) {
    CreateTableViewWizard wizard = getCreateTableViewWizard();
    wizard.configure(
      entityBundle.getEntity().getId(),
      newId -> {
        wizard.setOpen(false);
        getGlobalApplicationState().getPlaceChanger().goTo(new Synapse(newId));
      },
      () -> {
        wizard.setOpen(false);
      }
    );
    wizard.setOpen(true);
  }

  private void onUploadNewFileEntity() {
    checkUploadEntity(() -> {
      UploadDialogWidget uploader = getNewUploadDialogWidget();
      uploader.configure(
        DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK,
        null,
        entityBundle.getEntity().getId(),
        null,
        true
      );
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
    AccessRequirementsPlace place = new AccessRequirementsPlace(
      AccessRequirementsPlace.ID_PARAM +
      "=" +
      entity.getId() +
      "&" +
      AccessRequirementsPlace.TYPE_PARAM +
      "=" +
      RestrictableObjectType.ENTITY.toString()
    );
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
      view.showErrorMessage(
        "Can only edit the provenance of the most recent version."
      );
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
    uploadDialogWidget.configure(
      DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK,
      entityBundle.getEntity(),
      null,
      null,
      true
    );
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
    getEvaluationSubmitter()
      .configure(this.entityBundle.getEntity(), null, null);
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
      .setVersionSelection(EntityFinderWidget.VersionSelection.DISALLOWED)
      .setSelectedHandler((selected, entityFinder) -> {
        createLink(selected.getTargetId(), entityFinder);
      })
      .setTreeOnly(true)
      .setModalTitle("Create Link to " + entityTypeDisplay)
      .setHelpMarkdown(
        "Search or Browse to find a Project or Folder that you have access to, and place a symbolic link for easy access"
      )
      .setPromptCopy(
        "Find a destination and place a link to <b>" +
        SafeHtmlUtils.fromString(entity.getName()).asString() +
        "</b> (" +
        entity.getId() +
        ")"
      )
      .setSelectedCopy(count -> "Destination")
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
      targetVersionNumber =
        ((Versionable) entityBundle.getEntity()).getVersionNumber();
    }
    ref.setTargetVersionNumber(targetVersionNumber);
    link.setLinksTo(ref); // links to this entity
    link.setLinksToClassName(entityBundle.getEntity().getClass().getName());
    link.setName(entityBundle.getEntity().getName()); // copy name of this entity as default
    getSynapseJavascriptClient()
      .createEntity(
        link,
        new AsyncCallback<Entity>() {
          @Override
          public void onSuccess(Entity result) {
            Integer autoCloseInMs = null;
            String primaryButtonText = DisplayConstants.VIEW_LINK_CONTAINER;
            AlertButtonConfig.Callback onPrimaryButtonClick = () -> {
              Long version = null;
              String areaToken = "";
              getGlobalApplicationState()
                .getPlaceChanger()
                .goTo(
                  new Synapse(target, version, EntityArea.FILES, areaToken)
                );
            };
            String title = "";
            ToastMessageOptions options = ToastMessageOptions.create(
              title,
              autoCloseInMs,
              primaryButtonText,
              onPrimaryButtonClick
            );
            popupUtils.notify(
              DisplayConstants.TEXT_LINK_SAVED,
              NotificationVariant.SUCCESS,
              options
            );
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
        }
      );
  }

  private void onMove() {
    // Validate the user can update this entity.
    preflightController.checkUpdateEntity(
      this.entityBundle,
      new Callback() {
        @Override
        public void invoke() {
          postCheckMove();
        }
      }
    );
  }

  private void postCheckMove() {
    EntityFinderWidget.Builder builder = getEntityFinderBuilder()
      .setModalTitle("Move " + entityTypeDisplay)
      .setHelpMarkdown(
        "Search or Browse Synapse to find a destination to move this " +
        entityTypeDisplay
      )
      .setPromptCopy(
        "Find a destination to move <b>" +
        SafeHtmlUtils.fromString(entity.getName()).asString() +
        "</b> (" +
        entity.getId() +
        ")"
      )
      .setSelectedCopy(count -> "Destination")
      .setConfirmButtonCopy("Move")
      .setVersionSelection(EntityFinderWidget.VersionSelection.DISALLOWED)
      .setTreeOnly(true)
      .setSelectedHandler((selected, finder) -> {
        String entityId = entityBundle.getEntity().getId();
        getSynapseClient()
          .moveEntity(
            entityId,
            selected.getTargetId(),
            new AsyncCallback<Entity>() {
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
            }
          );
      });

    if (entityBundle.getEntity() instanceof Table) {
      builder
        .setInitialScope(EntityFinderScope.ALL_PROJECTS)
        .setInitialContainer(EntityFinderWidget.InitialContainer.NONE)
        .setVisibleTypesInTree(PROJECT)
        .setSelectableTypes(PROJECT);
    } else {
      builder
        .setInitialScope(EntityFinderScope.CURRENT_PROJECT)
        .setInitialContainer(EntityFinderWidget.InitialContainer.PARENT)
        .setVisibleTypesInTree(CONTAINER)
        .setSelectableTypes(CONTAINER);
    }

    builder.build().show();
  }

  private void onViewWikiSource() {
    WikiPageKey key = new WikiPageKey(
      this.entityBundle.getEntity().getId(),
      ObjectType.ENTITY.name(),
      wikiPageId
    );
    getSynapseJavascriptClient()
      .getV2WikiPageAsV1(
        key,
        new AsyncCallback<WikiPage>() {
          @Override
          public void onSuccess(WikiPage page) {
            view.showInfoDialog("Wiki Source", page.getMarkdown());
          }

          @Override
          public void onFailure(Throwable caught) {
            view.showErrorMessage(caught.getMessage());
          }
        }
      );
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
    WikiPageKey key = new WikiPageKey(
      this.entityBundle.getEntity().getId(),
      ObjectType.ENTITY.name(),
      wikiPageId
    );
    getWikiMarkdownEditor()
      .configure(
        key,
        wikiPage -> {
          fireEntityUpdatedEvent();
        }
      );
  }

  private void onCreateTableViewSnapshot() {
    if (isCurrentVersion) {
      checkUpdateEntity(() -> {
        postCheckCreateTableViewSnapshot();
      });
    } else {
      view.showErrorMessage(
        "Can only create a new version from the current table, not an old version."
      );
    }
  }

  private void onEditDefiningSql() {
    if (isCurrentVersion) {
      checkUpdateEntity(this::postCheckEditDefiningSql);
    }
  }

  private PromptCallback getUpdateDefiningSqlCallback() {
    return value -> {
      ((HasDefiningSql) entity).setDefiningSQL(value);
      getSynapseJavascriptClient()
        .updateEntity(
          entity,
          null,
          false,
          new AsyncCallback<Entity>() {
            @Override
            public void onSuccess(Entity result) {
              fireEntityUpdatedEvent();
              view.showSuccess(
                "Updated the Synapse SQL query that defines this " +
                EntityTypeUtils.getFriendlyEntityTypeName(result) +
                "."
              );
            }

            @Override
            public void onFailure(Throwable caught) {
              view.showErrorMessage(caught.getMessage());
            }
          }
        );
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
        getSynapseJavascriptClient()
          .createSnapshot(
            entityId,
            comment,
            label,
            activityId,
            new AsyncCallback<SnapshotResponse>() {
              @Override
              public void onSuccess(SnapshotResponse result) {
                view.hideCreateVersionDialog();
                fireEntityUpdatedEvent();

                Synapse newVersionPlace = new Synapse(
                  entity.getId(),
                  result.getSnapshotVersionNumber(),
                  EntityArea.TABLES,
                  null
                );
                ToastMessageOptions.Builder messageBuilder =
                  new ToastMessageOptions.Builder();
                messageBuilder.setTitle(SNAPSHOT_CREATED);
                popupUtils.notify(
                  SNAPSHOT_CREATED_DETAILS_TABLE,
                  DisplayUtils.NotificationVariant.SUCCESS,
                  messageBuilder.build()
                );
                getGlobalApplicationState()
                  .getPlaceChanger()
                  .goTo(newVersionPlace);
              }

              @Override
              public void onFailure(Throwable caught) {
                view.hideCreateVersionDialog();
                view.showErrorMessage(caught.getMessage());
              }
            }
          );
      };
    } else if (
      entity instanceof EntityView || entity instanceof EntityRefCollectionView
    ) {
      return values -> {
        String entityId = entityBundle.getEntity().getId();
        String label = values.get(0);
        String comment = values.get(1);
        // create the version via an update table transaction
        // Start the job.
        TableUpdateTransactionRequest transactionRequest =
          new TableUpdateTransactionRequest();
        transactionRequest.setEntityId(entityId);
        SnapshotRequest snapshotRequest = new SnapshotRequest();
        snapshotRequest.setSnapshotLabel(label);
        snapshotRequest.setSnapshotComment(comment);
        transactionRequest.setSnapshotOptions(snapshotRequest);
        transactionRequest.setChanges(new ArrayList<>());
        transactionRequest.setCreateSnapshot(true);
        view.hideMultiplePromptDialog();
        view.showCreateVersionDialog();
        String message = entity instanceof EntityView
          ? CREATING_A_NEW_VIEW_VERSION_MESSAGE
          : CREATING_A_NEW_DATASET_VERSION_MESSAGE;
        getJobTrackingWidget()
          .startAndTrackJob(
            message,
            false,
            AsynchType.TableTransaction,
            transactionRequest,
            new AsynchronousProgressHandler<TableUpdateTransactionResponse>() {
              @Override
              public void onFailure(Throwable failure) {
                view.hideCreateVersionDialog();
                view.showErrorMessage(failure.getMessage());
              }

              @Override
              public void onComplete(TableUpdateTransactionResponse response) {
                view.hideCreateVersionDialog();
                String errors =
                  QueryResultEditorWidget.getEntityUpdateResultsFailures(
                    response
                  );
                if (!errors.isEmpty()) {
                  view.showErrorMessage(errors);
                } else {
                  fireEntityUpdatedEvent();
                  ToastMessageOptions.Builder messageBuilder =
                    new ToastMessageOptions.Builder();
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

                  popupUtils.notify(
                    toastMsg,
                    DisplayUtils.NotificationVariant.SUCCESS,
                    messageBuilder.build()
                  );
                  getGlobalApplicationState()
                    .getPlaceChanger()
                    .goTo(
                      new Synapse(
                        entity.getId(),
                        newVersionNumber,
                        newVersionArea,
                        null
                      )
                    );
                }
              }

              @Override
              public void onCancel() {
                view.hideCreateVersionDialog();
              }
            }
          );
      };
    } else {
      throw new IllegalArgumentException(
        "A snapshot cannot be made of an entity with type: " +
        entity.getClass().getName()
      );
    }
  }

  private String getCreateSnapshotTitleCopy(Entity entity) {
    if (
      entity instanceof TableEntity ||
      entity instanceof EntityView ||
      entity instanceof DatasetCollection
    ) {
      return CREATE_SNAPSHOT;
    } else if (entity instanceof Dataset) {
      return CREATE_STABLE_VERSION;
    } else {
      throw new IllegalArgumentException(
        "A snapshot cannot be made of an entity with type: " +
        entity.getClass().getName()
      );
    }
  }

  private String getCreateSnapshotBodyCopy(Entity entity) {
    if (entity instanceof TableEntity) {
      return CREATE_NEW_TABLE_ENTITY_VERSION_PROMPT_BODY;
    } else if (entity instanceof EntityView) {
      return CREATE_NEW_VIEW_VERSION_PROMPT_BODY;
    } else if (entity instanceof DatasetCollection) {
      return CREATE_NEW_DATASET_COLLECTION_VERSION_PROMPT_BODY;
    } else if (entity instanceof Dataset) {
      return CREATE_NEW_DATASET_VERSION_PROMPT_BODY;
    } else {
      throw new IllegalArgumentException(
        "A snapshot cannot be made of an entity with type: " +
        entity.getClass().getName()
      );
    }
  }

  private void postCheckCreateTableViewSnapshot() {
    // prompt for new version label and comment
    List<String> prompts = new ArrayList<>();
    prompts.add("Label");
    prompts.add("Comment");
    PromptForValuesModalView.Configuration.Builder configBuilder =
      getPromptForValuesModalConfigBuilder();
    configBuilder
      .setTitle(getCreateSnapshotTitleCopy(entity))
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
      view.showPromptDialog(
        DisplayConstants.ENTER_PAGE_TITLE,
        "",
        new PromptCallback() {
          @Override
          public void callback(String result) {
            createWikiPage(result);
          }
        },
        PromptForValuesModalView.InputType.TEXTBOX
      );
    }
  }

  public void createWikiPage(final String name) {
    if (DisplayUtils.isDefined(name)) {
      WikiPage page = new WikiPage();
      page.setParentWikiId(wikiPageId);
      page.setTitle(name);
      getSynapseClient()
        .createV2WikiPageWithV1(
          entityBundle.getEntity().getId(),
          ObjectType.ENTITY.name(),
          page,
          new AsyncCallback<WikiPage>() {
            @Override
            public void onSuccess(WikiPage result) {
              view.showSuccess("'" + name + "' Page Added");
              Synapse newPlace = new Synapse(
                entityBundle.getEntity().getId(),
                getVersionIfNotLatest().orElse(null),
                EntityArea.WIKI,
                result.getId()
              );
              getGlobalApplicationState().getPlaceChanger().goTo(newPlace);
            }

            @Override
            public void onFailure(Throwable caught) {
              view.showErrorMessage(
                DisplayConstants.ERROR_PAGE_CREATION_FAILED +
                ": " +
                caught.getMessage()
              );
            }
          }
        );
    }
  }

  private void onRename() {
    if (checkIsLatestVersion()) {
      checkUpdateEntity(() -> {
        postCheckRename();
      });
    } else {
      if (entityBundle.getEntityType() == dataset) {
        view.showErrorMessage("Can only change the name of the draft dataset.");
      } else {
        view.showErrorMessage(
          "Can only change the name of the most recent " +
          entityBundle.getEntityType() +
          " version."
        );
      }
    }
  }

  private boolean checkIsLatestVersion() {
    // If the entity is not a versionable entity, it should be considered as the LatestVersion
    if (!(entityBundle.getEntity() instanceof VersionableEntity)) {
      return true;
    } else {
      if (((VersionableEntity) entityBundle.getEntity()).getIsLatestVersion()) {
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Called if the preflight check for a rename passes.
   */
  private void postCheckRename() {
    getRenameEntityModalWidget()
      .onRename(
        this.entity,
        () -> {
          fireEntityUpdatedEvent();
        }
      );
  }

  private void onEditFileMetadata() {
    // Can only edit file metadata of the current file version
    if (isCurrentVersion) {
      // Validate the user can update this entity.
      checkUpdateEntity(() -> {
        postCheckEditFileMetadata();
      });
    } else {
      view.showErrorMessage(
        "Can only edit the metadata of the most recent file version."
      );
    }
  }

  /**
   * Called if the preflight check for edit file metadata passes.
   */
  private void postCheckEditFileMetadata() {
    FileHandle originalFileHandle = DisplayUtils.getFileHandle(entityBundle);
    getEditFileMetadataModalWidget()
      .configure(
        (FileEntity) entityBundle.getEntity(),
        originalFileHandle,
        () -> {
          fireEntityUpdatedEvent();
        }
      );
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
    getEditProjectMetadataModalWidget()
      .configure(
        (Project) entityBundle.getEntity(),
        canChangeSettings,
        () -> {
          fireEntityUpdatedEvent();
        }
      );
  }

  private void postCheckEditDefiningSql() {
    String entityId = entity.getId();
    getSqlDefinedEditorModalWidget()
      .configure(
        entityId,
        () -> {
          getSqlDefinedEditorModalWidget().setOpen(false);
          eventBus.fireEvent(new EntityUpdatedEvent(entity.getId()));
        },
        () -> {
          getSqlDefinedEditorModalWidget().setOpen(false);
        }
      );
    getSqlDefinedEditorModalWidget().setOpen(true);
  }

  private void onViewDefiningSql() {
    view.showInfoDialog(
      "Defining SQL",
      ((HasDefiningSql) entity).getDefiningSQL()
    );
  }

  public void onDeleteWiki() {
    final WikiPageKey key = new WikiPageKey(
      this.entityBundle.getEntity().getId(),
      ObjectType.ENTITY.name(),
      wikiPageId
    );
    getWikiPageDeleteConfirmationDialog()
      .show(
        key,
        parentWikiId -> {
          getGlobalApplicationState()
            .getPlaceChanger()
            .goTo(
              new Synapse(
                entityBundle.getEntity().getId(),
                null,
                EntityArea.WIKI,
                parentWikiId
              )
            );
        }
      );
  }

  @Override
  public void onDeleteEntity() {
    // Confirm the delete with the user. Mention that everything inside folder will also be deleted if
    // this is a folder entity.
    String display =
      ARE_YOU_SURE_YOU_WANT_TO_DELETE +
      this.entityTypeDisplay +
      " \"" +
      this.entity.getName() +
      "\"?";
    if (this.entity instanceof Folder) {
      display += DELETE_FOLDER_EXPLANATION;
    }

    view.showConfirmDeleteDialog(
      display,
      new Callback() {
        @Override
        public void invoke() {
          postConfirmedDeleteEntity();
        }
      }
    );
  }

  /**
   * Called after the user has confirmed the delete of the entity.
   */
  public void postConfirmedDeleteEntity() {
    // The user has confirmed the delete, the next step is the preflight check.
    preflightController.checkDeleteEntity(
      this.entityBundle,
      new Callback() {
        @Override
        public void invoke() {
          postCheckDeleteEntity();
        }
      }
    );
  }

  /**
   * After all checks have been made we can do the actual entity delete.
   */
  public void postCheckDeleteEntity() {
    final String entityId = this.entityBundle.getEntity().getId();
    getSynapseJavascriptClient()
      .deleteEntityById(
        entityId,
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            view.showInfo(THE + entityTypeDisplay + WAS_SUCCESSFULLY_DELETED);
            // Go to entity's parent
            Place gotoPlace = createDeletePlace();
            getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
            KeyFactory keyFactory = keyFactoryProvider.getKeyFactory(
              authenticationController.getCurrentUserAccessToken()
            );
            queryClient.invalidateQueries(
              InvalidateQueryFilters.create(
                keyFactory.getTrashCanItemsQueryKey()
              )
            );
          }

          @Override
          public void onFailure(Throwable caught) {
            view.showErrorMessage(
              DisplayConstants.ERROR_ENTITY_DELETE_FAILURE + caught.getMessage()
            );
          }
        }
      );
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
      if (
        entityBundle.getEntity() instanceof EntityRefCollectionView
      ) gotoPlace =
        new Synapse(parentId, null, EntityArea.DATASETS, null); else if (
        entityBundle.getEntity() instanceof Table
      ) gotoPlace =
        new Synapse(parentId, null, EntityArea.TABLES, null); else if (
        entityBundle.getEntity() instanceof DockerRepository
      ) gotoPlace =
        new Synapse(parentId, null, EntityArea.DOCKER, null); else if (
        entityBundle.getEntity() instanceof FileEntity ||
        entityBundle.getEntity() instanceof Folder
      ) gotoPlace =
        new Synapse(parentId, null, EntityArea.FILES, null); else gotoPlace =
        new Synapse(parentId);
    } else {
      gotoPlace =
        new Profile(
          authenticationController.getCurrentUserPrincipalId(),
          ProfileArea.PROJECTS
        );
    }
    return gotoPlace;
  }

  @Override
  public void onShare() {
    getAccessControlListModalWidget()
      .configure(entity, permissions.getCanChangePermissions());
    this.getAccessControlListModalWidget()
      .showSharing(() -> {
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

  private String getTooltipTextForRequiredActionForDownload(
    Class<? extends org.sagebionetworks.repo.model.download.Action> clazz
  ) {
    if (RequestDownload.class.equals(clazz)) {
      return REQUEST_DOWNLOAD_GUIDANCE;
    } else if (MeetAccessRequirement.class.equals(clazz)) {
      return ACCESS_REQUIREMENT_GUIDANCE;
    } else if (EnableTwoFa.class.equals(clazz)) {
      return ENABLE_2FA_GUIDANCE;
    }
    return "";
  }
}
