package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.DisplayConstants.FILE_VIEW;
import static org.sagebionetworks.web.client.DisplayConstants.MATERIALIZED_VIEW;
import static org.sagebionetworks.web.client.DisplayConstants.VIEW_DOWNLOAD_LIST;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.ARE_YOU_SURE_YOU_WANT_TO_DELETE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETE_FOLDER_EXPLANATION;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETE_PREFIX;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.EDIT_NAME_AND_DESCRIPTION;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.EDIT_WIKI_PREFIX;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.MOVE_PREFIX;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.THE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.UPDATE_DOI_FOR;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WAS_SUCCESSFULLY_DELETED;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WIKI;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_COLLECTOR_URL;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_1;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_PRIORITY;
import static org.sagebionetworks.web.shared.WebConstants.REVIEW_DATA_REQUEST_COMPONENT_ID;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.client.exceptions.SynapseClientException;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.repo.model.download.AddBatchOfFilesToDownloadListResponse;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetCollection;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.SnapshotResponse;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionResponse;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.NotificationVariant;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.ToastMessageOptions;
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
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerView;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidget;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadHandlerWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.ActionListener;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.MaterializedViewEditor;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.FormParams;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.CallbackMockStubber;
import org.sagebionetworks.web.test.helper.SelfReturningAnswer;

@RunWith(MockitoJUnitRunner.class)
public class EntityActionControllerImplTest {

  @Mock
  EntityActionControllerView mockView;

  @Mock
  PreflightController mockPreflightController;

  @Mock
  SynapseClientAsync mockSynapseClient;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  AuthenticationController mockAuthenticationController;

  @Mock
  AccessControlListModalWidget mockAccessControlListModalWidget;

  @Mock
  RenameEntityModalWidget mockRenameEntityModalWidget;

  @Mock
  EditFileMetadataModalWidget mockEditFileMetadataModalWidget;

  @Mock
  EditProjectMetadataModalWidget mockEditProjectMetadataModalWidget;

  @Mock
  MaterializedViewEditor mockMaterializedViewEditor;

  EntityFinderWidget.Builder mockEntityFinderBuilder;

  @Mock
  EntityFinderWidget mockEntityFinder;

  @Mock
  EvaluationSubmitter mockSubmitter;

  @Mock
  UploadDialogWidget mockUploader;

  @Mock
  EntityActionMenu mockActionMenu;

  @Mock
  EventBus mockEventBus;

  @Mock
  AsynchronousProgressWidget mockJobTrackingWidget;

  EntityBundle entityBundle;
  UserEntityPermissions permissions;

  EntityActionControllerImpl controller;
  String parentId;
  String entityId;
  String entityName = "my entity";
  String currentUserId = "12344321";
  String wikiPageId = "999";

  @Mock
  WikiMarkdownEditor mockMarkdownEditorWidget;

  @Mock
  ProvenanceEditorWidget mockProvenanceEditorWidget;

  @Mock
  StorageLocationWidget mockStorageLocationWidget;

  Reference selected;

  @Mock
  EvaluationEditorModal mockEvalEditor;

  @Mock
  CookieProvider mockCookies;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Mock
  ChallengeClientAsync mockChallengeClient;

  @Mock
  SelectTeamModal mockSelectTeamModal;

  @Mock
  ApproveUserAccessModal mockApproveUserAccessModal;

  @Mock
  UserProfileClientAsync mockUserProfileClient;

  @Captor
  ArgumentCaptor<AsyncCallback<UserBundle>> userBundleCaptor;

  @Captor
  ArgumentCaptor<ActionListener> actionListenerCaptor;

  @Captor
  ArgumentCaptor<AsyncCallback<SnapshotResponse>> tableSnapshotResponseCaptor;

  @Mock
  UserBundle mockUserBundle;

  @Mock
  Throwable mockThrowable;

  @Mock
  PortalGinInjector mockPortalGinInjector;

  @Mock
  IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;

  @Mock
  PublicPrincipalIds mockPublicPrincipalIds;

  @Mock
  AccessControlList mockACL;

  @Mock
  SynapseJavascriptClient mockSynapseJavascriptClient;

  @Mock
  AddFolderDialogWidget mockAddFolderDialogWidget;

  @Mock
  CreateTableViewWizard mockCreateTableViewWizard;

  @Mock
  UploadTableModalWidget mockUploadTableModalWidget;

  @Mock
  AddExternalRepoModal mockAddExternalRepoModal;

  @Mock
  GWTWrapper mockGWT;

  @Mock
  SynapseProperties mockSynapseProperties;

  @Captor
  ArgumentCaptor<EntityFinderWidget.SelectedHandler<Reference>> entityFinderSelectedHandlerCaptor;

  @Captor
  ArgumentCaptor<CallbackP<List<String>>> callbackListStringCaptor;

  @Captor
  ArgumentCaptor<TableUpdateTransactionRequest> tableUpdateTransactionRequestCaptor;

  @Captor
  ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;

  @Captor
  ArgumentCaptor<AsyncCallback<AddBatchOfFilesToDownloadListResponse>> addToDownloadListAsyncCallbackCaptor;

  @Mock
  TableUpdateTransactionResponse mockTableUpdateTransactionResponse;

  @Mock
  SnapshotResponse mockSnapshotResponse;

  @Mock
  PromptForValuesModalView.Configuration mockPromptModalConfiguration;

  @Mock
  EntityView mockEntityView;

  @Mock
  MaterializedView mockMaterializedView;

  @Mock
  AddToDownloadListV2 mockAddToDownloadListWidget;

  @Mock
  RestrictionInformationResponse mockRestrictionInformation;

  @Mock
  FileDownloadHandlerWidget mockFileDownloadHandlerWidget;

  @Mock
  SynapseJSNIUtilsImpl mockJsniUtils;

  @Mock
  FileClientsHelp mockFileClientsHelp;

  @Mock
  ContainerClientsHelp mockContainerClientsHelp;

  PromptForValuesModalView.Configuration.Builder mockPromptModalConfigurationBuilder;
  Set<ResourceAccess> resourceAccessSet;

  public static final String SELECTED_TEAM_ID = "987654";
  public static final long PUBLIC_USER_ID = 77772L;

  EntityArea currentEntityArea;

  @Before
  public void before() {
    mockEntityFinderBuilder =
      mock(EntityFinderWidget.Builder.class, new SelfReturningAnswer());
    mockPromptModalConfigurationBuilder =
      mock(
        PromptForValuesModalView.Configuration.Builder.class,
        new SelfReturningAnswer()
      );

    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockAuthenticationController.getCurrentUserPrincipalId())
      .thenReturn(currentUserId);
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);

    when(mockPortalGinInjector.getSynapseProperties())
      .thenReturn(mockSynapseProperties);
    when(mockPortalGinInjector.getAccessControlListModalWidget())
      .thenReturn(mockAccessControlListModalWidget);
    when(mockPortalGinInjector.getRenameEntityModalWidget())
      .thenReturn(mockRenameEntityModalWidget);
    when(mockPortalGinInjector.getEditFileMetadataModalWidget())
      .thenReturn(mockEditFileMetadataModalWidget);
    when(mockPortalGinInjector.getEditProjectMetadataModalWidget())
      .thenReturn(mockEditProjectMetadataModalWidget);
    when(mockPortalGinInjector.getEntityFinderBuilder())
      .thenReturn(mockEntityFinderBuilder);
    when(mockPortalGinInjector.getMaterializedViewEditor())
      .thenReturn(mockMaterializedViewEditor);
    when(mockEntityFinderBuilder.build()).thenReturn(mockEntityFinder);

    when(mockPortalGinInjector.getUploadDialogWidget())
      .thenReturn(mockUploader);
    when(mockPortalGinInjector.getWikiMarkdownEditor())
      .thenReturn(mockMarkdownEditorWidget);
    when(mockPortalGinInjector.getProvenanceEditorWidget())
      .thenReturn(mockProvenanceEditorWidget);
    when(mockPortalGinInjector.getStorageLocationWidget())
      .thenReturn(mockStorageLocationWidget);
    when(mockPortalGinInjector.getEvaluationEditorModal())
      .thenReturn(mockEvalEditor);
    when(mockPortalGinInjector.getSelectTeamModal())
      .thenReturn(mockSelectTeamModal);
    when(mockPortalGinInjector.getApproveUserAccessModal())
      .thenReturn(mockApproveUserAccessModal);
    when(mockPortalGinInjector.getChallengeClientAsync())
      .thenReturn(mockChallengeClient);
    when(mockPortalGinInjector.getSynapseClientAsync())
      .thenReturn(mockSynapseClient);
    when(mockPortalGinInjector.getGlobalApplicationState())
      .thenReturn(mockGlobalApplicationState);
    when(mockPortalGinInjector.getEvaluationSubmitter())
      .thenReturn(mockSubmitter);
    when(mockSynapseProperties.getPublicPrincipalIds())
      .thenReturn(mockPublicPrincipalIds);
    when(mockPortalGinInjector.getSynapseJavascriptClient())
      .thenReturn(mockSynapseJavascriptClient);
    when(mockPortalGinInjector.getSynapseJSNIUtils()).thenReturn(mockJsniUtils);
    when(mockPortalGinInjector.getCreateTableViewWizard())
      .thenReturn(mockCreateTableViewWizard);
    when(mockPortalGinInjector.getUploadTableModalWidget())
      .thenReturn(mockUploadTableModalWidget);
    when(mockPortalGinInjector.getAddExternalRepoModal())
      .thenReturn(mockAddExternalRepoModal);
    when(mockPortalGinInjector.getAddFolderDialogWidget())
      .thenReturn(mockAddFolderDialogWidget);
    when(mockPortalGinInjector.creatNewAsynchronousProgressWidget())
      .thenReturn(mockJobTrackingWidget);
    when(mockPortalGinInjector.getPromptForValuesModalConfigurationBuilder())
      .thenReturn(mockPromptModalConfigurationBuilder);
    when(mockPortalGinInjector.getFileDownloadHandlerWidget())
      .thenReturn(mockFileDownloadHandlerWidget);
    when(mockPortalGinInjector.getFileClientsHelp())
      .thenReturn(mockFileClientsHelp);
    when(mockPortalGinInjector.getContainerClientsHelp())
      .thenReturn(mockContainerClientsHelp);
    when(mockIsACTMemberAsyncHandler.isACTActionAvailable())
      .thenReturn(getDoneFuture(false));
    when(mockSynapseJavascriptClient.getRestrictionInformation(any(), any()))
      .thenReturn(getDoneFuture(mockRestrictionInformation));

    // The controller under test.
    controller =
      new EntityActionControllerImpl(
        mockView,
        mockPreflightController,
        mockPortalGinInjector,
        mockAuthenticationController,
        mockCookies,
        mockIsACTMemberAsyncHandler,
        mockGWT,
        mockEventBus,
        mockPopupUtils
      );

    parentId = "syn456";
    entityId = "syn123";
    TableEntity table = new TableEntity();
    table.setName(entityName);
    table.setId(entityId);
    table.setParentId(parentId);
    table.setVersionNumber(3L);
    table.setIsLatestVersion(true);
    table.setIsSearchEnabled(false);
    permissions = new UserEntityPermissions();
    permissions.setCanChangePermissions(true);
    permissions.setCanView(true);
    permissions.setCanDelete(true);
    permissions.setCanPublicRead(true);
    permissions.setCanUpload(true);
    permissions.setCanDownload(true);
    permissions.setCanAddChild(true);
    permissions.setCanEdit(true);
    permissions.setCanCertifiedUserEdit(true);
    permissions.setCanChangeSettings(true);
    permissions.setCanMove(true);
    entityBundle = new EntityBundle();
    entityBundle.setEntity(table);
    entityBundle.setHasChildren(false);
    entityBundle.setPermissions(permissions);
    entityBundle.setDoiAssociation(new DoiAssociation());
    entityBundle.setBenefactorAcl(mockACL);
    resourceAccessSet = new HashSet<>();
    when(mockACL.getResourceAccess()).thenReturn(resourceAccessSet);
    when(mockPublicPrincipalIds.isPublic(PUBLIC_USER_ID)).thenReturn(true);
    selected = new Reference();
    selected.setTargetId("syn9876");

    when(mockEntityView.getId()).thenReturn(entityId);
    when(mockEntityView.getParentId()).thenReturn(parentId);
    when(mockEntityView.getViewTypeMask())
      .thenReturn(new Long(WebConstants.FILE));

    // Setup the mock entity selector to select an entity.
    Mockito
      .doAnswer(
        new Answer<Void>() {
          @Override
          public Void answer(InvocationOnMock invocation) throws Throwable {
            verify(mockEntityFinderBuilder)
              .setSelectedHandler(entityFinderSelectedHandlerCaptor.capture());
            EntityFinderWidget.SelectedHandler<Reference> handler = entityFinderSelectedHandlerCaptor.getValue();
            handler.onSelected(selected, mockEntityFinder);
            return null;
          }
        }
      )
      .when(mockEntityFinder)
      .show();
    currentEntityArea = null;
    CallbackMockStubber
      .invokeCallback()
      .when(mockGWT)
      .scheduleExecution(any(Callback.class), anyInt());

    when(mockPromptModalConfigurationBuilder.buildConfiguration())
      .thenReturn(mockPromptModalConfiguration);
    when(mockSynapseJavascriptClient.getChallengeForProject(anyString()))
      .thenReturn(getDoneFuture(new Challenge()));
  }

  @Test
  public void testConfigureWithTableEntity() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX + EntityTypeUtils.getDisplayName(EntityType.table)
      );
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, true);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // full text search
    verify(mockActionMenu)
      .setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, true);
    verify(mockActionMenu)
      .setActionText(Action.TOGGLE_FULL_TEXT_SEARCH, "Enable Full Text Search");
    verify(mockActionMenu)
      .setActionListener(Action.TOGGLE_FULL_TEXT_SEARCH, controller);
    // Show scope/items should not be visible for a TableEntity
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
  }

  @Test
  public void testConfigureWithDataset() {
    Dataset dataset = new Dataset();
    entityBundle.setEntity(dataset);
    boolean isCurrentVersion = true;
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      isCurrentVersion,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX + EntityTypeUtils.getDisplayName(EntityType.dataset)
      );
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, true);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // edit actions (should be disabled in the ui, even if user has permission)
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope should not be visible
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Edit dataset items should be visible
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, true);
  }

  @Test
  public void testConfigureWithDatasetNoPermission() {
    Dataset dataset = new Dataset();
    entityBundle.setEntity(dataset);
    boolean isCurrentVersion = true;
    boolean canEdit = false;
    boolean canCertifiedUserEdit = false;
    boolean canDelete = false;
    boolean canChangePermission = false;
    permissions.setCanEdit(canEdit);
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    permissions.setCanDelete(canDelete);
    permissions.setCanChangePermissions(canChangePermission);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      isCurrentVersion,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // Cannot delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, false);
    verify(mockActionMenu)
      .setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX + EntityTypeUtils.getDisplayName(EntityType.dataset)
      );
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share is always visible
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // Cannot rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, false);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload always disabled for datasets
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // Cannot create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, false);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // edit data actions always disabled for datasets
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope always disabled for datasets
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Cannot edit items without permission
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testConfigureWithDatasetSnapshot() {
    Dataset dataset = new Dataset();
    entityBundle.setEntity(dataset);
    boolean isCurrentVersion = false; // dataset is a snapshot!
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      isCurrentVersion,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX + EntityTypeUtils.getDisplayName(EntityType.dataset)
      );
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, true);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // edit actions (should be disabled in the ui, even if user has permission)
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope should not be visible
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Edit dataset items should NOT be visible if not the current version
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testConfigureWithDatasetCollection() {
    DatasetCollection datasetCollection = new DatasetCollection();
    entityBundle.setEntity(datasetCollection);
    boolean isCurrentVersion = true;
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      isCurrentVersion,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX +
        EntityTypeUtils.getDisplayName(EntityType.datasetcollection)
      );
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, true);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // edit actions (should be disabled in the ui, even if user has permission)
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope should not be visible
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Edit dataset items should be visible
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, true);
  }

  @Test
  public void testConfigureWithDatasetCollectionNoPermission() {
    DatasetCollection datasetCollection = new DatasetCollection();
    entityBundle.setEntity(datasetCollection);
    boolean isCurrentVersion = true;
    boolean canEdit = false;
    boolean canCertifiedUserEdit = false;
    boolean canDelete = false;
    boolean canChangePermission = false;
    permissions.setCanEdit(canEdit);
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    permissions.setCanDelete(canDelete);
    permissions.setCanChangePermissions(canChangePermission);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      isCurrentVersion,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // Cannot delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, false);
    verify(mockActionMenu)
      .setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX +
        EntityTypeUtils.getDisplayName(EntityType.datasetcollection)
      );
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share is always visible
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // Cannot rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, false);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload always disabled for datasets
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // Cannot create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, false);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // edit data actions always disabled for datasets
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope always disabled for datasets
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Cannot edit items without permission
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testConfigureWithDatasetCollectionSnapshot() {
    DatasetCollection datasetCollection = new DatasetCollection();
    entityBundle.setEntity(datasetCollection);
    boolean isCurrentVersion = false; // dataset is a snapshot!
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      isCurrentVersion,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(
        Action.DELETE_ENTITY,
        DELETE_PREFIX +
        EntityTypeUtils.getDisplayName(EntityType.datasetcollection)
      );
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, true);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // edit actions (should be disabled in the ui, even if user has permission)
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope should not be visible
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Edit dataset items should NOT be visible if not the current version
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testConfigureWithEntityView() {
    entityBundle.setEntity(mockEntityView);
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(Action.DELETE_ENTITY, DELETE_PREFIX + FILE_VIEW);
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, true);
    verify(mockActionMenu)
      .setActionListener(Action.CREATE_TABLE_VERSION, controller);
    // edit actions
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, true);
    // Show scope should be visible
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, true);
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Edit dataset items should not be visible

    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testConfigureWithMaterializedView() {
    entityBundle.setEntity(mockMaterializedView);
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");

    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // delete
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(Action.DELETE_ENTITY, DELETE_PREFIX + MATERIALIZED_VIEW);
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // upload
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // versions not currently supported for Materialized Views
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, false);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, false);
    // edit actions, never enabled for Materialized Views
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope should NOT be visible for Materialized Views (it's determined by the definingSQL)
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    // Materialized View has SQL definition that can be edited
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, true);
    // Edit dataset items should not be visible
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testConfigureWithMaterializedViewNoPermission() {
    entityBundle.setEntity(mockMaterializedView);
    boolean canEdit = false;
    boolean canCertifiedUserEdit = false;
    boolean canDelete = false;
    boolean canChangePermission = false;
    permissions.setCanEdit(canEdit);
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    permissions.setCanDelete(canDelete);
    permissions.setCanChangePermissions(canChangePermission);

    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");

    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // Cannot delete without permission
    verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, false);
    verify(mockActionMenu)
      .setActionText(Action.DELETE_ENTITY, DELETE_PREFIX + MATERIALIZED_VIEW);
    verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
    // share is always visible
    verify(mockActionMenu).setActionVisible(Action.VIEW_SHARING_SETTINGS, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_SHARING_SETTINGS, controller);
    // Cannot rename
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, false);
    verify(mockActionMenu)
      .setActionText(Action.CHANGE_ENTITY_NAME, EDIT_NAME_AND_DESCRIPTION);
    verify(mockActionMenu)
      .setActionListener(Action.CHANGE_ENTITY_NAME, controller);
    // Upload is never visible on MaterializedView
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    // versions not currently supported for Materialized Views
    // version history
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, false);
    // create table version (snapshot)
    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, false);
    // edit actions, never enabled for Materialized Views
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    // Show scope should NOT be visible for Materialized Views (it's determined by the definingSQL)
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
    // Cannot edit defining SQL without permission
    verify(mockActionMenu).setActionVisible(Action.EDIT_DEFINING_SQL, false);
    // Edit dataset items should never be visible for a materialized view
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testDisableFullTextSearch() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    ((TableEntity) entityBundle.getEntity()).setIsSearchEnabled(true);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, true);
    verify(mockActionMenu)
      .setActionText(
        Action.TOGGLE_FULL_TEXT_SEARCH,
        "Disable Full Text Search"
      );
    verify(mockActionMenu)
      .setActionListener(Action.TOGGLE_FULL_TEXT_SEARCH, controller);
  }

  @Test
  public void testConfigureDockerRepo() {
    // verify unable to rename or move docker repo entity name
    entityBundle.setEntity(new DockerRepository());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, false);
    verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, false);
    verify(mockActionMenu)
      .setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, false);
  }

  private void setPublicCanRead() {
    ResourceAccess ra = new ResourceAccess();
    ra.setAccessType(Collections.singleton(ACCESS_TYPE.READ));
    ra.setPrincipalId(PUBLIC_USER_ID);
    resourceAccessSet.add(ra);
  }

  @Test
  public void testConfigureProjectLevelTableCommandsCanEdit() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.TABLES;
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.UPLOAD_TABLE, canCertifiedUserEdit);
    verify(mockActionMenu).setActionListener(Action.UPLOAD_TABLE, controller);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_TABLE, canCertifiedUserEdit);
    verify(mockActionMenu).setActionListener(Action.ADD_TABLE, controller);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_FILE_VIEW, canCertifiedUserEdit);
    verify(mockActionMenu).setActionListener(Action.ADD_FILE_VIEW, controller);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_PROJECT_VIEW, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionListener(Action.ADD_PROJECT_VIEW, controller);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_MATERIALIZED_VIEW, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionListener(Action.ADD_MATERIALIZED_VIEW, controller);
  }

  @Test
  public void testConfigureProjectLevelTableCommandsCannotEdit() {
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.TABLES;
    boolean canCertifiedUserEdit = false;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.UPLOAD_TABLE, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_TABLE, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_FILE_VIEW, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_PROJECT_VIEW, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_MATERIALIZED_VIEW, canCertifiedUserEdit);
  }

  @Test
  public void testConfigureProjectLevelTableCommandsCanEditNotOnTablesTab() {
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.FILES;
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE, false);
    verify(mockActionMenu).setActionVisible(Action.ADD_TABLE, false);
    verify(mockActionMenu).setActionVisible(Action.ADD_FILE_VIEW, false);
    verify(mockActionMenu).setActionVisible(Action.ADD_PROJECT_VIEW, false);
  }

  @Test
  public void testConfigureProjectLevelDatasetCommandsCanEdit() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.DATASETS;
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.ADD_DATASET, canCertifiedUserEdit);
    verify(mockActionMenu).setActionListener(Action.ADD_DATASET, controller);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_DATASET_COLLECTION, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionListener(Action.ADD_DATASET_COLLECTION, controller);
  }

  @Test
  public void testConfigureProjectLevelDatasetCommandsCannotEdit() {
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.DATASETS;
    boolean canCertifiedUserEdit = false;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.ADD_DATASET, canCertifiedUserEdit);
    verify(mockActionMenu).setActionListener(Action.ADD_DATASET, controller);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_DATASET_COLLECTION, canCertifiedUserEdit);
    verify(mockActionMenu)
      .setActionListener(Action.ADD_DATASET_COLLECTION, controller);
  }

  @Test
  public void testConfigureProjectLevelDatasetCommandsCanEditNotOnDatasetsTab() {
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.FILES;
    boolean canCertifiedUserEdit = true;
    permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.ADD_DATASET, false);
    verify(mockActionMenu)
      .setActionVisible(Action.ADD_DATASET_COLLECTION, false);
  }

  @Test
  public void testConfigureReorderWikiSubpagesWithTree() {
    List<V2WikiHeader> headers = new ArrayList<>();
    V2WikiHeader page = new V2WikiHeader();
    page.setId("rootid");
    headers.add(page);
    page = new V2WikiHeader();
    page.setId("page 1");
    page.setTitle("page 1 title");
    page.setParentId("rootid");
    headers.add(page);

    AsyncMockStubber
      .callSuccessWith(headers)
      .when(mockSynapseJavascriptClient)
      .getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.WIKI;
    boolean canEdit = true;
    permissions.setCanEdit(canEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.REORDER_WIKI_SUBPAGES, true);
  }

  @Test
  public void testConfigureReorderWikiSubpagesNoTree() {
    AsyncMockStubber
      .callSuccessWith(new ArrayList<V2WikiHeader>())
      .when(mockSynapseJavascriptClient)
      .getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.WIKI;
    boolean canEdit = true;
    permissions.setCanEdit(canEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu)
      .setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
  }

  @Test
  public void testConfigureReorderWikiSubpagesNoEdit() {
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.WIKI;
    boolean canEdit = false;
    permissions.setCanEdit(canEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu)
      .setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
  }

  @Test
  public void testConfigureReorderWikiSubpagesNotOnProject() {
    entityBundle.setEntity(new Folder());
    currentEntityArea = null;
    boolean canEdit = true;
    permissions.setCanEdit(canEdit);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu)
      .setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
  }

  @Test
  public void testConfigurePublicReadTable() {
    setPublicCanRead();
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.SHOW_ANNOTATIONS, true);
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);

    // verify other table commands. current user canCertifiedUserEdit
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, true);
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
    verify(mockActionMenu).setActionVisible(Action.SHOW_TABLE_SCHEMA, true);
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
  }

  @Test
  public void testConfigureTableNoEdit() {
    setPublicCanRead();
    permissions.setCanCertifiedUserEdit(false);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.CREATE_TABLE_VERSION, true);
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);

    // verify other table commands. the current user cannot edit
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.SHOW_TABLE_SCHEMA, true);
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
  }

  @Test
  public void testConfigurePublicReadFile() {
    setPublicCanRead();
    Entity file = new FileEntity();
    file.setId(entityId);
    file.setParentId(parentId);
    entityBundle.setEntity(file);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.SHOW_ANNOTATIONS, true);
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);

    // table commands not shown for File
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
    verify(mockActionMenu).setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
    verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
  }

  @Test
  public void testConfigureVersionHistory() {
    Entity file = new FileEntity();
    file.setId(entityId);
    file.setParentId(parentId);
    permissions = new UserEntityPermissions();
    permissions.setCanChangePermissions(true);
    permissions.setCanDelete(true);
    permissions.setCanPublicRead(true);
    permissions.setCanUpload(true);
    permissions.setCanAddChild(true);
    permissions.setCanEdit(true);
    permissions.setCanCertifiedUserEdit(true);
    permissions.setCanMove(true);
    permissions.setCanDownload(true);
    entityBundle = new EntityBundle();
    entityBundle.setEntity(file);
    entityBundle.setPermissions(permissions);
    entityBundle.setBenefactorAcl(mockACL);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.SHOW_VERSION_HISTORY, true);
  }

  @Test
  public void testConfigureNoWiki() {
    entityBundle.setEntity(new Project());
    entityBundle.setRootWikiId(null);
    currentEntityArea = EntityArea.WIKI;
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, true);
    verify(mockActionMenu).setActionListener(Action.EDIT_WIKI_PAGE, controller);
    verify(mockActionMenu)
      .setActionText(
        Action.EDIT_WIKI_PAGE,
        EDIT_WIKI_PREFIX +
        EntityTypeUtils.getDisplayName(EntityType.project) +
        WIKI
      );
  }

  @Test
  public void testConfigureWiki() {
    entityBundle.setEntity(new Folder());
    entityBundle.setRootWikiId("7890");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, true);
    verify(mockActionMenu).setActionListener(Action.EDIT_WIKI_PAGE, controller);
    verify(mockActionMenu)
      .setActionText(
        Action.EDIT_WIKI_PAGE,
        EDIT_WIKI_PREFIX +
        EntityTypeUtils.getDisplayName(EntityType.folder) +
        WIKI
      );
  }

  @Test
  public void testConfigureWikiCannotEdit() {
    entityBundle.setEntity(new Folder());
    entityBundle.setRootWikiId("7890");
    permissions.setCanEdit(false);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, false);
    verify(mockActionMenu).setActionListener(Action.EDIT_WIKI_PAGE, controller);
  }

  @Test
  public void testConfigureDeleteWiki() {
    entityBundle.setEntity(new Project());
    entityBundle.setRootWikiId("7890");
    currentEntityArea = EntityArea.WIKI;
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.DELETE_WIKI_PAGE, true);
    verify(mockActionMenu)
      .setActionListener(Action.DELETE_WIKI_PAGE, controller);
  }

  @Test
  public void testConfigureDeleteWikiCannotDelete() {
    entityBundle.setEntity(new Project());
    entityBundle.setRootWikiId("7890");
    permissions.setCanDelete(false);
    currentEntityArea = EntityArea.WIKI;
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.DELETE_WIKI_PAGE, false);
    verify(mockActionMenu)
      .setActionListener(Action.DELETE_WIKI_PAGE, controller);
  }

  @Test
  public void testConfigureDeleteWikiFolder() {
    entityBundle.setEntity(new Folder());
    entityBundle.setRootWikiId("7890");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.DELETE_WIKI_PAGE, false);
  }

  @Test
  public void testConfigureWikiTable() {
    entityBundle.setEntity(new TableEntity());
    entityBundle.setRootWikiId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, true);
  }

  @Test
  public void testConfigureWikiView() {
    entityBundle.setEntity(mockEntityView);
    entityBundle.setRootWikiId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, true);
  }

  @Test
  public void testConfigureWikiDataset() {
    entityBundle.setEntity(new Dataset());
    entityBundle.setRootWikiId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, true);
  }

  @Test
  public void testConfigureViewWikiSource() {
    entityBundle.setEntity(new Folder());
    entityBundle.setRootWikiId("7890");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_WIKI_SOURCE, controller);
  }

  @Test
  public void testConfigureViewWikiSourceCannotEdit() {
    entityBundle.setEntity(new Folder());
    entityBundle.setRootWikiId("7890");
    permissions.setCanEdit(false);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, true);
    verify(mockActionMenu)
      .setActionListener(Action.VIEW_WIKI_SOURCE, controller);
  }

  @Test
  public void testConfigureViewWikiSourceWikiTable() {
    entityBundle.setEntity(new TableEntity());
    entityBundle.setRootWikiId("22");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, true);
  }

  @Test
  public void testConfigureViewWikiSourceWikiView() {
    entityBundle.setEntity(mockEntityView);
    entityBundle.setRootWikiId("22");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, true);
  }

  @Test
  public void testConfigureViewWikiSourceDataset() {
    entityBundle.setEntity(new Dataset());
    entityBundle.setRootWikiId("22");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, true);
  }

  @Test
  public void testConfigureAddSubmissionView() {
    entityBundle.setEntity(new Project());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      EntityArea.TABLES,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.ADD_SUBMISSION_VIEW, true);
    verify(mockActionMenu)
      .setActionListener(Action.ADD_SUBMISSION_VIEW, controller);
  }

  @Test
  public void testConfigureAddSubmissionViewCannotEdit() {
    entityBundle.setEntity(new Project());
    permissions.setCanCertifiedUserEdit(false);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      EntityArea.TABLES,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.ADD_SUBMISSION_VIEW, false);
    verify(mockActionMenu)
      .setActionListener(Action.ADD_SUBMISSION_VIEW, controller);
  }

  @Test
  public void testConfigureMoveTable() {
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, true);
  }

  @Test
  public void testConfigureMoveProject() {
    entityBundle.setEntity(new Project());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, false);
  }

  @Test
  public void testConfigureMove() {
    entityBundle.setEntity(new Folder());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, true);
    verify(mockActionMenu)
      .setActionText(
        Action.MOVE_ENTITY,
        MOVE_PREFIX + EntityTypeUtils.getDisplayName(EntityType.folder)
      );
    verify(mockActionMenu).setActionListener(Action.MOVE_ENTITY, controller);
  }

  @Test
  public void testCannotMoveWithoutCanMove() {
    permissions.setCanMove(false);
    entityBundle.setEntity(new Folder());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, false);
  }

  @Test
  public void testConfigureUploadNewFile() {
    entityBundle.setEntity(new FileEntity());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, true);
    verify(mockActionMenu)
      .setActionListener(Action.UPLOAD_NEW_FILE, controller);
  }

  @Test
  public void testConfigureUploadNewFileNoUpload() {
    entityBundle.getPermissions().setCanCertifiedUserEdit(false);
    entityBundle.setEntity(new FileEntity());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
    verify(mockActionMenu)
      .setActionListener(Action.UPLOAD_NEW_FILE, controller);
  }

  @Test
  public void testConfigureProvenanceFileCanEdit() {
    boolean canEdit = true;
    entityBundle.getPermissions().setCanEdit(canEdit);
    entityBundle.setEntity(new FileEntity());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
    verify(mockActionMenu)
      .setActionListener(Action.EDIT_PROVENANCE, controller);
  }

  @Test
  public void testConfigureProvenanceFileCannotEdit() {
    boolean canEdit = false;
    entityBundle.getPermissions().setCanEdit(canEdit);
    entityBundle.setEntity(new FileEntity());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
    verify(mockActionMenu)
      .setActionListener(Action.EDIT_PROVENANCE, controller);
  }

  @Test
  public void testConfigureProvenanceDockerCanEdit() {
    boolean canEdit = true;
    entityBundle.getPermissions().setCanEdit(canEdit);
    entityBundle.setEntity(new DockerRepository());
    currentEntityArea = EntityArea.DOCKER;
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
    verify(mockActionMenu)
      .setActionListener(Action.EDIT_PROVENANCE, controller);
  }

  @Test
  public void testConfigureProvenanceDockerCannotEdit() {
    boolean canEdit = false;
    entityBundle.getPermissions().setCanEdit(canEdit);
    entityBundle.setEntity(new DockerRepository());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
    verify(mockActionMenu)
      .setActionListener(Action.EDIT_PROVENANCE, controller);
  }

  @Test
  public void testConfigureProvenanceNonFileNorDocker() {
    entityBundle.setEntity(new Folder());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, false);
  }

  @Test
  public void testOnSelectApproveUserAccess() {
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.APPROVE_USER_ACCESS, null);
    verify(mockApproveUserAccessModal).configure(entityBundle);
    verify(mockApproveUserAccessModal).show();
  }

  @Test
  public void testOnEditProvenance() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.EDIT_PROVENANCE, null);
    verify(mockProvenanceEditorWidget).configure(entityBundle);
    verify(mockProvenanceEditorWidget).show();
  }

  @Test
  public void testOnDeleteConfirmCancel() {
    /*
     * The user must be shown a confirm dialog before a delete. Confirm is signaled via the
     * Callback.invoke() in this case we do not want to confirm.
     */
    AsyncMockStubber
      .callNoInvovke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // the call under tests
    controller.onAction(Action.DELETE_ENTITY, null);
    verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
    // should not make it to the pre-flight check
    verify(mockPreflightController, never())
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
  }

  @Test
  public void testOnDeleteConfirmedPreFlightFailed() {
    // confirm the delete
    AsyncMockStubber
      .callWithInvoke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    /*
     * The preflight check is confirmed by calling Callback.invoke(), in this case it must not be
     * invoked.
     */
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // the call under test
    controller.onAction(Action.DELETE_ENTITY, null);
    verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
    verify(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    // Must not make it to the actual delete since preflight failed.
    verify(mockSynapseJavascriptClient, never())
      .deleteEntityById(anyString(), any(AsyncCallback.class));
  }

  @Test
  public void testOnCreateTableSnapshot() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    // the call under test
    controller.onAction(Action.CREATE_TABLE_VERSION, null);

    verify(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    verify(mockPromptModalConfigurationBuilder).setTitle(anyString());
    verify(mockPromptModalConfigurationBuilder).setBodyCopy(anyString());
    verify(mockPromptModalConfigurationBuilder, times(2))
      .addPrompt(anyString(), anyString());
    verify(mockPromptModalConfigurationBuilder)
      .setCallback(callbackListStringCaptor.capture());
    verify(mockView).showMultiplePromptDialog(mockPromptModalConfiguration);
    CallbackP<List<String>> valuesCallback = callbackListStringCaptor.getValue();
    // invoke with a label and comment
    String label = "my label";
    String comment = "my comment";
    List<String> values = new ArrayList<String>();
    values.add(label);
    values.add(comment);
    valuesCallback.invoke(values);
    verify(mockSynapseJavascriptClient)
      .createSnapshot(
        eq(entityId),
        eq(comment),
        eq(label),
        isNull(String.class),
        tableSnapshotResponseCaptor.capture()
      );
    verify(mockView).hideMultiplePromptDialog();
    verify(mockView).showCreateVersionDialog();

    AsyncCallback<SnapshotResponse> handler = tableSnapshotResponseCaptor.getValue();

    // Failure
    String errorMsg = "Error message";
    handler.onFailure(new Exception(errorMsg));
    verify(mockView).hideCreateVersionDialog();
    verify(mockView).showErrorMessage(errorMsg);

    // Success
    handler.onSuccess(mockSnapshotResponse);
    verify(mockView, times(2)).hideCreateVersionDialog();
    verify(mockPopupUtils).notify(any(), any(), any(ToastMessageOptions.class));
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
    verify(mockPlaceChanger).goTo(any());
  }

  @Test
  public void testOnCreateEntityViewSnapshot() {
    entityBundle.setEntity(mockEntityView);
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    // the call under test
    controller.onAction(Action.CREATE_TABLE_VERSION, null);

    verify(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    verify(mockPromptModalConfigurationBuilder).setTitle(anyString());
    verify(mockPromptModalConfigurationBuilder).setBodyCopy(anyString());
    verify(mockPromptModalConfigurationBuilder, times(2))
      .addPrompt(anyString(), anyString());
    verify(mockPromptModalConfigurationBuilder)
      .setCallback(callbackListStringCaptor.capture());
    verify(mockView).showMultiplePromptDialog(mockPromptModalConfiguration);
    CallbackP<List<String>> valuesCallback = callbackListStringCaptor.getValue();
    // invoke with a label and comment
    String label = "my label";
    String comment = "my comment";
    List<String> values = new ArrayList<String>();
    values.add(label);
    values.add(comment);
    valuesCallback.invoke(values);

    verify(mockJobTrackingWidget)
      .startAndTrackJob(
        eq(EntityActionControllerImpl.CREATING_A_NEW_VIEW_VERSION_MESSAGE),
        eq(false),
        eq(AsynchType.TableTransaction),
        tableUpdateTransactionRequestCaptor.capture(),
        asyncProgressHandlerCaptor.capture()
      );
    TableUpdateTransactionRequest request = tableUpdateTransactionRequestCaptor.getValue();
    AsynchronousProgressHandler<TableUpdateTransactionResponse> handler = asyncProgressHandlerCaptor.getValue();

    // verify request
    assertEquals(entityId, request.getEntityId());
    assertEquals(label, request.getSnapshotOptions().getSnapshotLabel());
    assertEquals(comment, request.getSnapshotOptions().getSnapshotComment());

    // The prompt is hidden after the request is made
    verify(mockView).hideMultiplePromptDialog();

    // verify response handler
    // on error
    String errorMessage = "an error";
    handler.onFailure(new Exception(errorMessage));
    verify(mockView).hideCreateVersionDialog();
    verify(mockView).showErrorMessage(errorMessage);
    reset(mockView);

    // on cancel
    handler.onCancel();
    verify(mockView).hideCreateVersionDialog();
    reset(mockView);

    // on success
    handler.onComplete(mockTableUpdateTransactionResponse);
    verify(mockView).hideCreateVersionDialog();
    verify(mockPopupUtils).notify(any(), any(), any(ToastMessageOptions.class));
    verify(mockPlaceChanger).goTo(any());
    // Don't fire an updated event; we used placeChanger to go to a new place.
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnCreateDatasetSnapshot() {
    Entity dataset = new Dataset();
    dataset.setId(entityId);
    dataset.setParentId(parentId);
    entityBundle.setEntity(dataset);
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    // the call under test
    controller.onAction(Action.CREATE_TABLE_VERSION, null);

    verify(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    verify(mockPromptModalConfigurationBuilder).setTitle(anyString());
    verify(mockPromptModalConfigurationBuilder).setBodyCopy(anyString());
    verify(mockPromptModalConfigurationBuilder, times(2))
      .addPrompt(anyString(), anyString());
    verify(mockPromptModalConfigurationBuilder)
      .setCallback(callbackListStringCaptor.capture());
    verify(mockView).showMultiplePromptDialog(mockPromptModalConfiguration);
    CallbackP<List<String>> valuesCallback = callbackListStringCaptor.getValue();
    // invoke with a label and comment
    String label = "my label";
    String comment = "my comment";
    List<String> values = new ArrayList<String>();
    values.add(label);
    values.add(comment);
    valuesCallback.invoke(values);

    verify(mockJobTrackingWidget)
      .startAndTrackJob(
        eq(EntityActionControllerImpl.CREATING_A_NEW_DATASET_VERSION_MESSAGE),
        eq(false),
        eq(AsynchType.TableTransaction),
        tableUpdateTransactionRequestCaptor.capture(),
        asyncProgressHandlerCaptor.capture()
      );
    TableUpdateTransactionRequest request = tableUpdateTransactionRequestCaptor.getValue();
    AsynchronousProgressHandler handler = asyncProgressHandlerCaptor.getValue();

    // verify request
    assertEquals(entityId, request.getEntityId());
    assertEquals(label, request.getSnapshotOptions().getSnapshotLabel());
    assertEquals(comment, request.getSnapshotOptions().getSnapshotComment());

    // The prompt is hidden when the request is initiated
    verify(mockView).hideMultiplePromptDialog();

    // verify response handler
    // on error
    String errorMessage = "an error";
    handler.onFailure(new Exception(errorMessage));
    verify(mockView).hideCreateVersionDialog();
    verify(mockView).showErrorMessage(errorMessage);
    reset(mockView);

    // on cancel
    handler.onCancel();
    verify(mockView).hideCreateVersionDialog();
    reset(mockView);

    // on success
    handler.onComplete(mockTableUpdateTransactionResponse);
    verify(mockView).hideCreateVersionDialog();
    verify(mockPopupUtils).notify(any(), any(), any(ToastMessageOptions.class));
    verify(mockPlaceChanger).goTo(any());
    // Don't fire an updated event; we used placeChanger to go to a new place.
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnDeleteConfirmedPreFlightPassedDeleteFailed() {
    // confirm the delete
    AsyncMockStubber
      .callWithInvoke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    // confirm pre-flight
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    String error = "some error";
    AsyncMockStubber
      .callFailureWith(new Throwable(error))
      .when(mockSynapseJavascriptClient)
      .deleteEntityById(anyString(), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // the call under test
    controller.onAction(Action.DELETE_ENTITY, null);
    verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
    verify(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    // an attempt to delete should be made
    verify(mockSynapseJavascriptClient)
      .deleteEntityById(anyString(), any(AsyncCallback.class));
    verify(mockView)
      .showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE + error);
  }

  @Test
  public void testOnDeleteConfirmedPreFlightPassedDeleteSuccess() {
    // confirm the delete
    AsyncMockStubber
      .callWithInvoke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    // confirm pre-flight
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockSynapseJavascriptClient)
      .deleteEntityById(anyString(), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // the call under test
    controller.onAction(Action.DELETE_ENTITY, null);
    verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
    verify(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    // an attempt to delete should be made
    verify(mockSynapseJavascriptClient)
      .deleteEntityById(anyString(), any(AsyncCallback.class));
    verify(mockView)
      .showInfo(
        THE +
        EntityTypeUtils.getDisplayName(EntityType.table) +
        WAS_SUCCESSFULLY_DELETED
      );
    verify(mockPlaceChanger)
      .goTo(new Synapse(parentId, null, EntityArea.TABLES, null));
  }

  @Test
  public void testCreateDeletePlaceNullParentId() {
    entityBundle.getEntity().setParentId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // call under test
    Place result = controller.createDeletePlace();
    assertTrue(result instanceof Profile);
    assertEquals(currentUserId, ((Profile) result).getUserId());
    assertEquals(ProfileArea.PROJECTS, ((Profile) result).getArea());
  }

  @Test
  public void testCreateDeletePlaceProject() {
    // setup a project
    Entity project = new Project();
    project.setId(entityId);
    project.setParentId(parentId);
    entityBundle = new EntityBundle();
    entityBundle.setEntity(project);
    entityBundle.setPermissions(permissions);
    entityBundle.setBenefactorAcl(mockACL);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // call under test
    Place result = controller.createDeletePlace();
    assertTrue(result instanceof Profile);
    assertEquals(currentUserId, ((Profile) result).getUserId());
    assertEquals(ProfileArea.PROJECTS, ((Profile) result).getArea());
  }

  @Test
  public void testCreateDeletePlaceFile() {
    // setup a project
    Entity file = new FileEntity();
    file.setId(entityId);
    file.setParentId(parentId);
    entityBundle.setEntity(file);
    entityBundle.setPermissions(entityBundle.getPermissions());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // call under test
    Place result = controller.createDeletePlace();
    Place expected = new Synapse(parentId, null, EntityArea.FILES, null);
    assertEquals(expected, result);
  }

  @Test
  public void testCreateDeletePlaceDocker() {
    Entity docker = new DockerRepository();
    docker.setId(entityId);
    docker.setParentId(parentId);
    entityBundle.setEntity(docker);
    entityBundle.setPermissions(entityBundle.getPermissions());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // call under test
    Place result = controller.createDeletePlace();
    Place expected = new Synapse(parentId, null, EntityArea.DOCKER, null);
    assertEquals(expected, result);
  }

  @Test
  public void testOnShareNoChange() {
    /*
     * Share change is confirmed by calling Callback.invoke(), in this case it must not be invoked.
     */
    AsyncMockStubber
      .callNoInvovke()
      .when(mockAccessControlListModalWidget)
      .showSharing(any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.VIEW_SHARING_SETTINGS, null);
    verify(mockAccessControlListModalWidget).showSharing(any(Callback.class));
    verify(mockAccessControlListModalWidget)
      .configure(any(Entity.class), anyBoolean());
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnShareWithChange() {
    // invoke this time
    AsyncMockStubber
      .callWithInvoke()
      .when(mockAccessControlListModalWidget)
      .showSharing(any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.VIEW_SHARING_SETTINGS, null);
    verify(mockAccessControlListModalWidget)
      .configure(any(Entity.class), anyBoolean());
    verify(mockAccessControlListModalWidget).showSharing(any(Callback.class));
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testRenameHappy() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockRenameEntityModalWidget)
      .onRename(any(Entity.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.CHANGE_ENTITY_NAME, null);
    verify(mockRenameEntityModalWidget)
      .onRename(any(Entity.class), any(Callback.class));
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testRenameNoChange() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callNoInvovke()
      .when(mockRenameEntityModalWidget)
      .onRename(any(Entity.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.CHANGE_ENTITY_NAME, null);
    verify(mockRenameEntityModalWidget)
      .onRename(any(Entity.class), any(Callback.class));
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testRenameFailedPreFlight() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callNoInvovke()
      .when(mockRenameEntityModalWidget)
      .onRename(any(Entity.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.CHANGE_ENTITY_NAME, null);
    verify(mockRenameEntityModalWidget, never())
      .onRename(any(Entity.class), any(Callback.class));
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testHasCustomRenameEditor() {
    assertTrue(controller.hasCustomRenameEditor(new FileEntity()));
    assertFalse(controller.hasCustomRenameEditor(new TableEntity()));
    assertTrue(controller.hasCustomRenameEditor(new Project()));
  }

  @Test
  public void testEditFileMetadataIsCurrent() {
    Entity file = new FileEntity();
    file.setId(entityId);
    file.setParentId(parentId);
    entityBundle.setEntity(file);

    // Most current version represented by null
    Synapse currentPlace = new Synapse(
      currentUserId,
      null,
      null,
      currentUserId
    );
    when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(currentPlace);
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callNoInvovke()
      .when(mockEditFileMetadataModalWidget)
      .configure(
        any(FileEntity.class),
        any(FileHandle.class),
        any(Callback.class)
      );

    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.EDIT_FILE_METADATA, null);
    verify(mockEditFileMetadataModalWidget)
      .configure(
        any(FileEntity.class),
        any(FileHandle.class),
        any(Callback.class)
      );
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testEditFileMetadataIsNotCurrent() {
    FileEntity file = new FileEntity();
    file.setId(entityId);
    file.setParentId(parentId);
    file.setVersionNumber(1L);
    entityBundle.setEntity(file);
    // currentPlace returns a non-null versionNumber
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callNoInvovke()
      .when(mockEditFileMetadataModalWidget)
      .configure(
        any(FileEntity.class),
        any(FileHandle.class),
        any(Callback.class)
      );
    controller.configure(
      mockActionMenu,
      entityBundle,
      false,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.EDIT_FILE_METADATA, null);
    verify(mockEditFileMetadataModalWidget, never())
      .configure(
        any(FileEntity.class),
        any(FileHandle.class),
        any(Callback.class)
      );
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
    verify(mockView)
      .showErrorMessage(
        "Can only edit the metadata of the most recent file version."
      );
  }

  @Test
  public void testEditProjectMetadata() {
    Entity project = new Project();
    project.setId(entityId);
    project.setParentId(parentId);
    entityBundle.setEntity(project);

    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callNoInvovke()
      .when(mockEditProjectMetadataModalWidget)
      .configure(any(Project.class), anyBoolean(), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.EDIT_PROJECT_METADATA, null);
    verify(mockEditProjectMetadataModalWidget)
      .configure(any(Project.class), anyBoolean(), any(Callback.class));
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testEditProjectMetadataFailedPreFlight() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callNoInvovke()
      .when(mockEditProjectMetadataModalWidget)
      .configure(any(Project.class), anyBoolean(), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // method under test
    controller.onAction(Action.EDIT_PROJECT_METADATA, null);
    verify(mockEditProjectMetadataModalWidget, never())
      .configure(any(Project.class), anyBoolean(), any(Callback.class));
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnAddWikiNoUpdate() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    entityBundle.setRootWikiId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.EDIT_WIKI_PAGE, null);
    verify(mockSynapseClient, never())
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnAddWikiCanUpdate() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callSuccessWith(new WikiPage())
      .when(mockSynapseClient)
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    entityBundle.setRootWikiId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      null,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.EDIT_WIKI_PAGE, null);
    verify(mockMarkdownEditorWidget)
      .configure(any(WikiPageKey.class), any(CallbackP.class));
  }

  @Test
  public void testOnViewWikiSource() {
    WikiPage page = new WikiPage();
    String markdown = "hello markdown";
    page.setMarkdown(markdown);
    AsyncMockStubber
      .callSuccessWith(page)
      .when(mockSynapseJavascriptClient)
      .getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
    entityBundle.setRootWikiId("111");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      null,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.VIEW_WIKI_SOURCE, null);
    verify(mockView).showInfoDialog(anyString(), eq(markdown));
  }

  @Test
  public void testOnViewWikiSourceError() {
    AsyncMockStubber
      .callFailureWith(new Exception())
      .when(mockSynapseJavascriptClient)
      .getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
    entityBundle.setRootWikiId("111");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      null,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.VIEW_WIKI_SOURCE, null);
    verify(mockView).showErrorMessage(anyString());
  }

  @Test
  public void testIsMovableType() {
    assertFalse(controller.isMovableType(new Project()));
    assertFalse(controller.isMovableType(new DockerRepository()));
    assertTrue(controller.isMovableType(new TableEntity()));
    assertTrue(controller.isMovableType(new EntityView()));
    assertTrue(controller.isMovableType(new FileEntity()));
    assertTrue(controller.isMovableType(new Folder()));
    assertTrue(controller.isMovableType(new Link()));
  }

  @Test
  public void testIsTopLevelProjectToolsMenu() {
    // is a top level tools menu if the target entity is a Project, and the area is set
    assertTrue(
      controller.isTopLevelProjectToolsMenu(new Project(), EntityArea.FILES)
    );
    assertTrue(
      controller.isTopLevelProjectToolsMenu(
        new Project(),
        EntityArea.DISCUSSION
      )
    );
    // if the area is not set, then it's the Project Settings)
    assertFalse(controller.isTopLevelProjectToolsMenu(new Project(), null));
    // if looking at a specific child (file/table/...), then it is not a top level project tools menu
    // for that area.
    assertFalse(controller.isTopLevelProjectToolsMenu(new FileEntity(), null));
  }

  @Test
  public void testIsContainerOnFilesTab() {
    // for commands like upload. can be used on a Folder, or when looking at a Project on the Files tab
    // (root file/folder container).
    assertTrue(
      controller.isContainerOnFilesTab(new Project(), EntityArea.FILES)
    );
    assertTrue(controller.isContainerOnFilesTab(new Folder(), null));
    assertFalse(
      controller.isContainerOnFilesTab(new Project(), EntityArea.WIKI)
    );
    assertFalse(
      controller.isContainerOnFilesTab(new Project(), EntityArea.DISCUSSION)
    );
  }

  @Test
  public void testIsWikableConfig() {
    // if entity is a project, must be in Wiki area for wiki commands to show up in the tools menu.
    assertTrue(controller.isWikiableConfig(new Project(), EntityArea.WIKI));
    assertFalse(controller.isWikiableConfig(new Project(), EntityArea.TABLES));
    assertFalse(controller.isWikiableConfig(new Project(), EntityArea.FILES));
    assertTrue(controller.isWikiableConfig(new TableEntity(), null));
    assertTrue(controller.isWikiableConfig(new FileEntity(), null));
  }

  @Test
  public void testIsWikableType() {
    assertTrue(controller.isWikiableType(new Project()));
    assertTrue(controller.isWikiableType(new TableEntity()));
    assertTrue(controller.isWikiableType(new EntityView()));
    assertTrue(controller.isWikiableType(new Dataset()));
    assertTrue(controller.isWikiableType(new FileEntity()));
    assertTrue(controller.isWikiableType(new Folder()));
    assertFalse(controller.isWikiableType(new Link()));
  }

  @Test
  public void testIsLinkType() {
    assertTrue(controller.isLinkType(new Project()));
    assertTrue(controller.isLinkType(new TableEntity()));
    assertTrue(controller.isLinkType(new FileEntity()));
    assertTrue(controller.isLinkType(new Folder()));
    assertFalse(controller.isLinkType(new Link()));
  }

  @Test
  public void testIsSubmittableType() {
    assertFalse(controller.isSubmittableType(new Project()));
    assertFalse(controller.isSubmittableType(new TableEntity()));
    assertTrue(controller.isSubmittableType(new FileEntity()));
    assertTrue(controller.isSubmittableType(new DockerRepository()));
    assertFalse(controller.isSubmittableType(new Folder()));
    assertFalse(controller.isSubmittableType(new Link()));
  }

  @Test
  public void testOnMoveNoUpdate() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    entityBundle.setEntity(new Folder());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.MOVE_ENTITY, null);
    verify(mockEntityFinderBuilder, never()).build();
    verify(mockEntityFinder, never()).show();
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnMoveCanUpdateFailed() {
    String error = "An error";
    AsyncMockStubber
      .callFailureWith(new Throwable(error))
      .when(mockSynapseClient)
      .moveEntity(anyString(), anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.MOVE_ENTITY, null);
    verify(mockEntityFinderBuilder).setSelectableTypes(EntityFilter.PROJECT);
    verify(mockEntityFinderBuilder)
      .setVersionSelection(EntityFinderWidget.VersionSelection.DISALLOWED);
    verify(mockEntityFinderBuilder).build();
    verify(mockEntityFinder).show();
    verify(mockEntityFinder, never()).hide();
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
    verify(mockEntityFinder).showError(error);
  }

  @Test
  public void testOnMoveCanUpdateSuccess() {
    AsyncMockStubber
      .callSuccessWith(new Folder())
      .when(mockSynapseClient)
      .moveEntity(anyString(), anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.MOVE_ENTITY, null);
    verify(mockEntityFinderBuilder).setSelectableTypes(EntityFilter.PROJECT);
    verify(mockEntityFinderBuilder)
      .setVersionSelection(EntityFinderWidget.VersionSelection.DISALLOWED);
    verify(mockEntityFinderBuilder)
      .setSelectedHandler(any(EntityFinderWidget.SelectedHandler.class));
    verify(mockEntityFinderBuilder).build();
    verify(mockEntityFinder).show();
    verify(mockEntityFinder).hide();
    verify(mockSynapseClient)
      .moveEntity(anyString(), anyString(), any(AsyncCallback.class));
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
    verify(mockView, never()).showErrorMessage(anyString());
  }

  @Test
  public void testCreateLinkBadRequest() {
    AsyncMockStubber
      .callFailureWith(new BadRequestException("bad"))
      .when(mockSynapseJavascriptClient)
      .createEntity(any(Entity.class), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.createLink("syn9876", mockEntityFinder);
    verify(mockEntityFinder).showError(DisplayConstants.ERROR_CANT_MOVE_HERE);
    verify(mockEntityFinder, never()).hide();
  }

  @Test
  public void testCreateLinkNotFound() {
    AsyncMockStubber
      .callFailureWith(new NotFoundException("not found"))
      .when(mockSynapseJavascriptClient)
      .createEntity(any(Entity.class), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.createLink("syn9876", mockEntityFinder);
    verify(mockEntityFinder).showError(DisplayConstants.ERROR_NOT_FOUND);
    verify(mockEntityFinder, never()).hide();
  }

  @Test
  public void testCreateLinkUnauthorizedException() {
    AsyncMockStubber
      .callFailureWith(new UnauthorizedException("no way"))
      .when(mockSynapseJavascriptClient)
      .createEntity(any(Entity.class), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.createLink("syn9876", mockEntityFinder);
    verify(mockEntityFinder).showError(DisplayConstants.ERROR_NOT_AUTHORIZED);
    verify(mockEntityFinder, never()).hide();
  }

  @Test
  public void testCreateLinkUnknownException() {
    String error = "some error";
    AsyncMockStubber
      .callFailureWith(new Throwable(error))
      .when(mockSynapseJavascriptClient)
      .createEntity(any(Entity.class), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.createLink("syn9876", mockEntityFinder);
    verify(mockEntityFinder).showError(error);
    verify(mockEntityFinder, never()).hide();
  }

  @Test
  public void testCreateLink() {
    String entityId = "syn123";
    Entity entity = entityBundle.getEntity();
    entity.setId(entityId);
    Long entityVersion = 42L;
    ((Versionable) entity).setVersionNumber(entityVersion);
    ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
    AsyncMockStubber
      .callSuccessWith(new Link())
      .when(mockSynapseJavascriptClient)
      .createEntity(argument.capture(), any(AsyncCallback.class));
    boolean isCurrentVersion = false;
    controller.configure(
      mockActionMenu,
      entityBundle,
      isCurrentVersion,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.setIsShowingVersion(true);
    String target = "syn9876";
    controller.createLink(target, mockEntityFinder);
    verify(mockView, never()).showErrorMessage(anyString());
    verify(mockPopupUtils)
      .notify(
        eq(DisplayConstants.TEXT_LINK_SAVED),
        eq(NotificationVariant.SUCCESS),
        any(ToastMessageOptions.class)
      );
    verify(mockEntityFinder).hide();
    Entity capture = argument.getValue();
    assertNotNull(capture);
    assertTrue(capture instanceof Link);
    Link link = (Link) capture;
    assertEquals(target, link.getParentId());
    assertEquals(entityBundle.getEntity().getName(), link.getName());
    Reference ref = link.getLinksTo();
    assertNotNull(ref);
    assertEquals(entityId, ref.getTargetId());
    assertEquals(entityVersion, ref.getTargetVersionNumber());
  }

  @Test
  public void testCreateLinkCurrentVersion() {
    String entityId = "syn123";
    Entity entity = entityBundle.getEntity();
    entity.setId(entityId);
    Long entityVersion = 42L;
    ((Versionable) entity).setVersionNumber(entityVersion);
    ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
    AsyncMockStubber
      .callSuccessWith(new Link())
      .when(mockSynapseJavascriptClient)
      .createEntity(argument.capture(), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.setIsShowingVersion(false);
    String target = "syn9876";
    controller.createLink(target, mockEntityFinder);
    verify(mockEntityFinder).hide();
    Entity capture = argument.getValue();
    assertNotNull(capture);
    assertTrue(capture instanceof Link);
    Link link = (Link) capture;
    assertEquals(target, link.getParentId());
    assertEquals(entityBundle.getEntity().getName(), link.getName());
    Reference ref = link.getLinksTo();
    assertNotNull(ref);
    assertEquals(entityId, ref.getTargetId());
    assertNull(ref.getTargetVersionNumber());
  }

  @Test
  public void testOnLinkNoUpdate() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.CREATE_LINK, null);
    verify(mockEntityFinder, never()).show();
    verify(mockView, never()).showInfo(anyString());
  }

  @Test
  public void testOnLink() {
    AsyncMockStubber
      .callSuccessWith(new Link())
      .when(mockSynapseJavascriptClient)
      .createEntity(any(Entity.class), any(AsyncCallback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.CREATE_LINK, null);
    verify(mockEntityFinderBuilder).setSelectableTypes(EntityFilter.CONTAINER);
    verify(mockEntityFinderBuilder)
      .setVersionSelection(EntityFinderWidget.VersionSelection.DISALLOWED);
    verify(mockEntityFinderBuilder).build();
    verify(mockEntityFinder).show();
    verify(mockEntityFinder).hide();
    verify(mockPopupUtils)
      .notify(
        eq(DisplayConstants.TEXT_LINK_SAVED),
        eq(NotificationVariant.SUCCESS),
        any(ToastMessageOptions.class)
      );
  }

  @Test
  public void testOnSubmitNoUpdate() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.SUBMIT_TO_CHALLENGE, null);
    verify(mockSubmitter, never())
      .configure(any(Entity.class), any(Set.class), any(FormParams.class));
  }

  @Test
  public void testOnSubmitWithUdate() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.SUBMIT_TO_CHALLENGE, null);
    verify(mockSubmitter)
      .configure(any(Entity.class), any(Set.class), any(FormParams.class));
  }

  @Test
  public void testOnChangeStorageLocation() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.CHANGE_STORAGE_LOCATION, null);
    verify(mockStorageLocationWidget).configure(this.entityBundle);
    verify(mockStorageLocationWidget).show();
  }

  @Test
  public void testOnChangeStorageLocationNoUpload() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.CHANGE_STORAGE_LOCATION, null);
    verify(mockStorageLocationWidget, never())
      .configure(any(EntityBundle.class));
    verify(mockStorageLocationWidget, never()).show();
  }

  @Test
  public void testOnUploadNewFileNoUpload() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.UPLOAD_NEW_FILE, null);
    verify(mockUploader, never()).show();
  }

  @Test
  public void testOnUploadNewFileWithUpload() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.UPLOAD_NEW_FILE, null);
    verify(mockUploader).show();
    verify(mockUploader).setUploaderLinkNameVisible(false);
  }

  @Test
  public void testConfigureNoWikiSubpageProject() {
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.WIKI;
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, true);
    verify(mockActionMenu)
      .setActionListener(Action.ADD_WIKI_SUBPAGE, controller);
  }

  @Test
  public void testConfigurWikiSubpageProjectNoRootPage() {
    entityBundle.setEntity(new Project());
    currentEntityArea = EntityArea.WIKI;
    wikiPageId = null;
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
    verify(mockActionMenu, never())
      .setActionVisible(Action.ADD_WIKI_SUBPAGE, true);
  }

  @Test
  public void testConfigureWikiSubpageFolder() {
    entityBundle.setEntity(new Folder());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
  }

  @Test
  public void testConfigureWikiSubpageTable() {
    entityBundle.setEntity(new TableEntity());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
  }

  @Test
  public void testConfigureWikiSubpageView() {
    entityBundle.setEntity(mockEntityView);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
  }

  @Test
  public void testOnAddWikiSubpageNoUpdate() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    entityBundle.setRootWikiId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.ADD_WIKI_SUBPAGE, null);
    verify(mockSynapseClient, never())
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnRootAddWikiSubpageCanUpdate() {
    // Edge case. User attempts to add a subpage on a project that does not yet have a wiki. Verify a
    // root page is created (and page refreshed)...
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    WikiPage newWikiPage = new WikiPage();
    String newWikiPageId = "49382";
    newWikiPage.setId(newWikiPageId);
    AsyncMockStubber
      .callSuccessWith(newWikiPage)
      .when(mockSynapseClient)
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    entityBundle.setRootWikiId(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      null,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.ADD_WIKI_SUBPAGE, null);
    verify(mockSynapseClient)
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockPlaceChanger)
      .goTo(new Synapse(entityId, null, EntityArea.WIKI, newWikiPageId));
  }

  @Test
  public void testOnAddWikiSubpageCanUpdate() {
    // Set up so that we are on the root wiki page, and we run the add subpage command.
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    WikiPage newWikiPage = new WikiPage();
    String newWikiPageId = "55555";
    newWikiPage.setId(newWikiPageId);

    AsyncMockStubber
      .callSuccessWith(newWikiPage)
      .when(mockSynapseClient)
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    entityBundle.setRootWikiId("123");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      "123",
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.ADD_WIKI_SUBPAGE, null);
    // verify that it has not yet created the wiki page
    verify(mockSynapseClient, never())
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
    // it prompts the user for a wiki page name
    ArgumentCaptor<PromptCallback> callbackCaptor = ArgumentCaptor.forClass(
      PromptCallback.class
    );
    verify(mockView)
      .showPromptDialog(
        anyString(),
        anyString(),
        callbackCaptor.capture(),
        eq(PromptForValuesModalView.InputType.TEXTBOX)
      );
    PromptCallback capturedCallback = callbackCaptor.getValue();
    // if called back with an undefined value, a wiki page is still not created
    capturedCallback.callback("");
    verify(mockSynapseClient, never())
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));

    capturedCallback.callback(null);
    verify(mockSynapseClient, never())
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));

    capturedCallback.callback("a valid name");
    verify(mockSynapseClient)
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockView).showSuccess(anyString());
    verify(mockPlaceChanger)
      .goTo(new Synapse(entityId, null, EntityArea.WIKI, newWikiPageId));
  }

  @Test
  public void testCreateWikiPageFailure() {
    // Set up so that we are on the root wiki page, and we run the add subpage command.
    String error = "goodnight";
    AsyncMockStubber
      .callFailureWith(new Exception(error))
      .when(mockSynapseClient)
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    entityBundle.setRootWikiId("123");
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      "123",
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.createWikiPage("foo");

    verify(mockSynapseClient)
      .createV2WikiPageWithV1(
        anyString(),
        anyString(),
        any(WikiPage.class),
        any(AsyncCallback.class)
      );
    verify(mockView).showErrorMessage(anyString());
  }

  @Test
  public void testConfigureCreateOrUpdateDoiNotFound() throws Exception {
    entityBundle.setDoiAssociation(null);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // initially hide, then show
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
  }

  @Test
  public void testConfigureCreateOrUpdateDoiView() throws Exception {
    // Create DOI is now available for Views since they're now versionable (SWC-4062, SWC-5191)
    entityBundle.setDoiAssociation(null);
    entityBundle.setEntity(mockEntityView);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
    verify(mockActionMenu)
      .setActionVisible(Action.TOGGLE_FULL_TEXT_SEARCH, true);
  }

  @Test
  public void testConfigureCreateOrUpdateDoiNotFoundNonEditable()
    throws Exception {
    permissions.setCanEdit(false);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // initially hide, never show
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
    verify(mockActionMenu, never())
      .setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
  }

  @Test
  public void testConfigureCreateOrUpdateDoiFound() throws Exception {
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // hide, and then show with 'update' text
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
    verify(mockActionMenu)
      .setActionText(
        Action.CREATE_OR_UPDATE_DOI,
        UPDATE_DOI_FOR +
        EntityTypeUtils.getDisplayName(
          EntityTypeUtils.getEntityTypeForClass(
            entityBundle.getEntity().getClass()
          )
        )
      );
  }

  @Test
  public void testConfigureCreateOrUpdateDoiFoundNonEditable()
    throws Exception {
    permissions.setCanEdit(false);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // hide, and then show with 'update' text
    verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
    verify(mockActionMenu, never())
      .setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
  }

  @Test
  public void testOnSelectChallengeTeam() {
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockChallengeClient)
      .createChallenge(any(Challenge.class), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.CREATE_CHALLENGE, null);
    verify(mockSelectTeamModal).show();

    // now simulate that a team was selected
    ArgumentCaptor<CallbackP> teamSelectedCallback = ArgumentCaptor.forClass(
      CallbackP.class
    );
    verify(mockSelectTeamModal).configure(teamSelectedCallback.capture());
    teamSelectedCallback.getValue().invoke(SELECTED_TEAM_ID);

    ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
    verify(mockChallengeClient)
      .createChallenge(captor.capture(), any(AsyncCallback.class));
    verify(mockPlaceChanger)
      .goTo(new Synapse(entityId, null, EntityArea.CHALLENGE, null));
    verify(mockView).showSuccess(DisplayConstants.CHALLENGE_CREATED);
    Challenge c = captor.getValue();
    assertNull(c.getId());
    assertEquals(SELECTED_TEAM_ID, c.getParticipantTeamId());
  }

  @Test
  public void testCreateChallengeFailure() {
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    String error = "an error";
    AsyncMockStubber
      .callFailureWith(new Exception(error))
      .when(mockChallengeClient)
      .createChallenge(any(Challenge.class), any(AsyncCallback.class));
    controller.onAction(Action.CREATE_CHALLENGE, null);
    // now simulate that a challenge team was selected
    ArgumentCaptor<CallbackP> teamSelectedCallback = ArgumentCaptor.forClass(
      CallbackP.class
    );
    verify(mockSelectTeamModal).configure(teamSelectedCallback.capture());
    teamSelectedCallback.getValue().invoke(SELECTED_TEAM_ID);

    verify(mockChallengeClient)
      .createChallenge(any(Challenge.class), any(AsyncCallback.class));
    verify(mockView).showErrorMessage(error);
  }

  @Test
  public void testConfigureChallengeNotFound() throws Exception {
    // note that the currentArea is null (project settings)
    currentEntityArea = null;
    entityBundle.setEntity(new Project());
    when(mockSynapseJavascriptClient.getChallengeForProject(anyString()))
      .thenReturn(getFailedFuture(new NotFoundException()));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // initially hide, then show
    InOrder inOrder = inOrder(mockActionMenu);
    inOrder
      .verify(mockActionMenu)
      .setActionVisible(Action.CREATE_CHALLENGE, false);
    inOrder
      .verify(mockActionMenu)
      .setActionVisible(Action.CREATE_CHALLENGE, true);
  }

  @Test
  public void testConfigureChallengeFoundProjectSettingsMenu()
    throws Exception {
    // project settings menu
    currentEntityArea = null;
    entityBundle.setEntity(new Project());
    AsyncMockStubber
      .callSuccessWith(new Challenge())
      .when(mockChallengeClient)
      .getChallengeForProject(anyString(), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu, never())
      .setActionVisible(Action.DELETE_CHALLENGE, true);
  }

  @Test
  public void testConfigureChallengeFound() throws Exception {
    // currentArea is on the challenge tab
    currentEntityArea = EntityArea.CHALLENGE;
    entityBundle.setEntity(new Project());
    when(mockSynapseJavascriptClient.getChallengeForProject(anyString()))
      .thenReturn(getDoneFuture(new Challenge()));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.DELETE_CHALLENGE, true);
  }

  @Test
  public void testConfigureCreateChallengeActionWikiArea() throws Exception {
    // SWC-3876: if tools menu is set up for wiki commands, do not show the Run Challenge command (even
    // in alpha mode)
    currentEntityArea = EntityArea.WIKI;
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    entityBundle.setEntity(new Project());
    AsyncMockStubber
      .callFailureWith(new NotFoundException())
      .when(mockChallengeClient)
      .getChallengeForProject(anyString(), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, false);
    verify(mockActionMenu, never())
      .setActionVisible(Action.CREATE_CHALLENGE, true);
  }

  @Test
  public void testConfigureChallengeFoundNonEditable() throws Exception {
    entityBundle.setEntity(new Project());
    permissions.setCanEdit(false);
    AsyncMockStubber
      .callSuccessWith(new Challenge())
      .when(mockChallengeClient)
      .getChallengeForProject(anyString(), any(AsyncCallback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    // initially hide, never show
    verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, false);
    verify(mockActionMenu, never())
      .setActionVisible(Action.CREATE_CHALLENGE, true);
  }

  @Test
  public void testGetChallengeError() throws Exception {
    entityBundle.setEntity(new Project());
    String error = "an error";
    when(mockSynapseJavascriptClient.getChallengeForProject(anyString()))
      .thenReturn(getFailedFuture(new Exception(error)));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, false);
    verify(mockView).showErrorMessage(error);
  }

  @Test
  public void testFolderDeletionPrompt() {
    /*
     * The user must be shown a confirm dialog before a delete. Confirm is signaled via the
     * Callback.invoke() in this case we do not want to confirm.
     */
    AsyncMockStubber
      .callNoInvovke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    Folder f = new Folder();
    f.setName("Test");
    entityBundle.setEntity(f);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    String display =
      ARE_YOU_SURE_YOU_WANT_TO_DELETE +
      "Folder \"Test\"?" +
      DELETE_FOLDER_EXPLANATION;
    // the call under tests
    controller.onAction(Action.DELETE_ENTITY, null);
    verify(mockView).showConfirmDeleteDialog(eq(display), any(Callback.class));
    // should not make it to the pre-flight check
    verify(mockPreflightController, never())
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
  }

  @Test
  public void testNotFolderDeletionPrompt() {
    /*
     * The user must be shown a confirm dialog before a delete. Confirm is signaled via the
     * Callback.invoke() in this case we do not want to confirm.
     */
    AsyncMockStubber
      .callNoInvovke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    Project p = new Project();
    p.setName("Test");
    entityBundle.setEntity(p);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    String display = ARE_YOU_SURE_YOU_WANT_TO_DELETE + "Project \"Test\"?";
    String folderDisplay = display + DELETE_FOLDER_EXPLANATION;
    // the call under tests
    controller.onAction(Action.DELETE_ENTITY, null);
    verify(mockView).showConfirmDeleteDialog(eq(display), any(Callback.class));
    verify(mockView, times(0))
      .showConfirmDeleteDialog(eq(folderDisplay), any(Callback.class));
    // should not make it to the pre-flight check
    verify(mockPreflightController, never())
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
  }

  @Test
  public void testConfigureManageAccessRequirementsAsNonACTMember() {
    entityBundle.setEntity(new Folder());
    entityBundle.setRootWikiId("7890");
    when(mockIsACTMemberAsyncHandler.isACTActionAvailable())
      .thenReturn(getDoneFuture(false));

    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, false);
    verify(mockActionMenu).setActionVisible(Action.APPROVE_USER_ACCESS, false);
    verify(mockActionMenu, never())
      .setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
    verify(mockIsACTMemberAsyncHandler, atLeastOnce()).isACTActionAvailable();
  }

  @Test
  public void testConfigureManageAccessRequirementsAsACTMemberAsACTMember() {
    entityBundle.setEntity(new Folder());
    entityBundle.setRootWikiId("7890");
    when(mockIsACTMemberAsyncHandler.isACTActionAvailable())
      .thenReturn(getDoneFuture(true));

    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
    verify(mockActionMenu).setActionVisible(Action.APPROVE_USER_ACCESS, true);
    verify(mockActionMenu)
      .setActionListener(Action.MANAGE_ACCESS_REQUIREMENTS, controller);
  }

  @Test
  public void testOnManageAccessRequirements() {
    entityBundle.setEntity(new Folder());
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    controller.onAction(Action.MANAGE_ACCESS_REQUIREMENTS, null);
    verify(mockPlaceChanger).goTo(any(AccessRequirementsPlace.class));
  }

  @Test
  public void testUploadNewFileEntity() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    String folderId = "syn1292";
    Entity parentFolder = new Folder();
    parentFolder.setId(folderId);
    entityBundle.setEntity(parentFolder);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.UPLOAD_FILE, null);
    boolean isEntity = true;
    Entity currentFileEntity = null;
    CallbackP<String> fileHandleIdCallback = null;
    verify(mockUploader)
      .configure(
        DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK,
        currentFileEntity,
        folderId,
        fileHandleIdCallback,
        isEntity
      );
    verify(mockUploader).setUploaderLinkNameVisible(true);
    verify(mockUploader).show();
  }

  @Test
  public void testCreateFolder() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.CREATE_FOLDER, null);
    verify(mockAddFolderDialogWidget).show(entityId);
  }

  @Test
  public void testUploadTable() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkCreateEntityAndUpload(
        any(EntityBundle.class),
        anyString(),
        any(Callback.class)
      );
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.UPLOAD_TABLE, null);
    verify(mockUploadTableModalWidget).configure(entityId, null);
    verify(mockUploadTableModalWidget).showModal(any(WizardCallback.class));
  }

  @Test
  public void testAddTable() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkCreateEntity(
        any(EntityBundle.class),
        anyString(),
        any(Callback.class)
      );
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.ADD_TABLE, null);
    verify(mockCreateTableViewWizard).configure(entityId, TableType.table);
    verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
  }

  @Test
  public void testAddFileView() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkCreateEntity(
        any(EntityBundle.class),
        anyString(),
        any(Callback.class)
      );
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.ADD_FILE_VIEW, null);
    verify(mockCreateTableViewWizard).configure(entityId, TableType.file_view);
    verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
  }

  @Test
  public void testAddProjectView() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkCreateEntity(
        any(EntityBundle.class),
        anyString(),
        any(Callback.class)
      );
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.ADD_PROJECT_VIEW, null);
    verify(mockCreateTableViewWizard)
      .configure(entityId, TableType.project_view);
    verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
  }

  @Test
  public void testCreateExternalDockerRepo() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkCreateEntity(
        any(EntityBundle.class),
        anyString(),
        any(Callback.class)
      );
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.CREATE_EXTERNAL_DOCKER_REPO, null);
    verify(mockAddExternalRepoModal)
      .configuration(eq(entityId), any(Callback.class));
    verify(mockAddExternalRepoModal).show();
  }

  @Test
  public void testDeleteChallengeCancelConfirm() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    controller.onAction(Action.DELETE_CHALLENGE, null);

    verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
    // should not make it to the pre-flight check
    verify(mockPreflightController, never())
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
  }

  @Test
  public void testDeleteChallengeConfirmed() {
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockChallengeClient)
      .deleteChallenge(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    controller.onAction(Action.DELETE_CHALLENGE, null);

    verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
    verify(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    verify(mockChallengeClient)
      .deleteChallenge(anyString(), any(AsyncCallback.class));
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testDeleteChallengeFailure() {
    String error = "unable to delete challenge";
    AsyncMockStubber
      .callFailureWith(new Exception(error))
      .when(mockChallengeClient)
      .deleteChallenge(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockView)
      .showConfirmDeleteDialog(anyString(), any(Callback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    controller.onAction(Action.DELETE_CHALLENGE, null);

    verify(mockChallengeClient)
      .deleteChallenge(anyString(), any(AsyncCallback.class));
    verify(mockView).showErrorMessage(error);
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnToggleFullTextSearch() {
    TableEntity tableEntity = (TableEntity) entityBundle.getEntity();
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callSuccessWith(tableEntity)
      .when(mockSynapseJavascriptClient)
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    assertFalse(tableEntity.getIsSearchEnabled());

    controller.onAction(Action.TOGGLE_FULL_TEXT_SEARCH, null);

    assertTrue(tableEntity.getIsSearchEnabled());
    verify(mockView).showSuccess(anyString());
  }

  @Test
  public void testOnToggleFullTextSearchFailed() {
    String errorMessage = "error during update";
    TableEntity tableEntity = (TableEntity) entityBundle.getEntity();
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    AsyncMockStubber
      .callFailureWith(new Exception(errorMessage))
      .when(mockSynapseJavascriptClient)
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    assertFalse(tableEntity.getIsSearchEnabled());

    controller.onAction(Action.TOGGLE_FULL_TEXT_SEARCH, null);

    assertFalse(tableEntity.getIsSearchEnabled());
    verify(mockView).showErrorMessage(errorMessage);
  }

  @Test
  public void testOnAddMaterializedView() {
    when(mockMaterializedViewEditor.configure(anyString()))
      .thenReturn(mockMaterializedViewEditor);
    AsyncMockStubber
      .callSuccessWith(mockMaterializedView)
      .when(mockSynapseJavascriptClient)
      .createEntity(any(Entity.class), any(AsyncCallback.class));
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkCreateEntity(
        any(EntityBundle.class),
        anyString(),
        any(Callback.class)
      );
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    controller.onAction(Action.ADD_MATERIALIZED_VIEW, null);
    verify(mockMaterializedViewEditor)
      .configure(entityBundle.getEntity().getId());
    verify(mockMaterializedViewEditor).show();
  }

  @Test
  public void testOnEditDefiningSql() {
    String oldSql = "select everything";
    entityBundle.setEntity(mockMaterializedView);
    when(mockMaterializedView.getDefiningSQL()).thenReturn(oldSql);
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));

    controller.onAction(Action.EDIT_DEFINING_SQL, null);

    // user is prompted for the new SQL
    ArgumentCaptor<PromptCallback> callbackCaptor = ArgumentCaptor.forClass(
      PromptCallback.class
    );
    verify(mockView)
      .showPromptDialog(
        anyString(),
        anyString(),
        callbackCaptor.capture(),
        eq(PromptForValuesModalView.InputType.TEXTAREA)
      );
    PromptCallback capturedCallback = callbackCaptor.getValue();
    String newSql = "select nothing";

    capturedCallback.callback(newSql);

    verify(mockMaterializedView).setDefiningSQL(newSql);
    verify(mockSynapseJavascriptClient)
      .updateEntity(
        eq(mockMaterializedView),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testConfigureFileDownloadCanDownload() {
    entityBundle.setEntity(new FileEntity());

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setDownloadMenuEnabled(true);
    verify(mockActionMenu).setDownloadMenuTooltipText("");
    verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_FILE, true);
    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu)
      .setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);

    verify(mockFileDownloadHandlerWidget)
      .configure(mockActionMenu, entityBundle, mockRestrictionInformation);
  }

  @Test
  public void testConfigureFileDownloadCannotDownload() {
    entityBundle.setEntity(new FileEntity());
    entityBundle.getPermissions().setCanDownload(false);

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setDownloadMenuEnabled(false);
    verify(mockActionMenu)
      .setDownloadMenuTooltipText(
        "You don't have download permission. Request access from an administrator, shown under File Tools ➔ File Sharing Settings"
      );
    verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_FILE, true);
    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu)
      .setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);

    verify(mockFileDownloadHandlerWidget)
      .configure(mockActionMenu, entityBundle, mockRestrictionInformation);
  }

  @Test
  public void testConfigureFileDownloadUnauthenticated() {
    entityBundle.setEntity(new FileEntity());
    entityBundle.getPermissions().setCanDownload(false);

    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setDownloadMenuEnabled(false);
    verify(mockActionMenu)
      .setDownloadMenuTooltipText("You need to log in to download this file.");
    verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_FILE, true);
    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu)
      .setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);

    verify(mockFileDownloadHandlerWidget)
      .configure(mockActionMenu, entityBundle, mockRestrictionInformation);
  }

  @Test
  public void testAddFileToDownloadCartHandlerSuccess() {
    FileEntity file = new FileEntity();
    file.setId(entityId);
    file.setName(entityName);
    file.setVersionNumber(3L);
    entityBundle.setEntity(file);

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.ADD_TO_DOWNLOAD_CART),
        actionListenerCaptor.capture()
      );

    // Call under test
    actionListenerCaptor.getValue().onAction(Action.ADD_TO_DOWNLOAD_CART, null);

    verify(mockSynapseJavascriptClient)
      .addFileToDownloadListV2(
        eq(entityId),
        eq(3L),
        addToDownloadListAsyncCallbackCaptor.capture()
      );

    // Call succeeds
    addToDownloadListAsyncCallbackCaptor.getValue().onSuccess(null);
    verify(mockPopupUtils)
      .showInfo(anyString(), anyString(), eq(VIEW_DOWNLOAD_LIST));
    verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
  }

  @Test
  public void testAddFileToDownloadCartHandlerFailure() {
    FileEntity file = new FileEntity();
    file.setId(entityId);
    file.setName(entityName);
    file.setVersionNumber(3L);
    entityBundle.setEntity(file);

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.ADD_TO_DOWNLOAD_CART),
        actionListenerCaptor.capture()
      );

    // Call under test
    actionListenerCaptor.getValue().onAction(Action.ADD_TO_DOWNLOAD_CART, null);

    verify(mockSynapseJavascriptClient)
      .addFileToDownloadListV2(
        eq(entityId),
        eq(3L),
        addToDownloadListAsyncCallbackCaptor.capture()
      );

    // Call fails
    String message = "failure reason";
    addToDownloadListAsyncCallbackCaptor
      .getValue()
      .onFailure(new SynapseClientException(message));
    verify(mockView).showErrorMessage(eq(message));
    verify(mockEventBus, never())
      .fireEvent(any(DownloadListUpdatedEvent.class));
  }

  @Test
  public void testAddFileToDownloadCartHandlerUnauthenticated() {
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);

    FileEntity file = new FileEntity();
    file.setId(entityId);
    file.setName(entityName);
    file.setVersionNumber(3L);
    entityBundle.setEntity(file);

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.ADD_TO_DOWNLOAD_CART),
        actionListenerCaptor.capture()
      );

    // Call under test
    actionListenerCaptor.getValue().onAction(Action.ADD_TO_DOWNLOAD_CART, null);

    verify(mockView).showErrorMessage(anyString());
    verify(mockPlaceChanger).goTo(any(LoginPlace.class));
    verify(mockSynapseJavascriptClient, never())
      .addFileToDownloadListV2(any(), any(), any());
    verify(mockEventBus, never())
      .fireEvent(any(DownloadListUpdatedEvent.class));
  }

  @Test
  public void testFileShowProgrammaticOptionsHandler() {
    FileEntity file = new FileEntity();
    file.setId(entityId);
    file.setName(entityName);
    file.setVersionNumber(3L);
    entityBundle.setEntity(file);

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu)
      .setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.SHOW_PROGRAMMATIC_OPTIONS),
        actionListenerCaptor.capture()
      );

    // Call under test
    actionListenerCaptor
      .getValue()
      .onAction(Action.SHOW_PROGRAMMATIC_OPTIONS, null);

    verify(mockFileClientsHelp).configureAndShow(entityId, 3L);
  }

  @Test
  public void testConfigureContainerDownload() {
    EntityChildrenResponse fileChildrenResponse = new EntityChildrenResponse();
    fileChildrenResponse.setPage(Collections.singletonList(new EntityHeader()));
    when(mockSynapseJavascriptClient.getEntityChildren(any()))
      .thenReturn(getDoneFuture(fileChildrenResponse));

    entityBundle.setEntity(new Project());
    entityBundle.getEntity().setId(entityId);
    entityBundle.setHasChildren(true);
    currentEntityArea = EntityArea.FILES;

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu).setActionEnabled(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu)
      .setActionTooltipText(Action.ADD_TO_DOWNLOAD_CART, null);
    verify(mockActionMenu)
      .setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);
    verify(mockActionMenu).setDownloadMenuEnabled(true);
    verify(mockActionMenu).setDownloadMenuTooltipText(null);

    // Test the action listeners
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.ADD_TO_DOWNLOAD_CART),
        actionListenerCaptor.capture()
      );
    actionListenerCaptor.getValue().onAction(Action.ADD_TO_DOWNLOAD_CART, null);
    verify(mockAddToDownloadListWidget).configure(entityId);

    verify(mockActionMenu)
      .setActionListener(
        eq(Action.SHOW_PROGRAMMATIC_OPTIONS),
        actionListenerCaptor.capture()
      );
    actionListenerCaptor
      .getValue()
      .onAction(Action.SHOW_PROGRAMMATIC_OPTIONS, null);
    verify(mockContainerClientsHelp).configureAndShow(entityId);
  }

  @Test
  public void testConfigureContainerDownloadNoChildren() {
    entityBundle.setEntity(new Folder());
    entityBundle.getEntity().setId(entityId);
    entityBundle.setHasChildren(false);

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setDownloadMenuEnabled(false);
    verify(mockActionMenu)
      .setDownloadMenuTooltipText(
        "There are no downloadable items in this folder."
      );

    // No need to see if there are any files
    verify(mockSynapseJavascriptClient, never()).getEntityChildren(any());
  }

  @Test
  public void testConfigureContainerDownloadNoFileChildren() {
    EntityChildrenResponse fileChildrenResponse = new EntityChildrenResponse();
    fileChildrenResponse.setPage(Collections.emptyList());
    when(mockSynapseJavascriptClient.getEntityChildren(any()))
      .thenReturn(getDoneFuture(fileChildrenResponse));

    entityBundle.setEntity(new Project());
    entityBundle.getEntity().setId(entityId);
    entityBundle.setHasChildren(true);
    currentEntityArea = EntityArea.FILES;

    // Call under test
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.ADD_TO_DOWNLOAD_CART, true);
    verify(mockActionMenu).setActionEnabled(Action.ADD_TO_DOWNLOAD_CART, false);
    verify(mockActionMenu)
      .setActionTooltipText(
        Action.ADD_TO_DOWNLOAD_CART,
        "There are no files in this folder."
      );
    verify(mockActionMenu)
      .setActionVisible(Action.SHOW_PROGRAMMATIC_OPTIONS, true);
    verify(mockActionMenu).setDownloadMenuEnabled(true);
    verify(mockActionMenu).setDownloadMenuTooltipText(null);
  }

  @Test
  public void testReportViolation() {
    String url = "https://www.synapse.org/#!Synapse:syn123";
    String ownerId = "4958725";
    String email = "email@address.com";
    String firstName = "Synapse";
    String lastName = "User";
    String username = "SynUser123";
    UserProfile profile = new UserProfile();
    profile.setOwnerId(ownerId);
    profile.setFirstName(firstName);
    profile.setLastName(lastName);
    profile.setUserName(username);
    profile.setEmails(Collections.singletonList(email));
    when(mockAuthenticationController.getCurrentUserProfile())
      .thenReturn(profile);
    when(mockGWT.getCurrentURL()).thenReturn(url);
    entityBundle.setEntity(new FileEntity());
    entityBundle.getEntity().setId(entityId);

    // Call under test - configuration
    controller.configure(
      mockActionMenu,
      entityBundle,
      true,
      wikiPageId,
      currentEntityArea,
      mockAddToDownloadListWidget
    );

    verify(mockActionMenu).setActionVisible(Action.REPORT_VIOLATION, true);
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.REPORT_VIOLATION),
        actionListenerCaptor.capture()
      );

    // Call under test - invocation
    actionListenerCaptor.getValue().onAction(Action.REPORT_VIOLATION, null);
    verify(mockJsniUtils)
      .showJiraIssueCollector(
        "", // summary
        FLAG_ISSUE_DESCRIPTION_PART_1 +
        url +
        WebConstants.FLAG_ISSUE_DESCRIPTION_PART_2, // description
        FLAG_ISSUE_COLLECTOR_URL,
        ownerId,
        DisplayUtils.getDisplayName(firstName, lastName, username),
        email,
        entityId, // Synapse data object ID
        REVIEW_DATA_REQUEST_COMPONENT_ID,
        null, // Access requirement ID
        FLAG_ISSUE_PRIORITY
      );
  }
}
