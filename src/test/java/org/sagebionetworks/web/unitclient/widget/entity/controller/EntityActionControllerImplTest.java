package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.constants.IconType;
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
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.UserProfileClientAsync;
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
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerView;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidget;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.CallbackMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
	EntityFinder mockEntityFinder;
	@Mock
	EvaluationSubmitter mockSubmitter;
	@Mock
	UploadDialogWidget mockUploader;
	@Mock
	ActionMenuWidget mockActionMenu;
	@Mock
	EventBus mockEventBus;
	EntityBundle entityBundle;
	UserEntityPermissions permissions;

	EntityActionControllerImpl controller;
	String parentId;
	String entityId;
	String currentUserId = "12344321";
	String wikiPageId = "999";
	@Mock
	WikiMarkdownEditor mockMarkdownEditorWidget;
	@Mock
	ProvenanceEditorWidget mockProvenanceEditorWidget;
	@Mock
	StorageLocationWidget mockStorageLocationWidget;
	Reference selected;
	List<AccessRequirement> accessReqs;
	@Mock
	EvaluationEditorModal mockEvalEditor;
	@Mock
	CookieProvider mockCookies;
	
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
	@Mock
	UserBundle mockUserBundle;
	@Mock
	Throwable mockThrowable;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackPCaptor;
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
	Set<ResourceAccess> resourceAccessSet;
	
	public static final String SELECTED_TEAM_ID = "987654";
	public static final long PUBLIC_USER_ID = 77772L;
	
	EntityArea currentEntityArea;
	
	@Before
	public void before() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		
		when(mockPortalGinInjector.getSynapseProperties()).thenReturn(mockSynapseProperties);
		when(mockPortalGinInjector.getAccessControlListModalWidget()).thenReturn(mockAccessControlListModalWidget);
		when(mockPortalGinInjector.getRenameEntityModalWidget()).thenReturn(mockRenameEntityModalWidget);
		when(mockPortalGinInjector.getEditFileMetadataModalWidget()).thenReturn(mockEditFileMetadataModalWidget);
		when(mockPortalGinInjector.getEditProjectMetadataModalWidget()).thenReturn(mockEditProjectMetadataModalWidget);
		when(mockPortalGinInjector.getEntityFinder()).thenReturn(mockEntityFinder);
		when(mockPortalGinInjector.getUploadDialogWidget()).thenReturn(mockUploader);
		when(mockPortalGinInjector.getWikiMarkdownEditor()).thenReturn(mockMarkdownEditorWidget);
		when(mockPortalGinInjector.getProvenanceEditorWidget()).thenReturn(mockProvenanceEditorWidget);
		when(mockPortalGinInjector.getStorageLocationWidget()).thenReturn(mockStorageLocationWidget);
		when(mockPortalGinInjector.getEvaluationEditorModal()).thenReturn(mockEvalEditor);
		when(mockPortalGinInjector.getSelectTeamModal()).thenReturn(mockSelectTeamModal);
		when(mockPortalGinInjector.getApproveUserAccessModal()).thenReturn(mockApproveUserAccessModal);
		when(mockPortalGinInjector.getChallengeClientAsync()).thenReturn(mockChallengeClient);
		when(mockPortalGinInjector.getSynapseClientAsync()).thenReturn(mockSynapseClient);
		when(mockPortalGinInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		when(mockPortalGinInjector.getEvaluationSubmitter()).thenReturn(mockSubmitter);
		when(mockSynapseProperties.getPublicPrincipalIds()).thenReturn(mockPublicPrincipalIds);
		when(mockPortalGinInjector.getSynapseJavascriptClient()).thenReturn(mockSynapseJavascriptClient);
		when(mockPortalGinInjector.getCreateTableViewWizard()).thenReturn(mockCreateTableViewWizard);
		when(mockPortalGinInjector.getUploadTableModalWidget()).thenReturn(mockUploadTableModalWidget);
		when(mockPortalGinInjector.getAddExternalRepoModal()).thenReturn(mockAddExternalRepoModal);
		when(mockPortalGinInjector.getAddFolderDialogWidget()).thenReturn(mockAddFolderDialogWidget);
		// The controller under test.
		controller = new EntityActionControllerImpl(mockView,
				mockPreflightController,
				mockPortalGinInjector,
				mockAuthenticationController, 
				mockCookies,
				mockIsACTMemberAsyncHandler,
				mockGWT,
				mockEventBus);
		
		parentId = "syn456";
		entityId = "syn123";
		accessReqs = new LinkedList<AccessRequirement>();
		Entity table = new TableEntity();
		table.setId(entityId);
		table.setParentId(parentId);
		permissions = new UserEntityPermissions();
		permissions.setCanChangePermissions(true);
		permissions.setCanDelete(true);
		permissions.setCanPublicRead(true);
		permissions.setCanUpload(true);
		permissions.setCanAddChild(true);
		permissions.setCanEdit(true);
		permissions.setCanCertifiedUserEdit(true);
		permissions.setCanChangeSettings(true);
		entityBundle = new EntityBundle();
		entityBundle.setEntity(table);
		entityBundle.setPermissions(permissions);
		entityBundle.setDoi(new Doi());
		entityBundle.setDoiAssociation(new DoiAssociation());
		entityBundle.setAccessRequirements(accessReqs);
		entityBundle.setBenefactorAcl(mockACL);
		resourceAccessSet = new HashSet<>();
		when(mockACL.getResourceAccess()).thenReturn(resourceAccessSet);
		when(mockPublicPrincipalIds.isPublic(PUBLIC_USER_ID)).thenReturn(true);
		selected = new Reference();
		selected.setTargetId("syn9876");
		// Setup the mock entity selector to select an entity.
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation)
					throws Throwable {
				SelectedHandler<Reference> handler = (SelectedHandler<Reference>) invocation.getArguments()[2];
				handler.onSelected(selected);
				return null;
			}
		}).when(mockEntityFinder).configure(any(EntityFilter.class), anyBoolean(), any(SelectedHandler.class));
		currentEntityArea = null;
		CallbackMockStubber.invokeCallback().when(mockGWT).scheduleExecution(any(Callback.class), anyInt());
	}

	@Test
	public void testConfigure(){
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		// delete
		verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, true);
		verify(mockActionMenu).setActionText(Action.DELETE_ENTITY, DELETE_PREFIX+EntityTypeUtils.getDisplayName(EntityType.table));
		verify(mockActionMenu).setActionListener(Action.DELETE_ENTITY, controller);
		// share
		verify(mockActionMenu).setActionVisible(Action.SHARE, true);
		verify(mockActionMenu).setActionListener(Action.SHARE, controller);
		// rename
		verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, true);
		verify(mockActionMenu).setActionText(Action.CHANGE_ENTITY_NAME, RENAME_PREFIX+EntityTypeUtils.getDisplayName(EntityType.table));
		verify(mockActionMenu).setActionListener(Action.CHANGE_ENTITY_NAME, controller);
		// upload
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
		// file history
		verify(mockActionMenu).setActionVisible(Action.SHOW_FILE_HISTORY, false);
	}
	
	@Test
	public void testConfigureDockerRepo(){
		//verify unable to rename or move docker repo entity name
		entityBundle.setEntity(new DockerRepository());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, false);
		verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, false);
		verify(mockActionMenu).setToolsButtonIcon("Docker Repository Tools", IconType.GEAR);
	}
	
	private void setPublicCanRead() {
		ResourceAccess ra = new ResourceAccess();
		ra.setAccessType(Collections.singleton(ACCESS_TYPE.READ));
		ra.setPrincipalId(PUBLIC_USER_ID);
		resourceAccessSet.add(ra);
	}
	
	@Test
	public void testConfigureProjectLevelTableCommandsCanEdit(){
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.TABLES;
		boolean canCertifiedUserEdit = true;
		permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE, canCertifiedUserEdit);
		verify(mockActionMenu).setActionListener(Action.UPLOAD_TABLE, controller);
		verify(mockActionMenu).setActionVisible(Action.ADD_TABLE, canCertifiedUserEdit);
		verify(mockActionMenu).setActionListener(Action.ADD_TABLE, controller);
		verify(mockActionMenu).setActionVisible(Action.ADD_FILE_VIEW, canCertifiedUserEdit);
		verify(mockActionMenu).setActionListener(Action.ADD_FILE_VIEW, controller);
		verify(mockActionMenu).setActionVisible(Action.ADD_PROJECT_VIEW, canCertifiedUserEdit);
		verify(mockActionMenu).setActionListener(Action.ADD_PROJECT_VIEW, controller);
		verify(mockActionMenu).setToolsButtonIcon("Tables Tools", IconType.GEAR);
	}
	@Test
	public void testConfigureProjectLevelTableCommandsCannotEdit(){
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.TABLES;
		boolean canCertifiedUserEdit = false;
		permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE, canCertifiedUserEdit);
		verify(mockActionMenu).setActionVisible(Action.ADD_TABLE, canCertifiedUserEdit);
		verify(mockActionMenu).setActionVisible(Action.ADD_FILE_VIEW, canCertifiedUserEdit);
		verify(mockActionMenu).setActionVisible(Action.ADD_PROJECT_VIEW, canCertifiedUserEdit);
	}
	@Test
	public void testConfigureProjectLevelTableCommandsCanEditNotOnTablesTab(){
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.FILES;
		boolean canCertifiedUserEdit = true;
		permissions.setCanCertifiedUserEdit(canCertifiedUserEdit);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE, false);
		verify(mockActionMenu).setActionVisible(Action.ADD_TABLE, false);
		verify(mockActionMenu).setActionVisible(Action.ADD_FILE_VIEW, false);
		verify(mockActionMenu).setActionVisible(Action.ADD_PROJECT_VIEW, false);
		verify(mockActionMenu).setToolsButtonIcon("Files Tools", IconType.GEAR);
	}
	
	@Test
	public void testConfigureReorderWikiSubpagesWithTree(){
		List<V2WikiHeader> headers = new ArrayList<>();
		V2WikiHeader page = new V2WikiHeader();
		page.setId("rootid");
		headers.add(page);
		page = new V2WikiHeader();
		page.setId("page 1");
		page.setTitle("page 1 title");
		page.setParentId("rootid");
		headers.add(page);
		
		AsyncMockStubber.callSuccessWith(headers).when(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.WIKI;
		boolean canEdit = true;
		permissions.setCanEdit(canEdit);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.REORDER_WIKI_SUBPAGES, true);
	}

	@Test
	public void testConfigureReorderWikiSubpagesNoTree(){
		AsyncMockStubber.callSuccessWith(new ArrayList<V2WikiHeader>()).when(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.WIKI;
		boolean canEdit = true;
		permissions.setCanEdit(canEdit);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
	}
	
	@Test
	public void testConfigureReorderWikiSubpagesNoEdit(){
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.WIKI;
		boolean canEdit = false;
		permissions.setCanEdit(canEdit);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
	}
	@Test
	public void testConfigureReorderWikiSubpagesNotOnProject(){
		entityBundle.setEntity(new Folder());
		currentEntityArea = null;
		boolean canEdit = true;
		permissions.setCanEdit(canEdit);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
	}

	
	@Test
	public void testConfigurePublicReadTable(){
		setPublicCanRead();
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		verify(mockActionMenu).setActionIcon(Action.SHARE, IconType.GLOBE);
		verify(mockActionMenu).setActionVisible(Action.SHOW_ANNOTATIONS, true);
		// for a table entity, do not show file history
		verify(mockActionMenu).setActionVisible(Action.SHOW_FILE_HISTORY, false);
		
		//verify other table commands.  current user canCertifiedUserEdit
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
		verify(mockActionMenu).setActionVisible(Action.SHOW_TABLE_SCHEMA, true);
		verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
	}
	
	@Test
	public void testConfigureTableNoEdit(){
		setPublicCanRead();
		permissions.setCanCertifiedUserEdit(false);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		// for a table entity, do not show file history
		verify(mockActionMenu).setActionVisible(Action.SHOW_FILE_HISTORY, false);
		
		//verify other table commands. the current user cannot edit
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
		verify(mockActionMenu).setActionVisible(Action.SHOW_TABLE_SCHEMA, true);
		verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
		verify(mockActionMenu).setToolsButtonIcon("Table Tools", IconType.GEAR);
	}
	
	@Test
	public void testConfigurePublicReadFile(){
		setPublicCanRead();
		Entity file = new FileEntity();
		file.setId(entityId);
		file.setParentId(parentId);
		entityBundle.setEntity(file);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionIcon(Action.SHARE, IconType.GLOBE);
		verify(mockActionMenu).setActionVisible(Action.SHOW_ANNOTATIONS, true);
		// for a table entity, do not show file history
		verify(mockActionMenu).setActionVisible(Action.SHOW_FILE_HISTORY, true);
		
		//table commands not shown for File
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu).setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, false);
		verify(mockActionMenu).setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
		verify(mockActionMenu).setActionVisible(Action.SHOW_VIEW_SCOPE, false);
	}
	
	@Test
	public void testConfigureNotPublicIsLoggedIn(){
		entityBundle.getPermissions().setCanPublicRead(false);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionIcon(Action.SHARE, IconType.LOCK);
	}
	
	@Test
	public void testConfigureFileHistory() {
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
		entityBundle = new EntityBundle();
		entityBundle.setEntity(file);
		entityBundle.setPermissions(permissions);
		entityBundle.setAccessRequirements(accessReqs);
		entityBundle.setBenefactorAcl(mockACL);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.SHOW_FILE_HISTORY, true);
	}
	
	@Test
	public void testConfigureNoWiki(){
		entityBundle.setEntity(new Project());
		entityBundle.setRootWikiId(null);
		currentEntityArea = EntityArea.WIKI;
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, true);
		verify(mockActionMenu).setActionListener(Action.EDIT_WIKI_PAGE, controller);
		verify(mockActionMenu).setActionText(Action.EDIT_WIKI_PAGE, EDIT_WIKI_PREFIX+EntityTypeUtils.getDisplayName(EntityType.project)+WIKI);
		verify(mockActionMenu).setToolsButtonIcon("Wiki Tools", IconType.GEAR);
	}
	
	@Test
	public void testConfigureWiki(){
		entityBundle.setEntity(new Folder());
		entityBundle.setRootWikiId("7890");
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, true);
		verify(mockActionMenu).setActionListener(Action.EDIT_WIKI_PAGE, controller);
		verify(mockActionMenu).setActionText(Action.EDIT_WIKI_PAGE, EDIT_WIKI_PREFIX+EntityTypeUtils.getDisplayName(EntityType.folder)+WIKI);
		verify(mockActionMenu).setToolsButtonIcon("Folder Tools", IconType.GEAR);
	}
	
	@Test
	public void testConfigureWikiCannotEdit(){
		entityBundle.setEntity(new Folder());
		entityBundle.setRootWikiId("7890");
		permissions.setCanEdit(false);
		controller.configure(mockActionMenu, entityBundle,true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, false);
		verify(mockActionMenu).setActionListener(Action.EDIT_WIKI_PAGE, controller);
	}
	
	@Test
	public void testConfigureDeleteWiki(){
		entityBundle.setEntity(new Project());
		entityBundle.setRootWikiId("7890");
		currentEntityArea = EntityArea.WIKI;
		controller.configure(mockActionMenu, entityBundle,true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.DELETE_WIKI_PAGE, true);
		verify(mockActionMenu).setActionListener(Action.DELETE_WIKI_PAGE, controller);
	}
	
	@Test
	public void testConfigureDeleteWikiCannotDelete(){
		entityBundle.setEntity(new Project());
		entityBundle.setRootWikiId("7890");
		permissions.setCanDelete(false);
		currentEntityArea = EntityArea.WIKI;
		controller.configure(mockActionMenu, entityBundle,true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.DELETE_WIKI_PAGE, false);
		verify(mockActionMenu).setActionListener(Action.DELETE_WIKI_PAGE, controller);
	}
	
	@Test
	public void testConfigureDeleteWikiFolder(){
		entityBundle.setEntity(new Folder());
		entityBundle.setRootWikiId("7890");
		controller.configure(mockActionMenu, entityBundle,true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.DELETE_WIKI_PAGE, false);
	}
	
	@Test
	public void testConfigureWikiNoWikiTable(){
		entityBundle.setEntity(new TableEntity());
		entityBundle.setRootWikiId(null);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, false);
	}
	
	@Test
	public void testConfigureWikiNoWikiView(){
		entityBundle.setEntity(new EntityView());
		entityBundle.setRootWikiId(null);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, false);
	}
	

	@Test
	public void testConfigureViewWikiSource(){
		entityBundle.setEntity(new Folder());
		entityBundle.setRootWikiId("7890");
		controller.configure(mockActionMenu, entityBundle,true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, false);
		verify(mockActionMenu).setActionListener(Action.VIEW_WIKI_SOURCE, controller);
	}
	
	@Test
	public void testConfigureViewWikiSourceCannotEdit(){
		entityBundle.setEntity(new Folder());
		entityBundle.setRootWikiId("7890");
		permissions.setCanEdit(false);
		controller.configure(mockActionMenu, entityBundle,true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, true);
		verify(mockActionMenu).setActionListener(Action.VIEW_WIKI_SOURCE, controller);
	}
	
	@Test
	public void testConfigureViewWikiSourceWikiTable(){
		entityBundle.setEntity(new TableEntity());
		entityBundle.setRootWikiId("22");
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, false);
	}
	
	@Test
	public void testConfigureViewWikiSourceWikiView(){
		entityBundle.setEntity(new EntityView());
		entityBundle.setRootWikiId("22");
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.VIEW_WIKI_SOURCE, false);
	}

	@Test
	public void testConfigureAddEvaluationProjectSettings(){
		currentEntityArea = null;
		entityBundle.setEntity(new Project());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_EVALUATION_QUEUE, false);
	}
	
	@Test
	public void testConfigureAddEvaluationOnChallengeTab(){
		currentEntityArea = EntityArea.CHALLENGE;
		entityBundle.setEntity(new Project());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_EVALUATION_QUEUE, true);
	}
	
	@Test
	public void testConfigureAddEvaluationInAlphaNotProject(){
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		entityBundle.setEntity(new FileEntity());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_EVALUATION_QUEUE, false);
	}
	
	@Test
	public void testConfigureMoveTable(){
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, true);
	}
	
	@Test
	public void testConfigureMoveProject(){
		entityBundle.setEntity(new Project());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, false);
	}
	
	@Test
	public void testConfigureMove(){
		entityBundle.setEntity(new Folder());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, true);
		verify(mockActionMenu).setActionText(Action.MOVE_ENTITY, MOVE_PREFIX+EntityTypeUtils.getDisplayName(EntityType.folder));
		verify(mockActionMenu).setActionListener(Action.MOVE_ENTITY, controller);
	}
	
	@Test
	public void testConfigureUploadNewFile(){
		entityBundle.setEntity(new FileEntity());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, true);
		verify(mockActionMenu).setActionListener(Action.UPLOAD_NEW_FILE, controller);
	}
	
	
	@Test
	public void testConfigureUploadNewFileNoUpload(){
		entityBundle.getPermissions().setCanCertifiedUserEdit(false);
		entityBundle.setEntity(new FileEntity());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.UPLOAD_NEW_FILE, false);
		verify(mockActionMenu).setActionListener(Action.UPLOAD_NEW_FILE, controller);
	}
	
	@Test
	public void testConfigureProvenanceFileCanEdit(){
		boolean canEdit = true;
		entityBundle.getPermissions().setCanEdit(canEdit);
		entityBundle.setEntity(new FileEntity());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
		verify(mockActionMenu).setActionListener(Action.EDIT_PROVENANCE, controller);
	}
	
	@Test
	public void testConfigureProvenanceFileCannotEdit(){
		boolean canEdit = false;
		entityBundle.getPermissions().setCanEdit(canEdit);
		entityBundle.setEntity(new FileEntity());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
		verify(mockActionMenu).setActionListener(Action.EDIT_PROVENANCE, controller);
	}

	@Test
	public void testConfigureProvenanceDockerCanEdit(){
		boolean canEdit = true;
		entityBundle.getPermissions().setCanEdit(canEdit);
		entityBundle.setEntity(new DockerRepository());
		currentEntityArea = EntityArea.DOCKER;
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
		verify(mockActionMenu).setActionListener(Action.EDIT_PROVENANCE, controller);
	}
	
	@Test
	public void testConfigureProvenanceDockerCannotEdit(){
		boolean canEdit = false;
		entityBundle.getPermissions().setCanEdit(canEdit);
		entityBundle.setEntity(new DockerRepository());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
		verify(mockActionMenu).setActionListener(Action.EDIT_PROVENANCE, controller);
	}
	
	@Test
	public void testConfigureProvenanceNonFileNorDocker(){
		entityBundle.setEntity(new Folder());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, false);
	}
	
	@Test
	public void testOnSelectApproveUserAccess(){
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.APPROVE_USER_ACCESS);
		verify(mockApproveUserAccessModal).configure(entityBundle);
		verify(mockApproveUserAccessModal).show();
	}
	
	@Test
	public void testOnEditProvenance(){
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.EDIT_PROVENANCE);
		verify(mockProvenanceEditorWidget).configure(entityBundle);
		verify(mockProvenanceEditorWidget).show();
	}

	@Test
	public void testOnDeleteConfirmCancel(){
		/*
		 *  The user must be shown a confirm dialog before a delete.  Confirm is signaled via the Callback.invoke()
		 *  in this case we do not want to confirm.
		 */
		AsyncMockStubber.callNoInvovke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// the call under tests
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		// should not make it to the pre-flight check
		verify(mockPreflightController, never()).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
	}
	
	@Test
	public void testOnDeleteConfirmedPreFlightFailed(){
		// confirm the delete
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		/*
		 * The preflight check is confirmed by calling Callback.invoke(), in this case it must not be invoked.
		 */
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// the call under test
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		verify(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		// Must not make it to the actual delete since preflight failed.
		verify(mockSynapseJavascriptClient, never()).deleteEntityById(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testOnDeleteConfirmedPreFlightPassedDeleteFailed(){
		// confirm the delete
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		// confirm pre-flight
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		String error = "some error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseJavascriptClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// the call under test
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		verify(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		// an attempt to delete should be made
		verify(mockSynapseJavascriptClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);
	}
	
	@Test
	public void testOnDeleteConfirmedPreFlightPassedDeleteSuccess(){
		// confirm the delete
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		// confirm pre-flight
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// the call under test
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		verify(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		// an attempt to delete should be made
		verify(mockSynapseJavascriptClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(THE + EntityTypeUtils.getDisplayName(EntityType.table) + WAS_SUCCESSFULLY_DELETED);
		verify(mockPlaceChanger).goTo(new Synapse(parentId, null, EntityArea.TABLES, null));
	}
	
	@Test
	public void testCreateDeletePlaceNullParentId(){
		entityBundle.getEntity().setParentId(null);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// call under test
		Place result =controller.createDeletePlace();
		assertTrue(result instanceof Profile);
		assertEquals(currentUserId, ((Profile)result).getUserId());
	}
	
	@Test
	public void testCreateDeletePlaceProject(){
		// setup a project
		Entity project = new Project();
		project.setId(entityId);
		project.setParentId(parentId);
		entityBundle = new EntityBundle();
		entityBundle.setEntity(project);
		entityBundle.setPermissions(permissions);
		entityBundle.setAccessRequirements(accessReqs);
		entityBundle.setBenefactorAcl(mockACL);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// call under test
		Place result = controller.createDeletePlace();
		assertTrue(result instanceof Profile);
		assertEquals(currentUserId, ((Profile)result).getUserId());
	}
	
	@Test
	public void testCreateDeletePlaceFile(){
		// setup a project
		Entity file = new FileEntity();
		file.setId(entityId);
		file.setParentId(parentId);
		entityBundle.setEntity(file);
		entityBundle.setPermissions(entityBundle.getPermissions());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// call under test
		Place result = controller.createDeletePlace();
		Place expected = new Synapse(parentId, null, EntityArea.FILES, null);
		assertEquals(expected, result);
	}

	@Test
	public void testCreateDeletePlaceDocker(){
		Entity docker = new DockerRepository();
		docker.setId(entityId);
		docker.setParentId(parentId);
		entityBundle.setEntity(docker);
		entityBundle.setPermissions(entityBundle.getPermissions());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// call under test
		Place result = controller.createDeletePlace();
		Place expected = new Synapse(parentId, null, EntityArea.DOCKER, null);
		assertEquals(expected, result);
	}
	
	@Test
	public void testOnShareNoChange(){
		/*
		 * Share change is confirmed by calling Callback.invoke(), in this case it must not be invoked.
		 */
		AsyncMockStubber.callNoInvovke().when(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.SHARE);
		verify(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		verify(mockAccessControlListModalWidget).configure(any(Entity.class), anyBoolean());
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnShareWithChange(){
		// invoke this time
		AsyncMockStubber.callWithInvoke().when(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.SHARE);
		verify(mockAccessControlListModalWidget).configure(any(Entity.class), anyBoolean());
		verify(mockAccessControlListModalWidget).showSharing(any(Callback.class));
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testRenameHappy(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callWithInvoke().when(mockRenameEntityModalWidget).onRename(any(Entity.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.CHANGE_ENTITY_NAME);
		verify(mockRenameEntityModalWidget).onRename(any(Entity.class), any(Callback.class));
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testRenameNoChange(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callNoInvovke().when(mockRenameEntityModalWidget).onRename(any(Entity.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.CHANGE_ENTITY_NAME);
		verify(mockRenameEntityModalWidget).onRename(any(Entity.class), any(Callback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testRenameFailedPreFlight(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callNoInvovke().when(mockRenameEntityModalWidget).onRename(any(Entity.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.CHANGE_ENTITY_NAME);
		verify(mockRenameEntityModalWidget, never()).onRename(any(Entity.class), any(Callback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	

	@Test
	public void testIsRenameOnly(){
		assertFalse(controller.isRenameOnly(new FileEntity()));
		assertTrue(controller.isRenameOnly(new TableEntity()));
		assertFalse(controller.isRenameOnly(new Project()));
	}
	
	@Test
	public void testEditFileMetadataIsCurrent(){
		Entity file = new FileEntity();
		file.setId(entityId);
		file.setParentId(parentId);
		entityBundle.setEntity(file);
		
		// Most current version represented by null
		Synapse currentPlace = new Synapse(currentUserId, null, null, currentUserId);
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(currentPlace);
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callNoInvovke().when(mockEditFileMetadataModalWidget).configure(any(FileEntity.class), any(FileHandle.class), any(Callback.class));
		
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.EDIT_FILE_METADATA);
		verify(mockEditFileMetadataModalWidget).configure(any(FileEntity.class), any(FileHandle.class), any(Callback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testEditFileMetadataIsNotCurrent(){
		FileEntity file = new FileEntity();
		file.setId(entityId);
		file.setParentId(parentId);
		file.setVersionNumber(1L);
		entityBundle.setEntity(file);
		// currentPlace returns a non-null versionNumber
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callNoInvovke().when(mockEditFileMetadataModalWidget).configure(any(FileEntity.class), any(FileHandle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, false, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.EDIT_FILE_METADATA);
		verify(mockEditFileMetadataModalWidget, never()).configure(any(FileEntity.class), any(FileHandle.class), any(Callback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
		verify(mockView).showErrorMessage("Can only edit the metadata of the most recent file version.");
	}
	
	@Test
	public void testEditProjectMetadata(){
		Entity project = new Project();
		project.setId(entityId);
		project.setParentId(parentId);
		entityBundle.setEntity(project);
		
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callNoInvovke().when(mockEditProjectMetadataModalWidget).configure(any(Project.class), anyBoolean(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.EDIT_PROJECT_METADATA);
		verify(mockEditProjectMetadataModalWidget).configure(any(Project.class), anyBoolean(), any(Callback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testEditProjectMetadataFailedPreFlight(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callNoInvovke().when(mockEditProjectMetadataModalWidget).configure(any(Project.class), anyBoolean(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		// method under test
		controller.onAction(Action.EDIT_PROJECT_METADATA);
		verify(mockEditProjectMetadataModalWidget, never()).configure(any(Project.class), anyBoolean(), any(Callback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnAddWikiNoUpdate(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		entityBundle.setRootWikiId(null);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.EDIT_WIKI_PAGE);
		verify(mockSynapseClient, never()).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnAddWikiCanUpdate(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		AsyncMockStubber.callSuccessWith(new WikiPage()).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		entityBundle.setRootWikiId(null);
		controller.configure(mockActionMenu, entityBundle, true, null, currentEntityArea);
		controller.onAction(Action.EDIT_WIKI_PAGE);
		verify(mockMarkdownEditorWidget).configure(any(WikiPageKey.class), any(CallbackP.class));
	}
	
	@Test
	public void testOnViewWikiSource(){
		WikiPage page = new WikiPage();
		String markdown = "hello markdown";
		page.setMarkdown(markdown);
		AsyncMockStubber.callSuccessWith(page).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class),any(AsyncCallback.class));
		entityBundle.setRootWikiId("111");
		controller.configure(mockActionMenu, entityBundle, true, null, currentEntityArea);
		controller.onAction(Action.VIEW_WIKI_SOURCE);
		verify(mockView).showInfoDialog(anyString(), eq(markdown));
	}
	
	@Test
	public void testOnViewWikiSourceError(){
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class),any(AsyncCallback.class));
		entityBundle.setRootWikiId("111");
		controller.configure(mockActionMenu, entityBundle, true, null, currentEntityArea);
		controller.onAction(Action.VIEW_WIKI_SOURCE);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testIsMovableType(){
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
		assertTrue(controller.isTopLevelProjectToolsMenu(new Project(), EntityArea.FILES));
		assertTrue(controller.isTopLevelProjectToolsMenu(new Project(), EntityArea.DISCUSSION));
		// if the area is not set, then it's the Project Settings) 
		assertFalse(controller.isTopLevelProjectToolsMenu(new Project(), null));
		// if looking at a specific child (file/table/...), then it is not a top level project tools menu for that area.
		assertFalse(controller.isTopLevelProjectToolsMenu(new FileEntity(), null));
	}

	@Test
	public void testIsContainerOnFilesTab() {
		// for commands like upload.  can be used on a Folder, or when looking at a Project on the Files tab (root file/folder container).
		assertTrue(controller.isContainerOnFilesTab(new Project(), EntityArea.FILES));
		assertTrue(controller.isContainerOnFilesTab(new Folder(), null));
		assertFalse(controller.isContainerOnFilesTab(new Project(), EntityArea.WIKI));
		assertFalse(controller.isContainerOnFilesTab(new Project(), EntityArea.DISCUSSION));
	}
	
	@Test
	public void testIsWikableConfig(){
		//if entity is a project, must be in Wiki area for wiki commands to show up in the tools menu. 
		assertTrue(controller.isWikiableConfig(new Project(), EntityArea.WIKI));
		assertFalse(controller.isWikiableConfig(new Project(), EntityArea.TABLES));
		assertFalse(controller.isWikiableConfig(new Project(), EntityArea.FILES));
		assertFalse(controller.isWikiableConfig(new TableEntity(), null));
		assertTrue(controller.isWikiableConfig(new FileEntity(), null));
	}
	
	@Test
	public void testIsWikableType(){
		assertTrue(controller.isWikiableType(new Project()));
		assertFalse(controller.isWikiableType(new TableEntity()));
		assertTrue(controller.isWikiableType(new FileEntity()));
		assertTrue(controller.isWikiableType(new Folder()));
		assertFalse(controller.isWikiableType(new Link()));
	}
	
	@Test
	public void testIsLinkType(){
		assertTrue(controller.isLinkType(new Project()));
		assertTrue(controller.isLinkType(new TableEntity()));
		assertTrue(controller.isLinkType(new FileEntity()));
		assertTrue(controller.isLinkType(new Folder()));
		assertFalse(controller.isLinkType(new Link()));
	}
	
	@Test
	public void testIsSubmittableType(){
		assertFalse(controller.isSubmittableType(new Project()));
		assertFalse(controller.isSubmittableType(new TableEntity()));
		assertTrue(controller.isSubmittableType(new FileEntity()));
		assertTrue(controller.isSubmittableType(new DockerRepository()));
		assertFalse(controller.isSubmittableType(new Folder()));
		assertFalse(controller.isSubmittableType(new Link()));
	}
	
	@Test
	public void testOnMoveNoUpdate(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		entityBundle.setEntity(new Folder());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.MOVE_ENTITY);
		verify(mockEntityFinder, never()).configure(anyBoolean(), any(SelectedHandler.class));
		verify(mockEntityFinder, never()).show();
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnMoveCanUpdateFailed(){
		String error = "An error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).moveEntity(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		entityBundle.setEntity(new Folder());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.MOVE_ENTITY);
		verify(mockEntityFinder).configure(eq(EntityFilter.CONTAINER), anyBoolean(), any(SelectedHandler.class));
		verify(mockEntityFinder).show();
		verify(mockEntityFinder).hide();
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
		verify(mockView).showErrorMessage(error);
	}
	
	@Test
	public void testOnMoveCanUpdateSuccess(){
		AsyncMockStubber.callSuccessWith(new Folder()).when(mockSynapseClient).moveEntity(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		entityBundle.setEntity(new Folder());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.MOVE_ENTITY);
		verify(mockEntityFinder).configure(eq(EntityFilter.CONTAINER), anyBoolean(), any(SelectedHandler.class));
		verify(mockEntityFinder).show();
		verify(mockEntityFinder).hide();
		verify(mockSynapseClient).moveEntity(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
		verify(mockView, never()).showErrorMessage(anyString());
	}
	
	@Test
	public void testCreateLinkBadRequest(){
		AsyncMockStubber.callFailureWith(new BadRequestException("bad")).when(mockSynapseClient).createEntity(any(Entity.class), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.createLink("syn9876");
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_CANT_MOVE_HERE);
	}
	
	@Test
	public void testCreateLinkNotFound(){
		AsyncMockStubber.callFailureWith(new NotFoundException("not found")).when(mockSynapseClient).createEntity(any(Entity.class), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.createLink("syn9876");
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
	}
	
	@Test
	public void testCreateLinkUnauthorizedException(){
		AsyncMockStubber.callFailureWith(new UnauthorizedException("no way")).when(mockSynapseClient).createEntity(any(Entity.class), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.createLink("syn9876");
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NOT_AUTHORIZED);
	}
	
	@Test
	public void testCreateLinkUnknownException(){
		String error = "some error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).createEntity(any(Entity.class), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.createLink("syn9876");
		verify(mockView).showErrorMessage(error);
	}
	
	@Test
	public void testCreateLink(){
		String entityId = "syn123";
		Entity entity = entityBundle.getEntity();
		entity.setId(entityId);
		Long entityVersion = 42L;
		((Versionable)entity).setVersionNumber(entityVersion);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		AsyncMockStubber.callSuccessWith(new Link()).when(mockSynapseClient).createEntity(argument.capture(), any(AsyncCallback.class));
		boolean isCurrentVersion = false;
		controller.configure(mockActionMenu, entityBundle, isCurrentVersion, wikiPageId, currentEntityArea);
		controller.setIsShowingVersion(true);
		String target = "syn9876";
		controller.createLink(target);
		verify(mockView, never()).showErrorMessage(anyString());
		verify(mockView).showInfo(DisplayConstants.TEXT_LINK_SAVED);
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
	public void testCreateLinkCurrentVersion(){
		String entityId = "syn123";
		Entity entity = entityBundle.getEntity();
		entity.setId(entityId);
		Long entityVersion = 42L;
		((Versionable)entity).setVersionNumber(entityVersion);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		AsyncMockStubber.callSuccessWith(new Link()).when(mockSynapseClient).createEntity(argument.capture(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.setIsShowingVersion(false);
		String target = "syn9876";
		controller.createLink(target);
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
	public void testOnLinkNoUpdate(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CREATE_LINK);
		verify(mockEntityFinder, never()).configure(anyBoolean(), any(SelectedHandler.class));
		verify(mockEntityFinder, never()).show();
		verify(mockView, never()).showInfo(anyString());
	}
	
	@Test
	public void testOnLink(){
		AsyncMockStubber.callSuccessWith(new Link()).when(mockSynapseClient).createEntity(any(Entity.class), any(AsyncCallback.class));
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CREATE_LINK);
		verify(mockEntityFinder).configure(eq(EntityFilter.CONTAINER), anyBoolean(), any(SelectedHandler.class));
		verify(mockEntityFinder).show();
		verify(mockView).showInfo(DisplayConstants.TEXT_LINK_SAVED);
	}
	
	@Test
	public void testOnSubmitNoUpdate(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.SUBMIT_TO_CHALLENGE);
		verify(mockSubmitter, never()).configure(any(Entity.class), any(Set.class));
	}
	
	@Test
	public void testOnSubmitWithUdate(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.SUBMIT_TO_CHALLENGE);
		verify(mockSubmitter).configure(any(Entity.class), any(Set.class));
	}
	
	@Test
	public void testOnChangeStorageLocation(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CHANGE_STORAGE_LOCATION);
		verify(mockStorageLocationWidget).configure(this.entityBundle);
		verify(mockStorageLocationWidget).show();
	}
	
	@Test
	public void testOnChangeStorageLocationNoUpload(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CHANGE_STORAGE_LOCATION);
		verify(mockStorageLocationWidget, never()).configure(any(EntityBundle.class));
		verify(mockStorageLocationWidget, never()).show();
	}

	
	@Test
	public void testOnUploadNewFileNoUpload(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.UPLOAD_NEW_FILE);
		verify(mockUploader, never()).show();
	}
	
	@Test
	public void testOnUploadNewFileWithUpload(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.UPLOAD_NEW_FILE);
		verify(mockUploader).show();
		verify(mockUploader).setUploaderLinkNameVisible(false);
	}

	@Test
	public void testConfigureNoWikiSubpageProject(){
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.WIKI;
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, true);
		verify(mockActionMenu).setActionListener(Action.ADD_WIKI_SUBPAGE, controller);
	}
	
	@Test
	public void testConfigurWikiSubpageProjectNoRootPage(){
		entityBundle.setEntity(new Project());
		currentEntityArea = EntityArea.WIKI;
		wikiPageId = null;
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
		verify(mockActionMenu, never()).setActionVisible(Action.ADD_WIKI_SUBPAGE, true);
	}
	
	@Test
	public void testConfigureWikiSubpageFolder(){
		entityBundle.setEntity(new Folder());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
	}
	
	@Test
	public void testConfigureWikiSubpageTable(){
		entityBundle.setEntity(new TableEntity());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
	}
	
	@Test
	public void testConfigureWikiSubpageView(){
		entityBundle.setEntity(new EntityView());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
	}

	
	@Test
	public void testOnAddWikiSubpageNoUpdate(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		entityBundle.setRootWikiId(null);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.ADD_WIKI_SUBPAGE);
		verify(mockSynapseClient, never()).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnRootAddWikiSubpageCanUpdate(){
		//Edge case.  User attempts to add a subpage on a project that does not yet have a wiki.  Verify a root page is created (and page refreshed)...
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		WikiPage newWikiPage = new WikiPage();
		String newWikiPageId = "49382";
		newWikiPage.setId(newWikiPageId);
		AsyncMockStubber.callSuccessWith(newWikiPage).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		entityBundle.setRootWikiId(null);
		controller.configure(mockActionMenu, entityBundle, true, null, currentEntityArea);
		controller.onAction(Action.ADD_WIKI_SUBPAGE);
		verify(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockPlaceChanger).goTo(new Synapse(entityId, null, EntityArea.WIKI, newWikiPageId));
	}
	
	@Test
	public void testOnAddWikiSubpageCanUpdate(){
		//Set up so that we are on the root wiki page, and we run the add subpage command.
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		WikiPage newWikiPage = new WikiPage();
		String newWikiPageId = "55555";
		newWikiPage.setId(newWikiPageId);
		
		AsyncMockStubber.callSuccessWith(newWikiPage).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		entityBundle.setRootWikiId("123");
		controller.configure(mockActionMenu, entityBundle, true,"123", currentEntityArea);
		controller.onAction(Action.ADD_WIKI_SUBPAGE);
		//verify that it has not yet created the wiki page
		verify(mockSynapseClient, never()).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
		//it prompts the user for a wiki page name
		ArgumentCaptor<PromptCallback> callbackCaptor = ArgumentCaptor.forClass(PromptCallback.class);
		verify(mockView).showPromptDialog(anyString(), callbackCaptor.capture());
		PromptCallback capturedCallback = callbackCaptor.getValue();
		//if called back with an undefined value, a wiki page is still not created
		capturedCallback.callback("");
		verify(mockSynapseClient, never()).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
		
		capturedCallback.callback(null);
		verify(mockSynapseClient, never()).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
		
		capturedCallback.callback("a valid name");
		verify(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockPlaceChanger).goTo(new Synapse(entityId, null, EntityArea.WIKI, newWikiPageId));
	}
	
	@Test
	public void testCreateWikiPageFailure(){
		//Set up so that we are on the root wiki page, and we run the add subpage command.
		String error = "goodnight";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		entityBundle.setRootWikiId("123");
		controller.configure(mockActionMenu, entityBundle, true,"123", currentEntityArea);
		controller.createWikiPage("foo");
		
		verify(mockSynapseClient).createV2WikiPageWithV1(anyString(), anyString(), any(WikiPage.class),any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testCreateDoi() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CREATE_DOI);
		verify(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		//refresh page
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testCreateDoiFail() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CREATE_DOI);
		verify(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testConfigureDoiNotFound() throws Exception {
		entityBundle.setDoi(null);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//initially hide, then show
		verify(mockActionMenu).setActionVisible(Action.CREATE_DOI, false);
		verify(mockActionMenu).setActionVisible(Action.CREATE_DOI, true);
	}
	
	@Test
	public void testConfigureDoiView() throws Exception {
		// Create DOI not available for Views
		entityBundle.setDoi(null);
		entityBundle.setEntity(new EntityView());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.CREATE_DOI, false);
		verify(mockActionMenu, never()).setActionVisible(Action.CREATE_DOI, true);
	}
	
	@Test
	public void testConfigureDoiNotFoundNonEditable() throws Exception {
		permissions.setCanEdit(false);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//initially hide, never show
		verify(mockActionMenu).setActionVisible(Action.CREATE_DOI, false);
		verify(mockActionMenu, never()).setActionVisible(Action.CREATE_DOI, true);
	}
	
	@Test
	public void testConfigureDoiFound() throws Exception {
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//initially hide, never show
		verify(mockActionMenu).setActionVisible(Action.CREATE_DOI, false);
		verify(mockActionMenu, never()).setActionVisible(Action.CREATE_DOI, true);
	}

	@Test
	public void testConfigureCreateOrUpdateDoiNotFound() throws Exception {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");

		entityBundle.setDoiAssociation(null);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//initially hide, then show
		verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
		verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
	}

	@Test
	public void testConfigureCreateOrUpdateDoiView() throws Exception {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");

		// Create DOI not available for Views
		entityBundle.setDoiAssociation(null);
		entityBundle.setEntity(new EntityView());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
		verify(mockActionMenu, never()).setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
	}

	@Test
	public void testConfigureCreateOrUpdateDoiNotFoundNonEditable() throws Exception {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");

		permissions.setCanEdit(false);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//initially hide, never show
		verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
		verify(mockActionMenu, never()).setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
	}

	@Test
	public void testConfigureCreateOrUpdateDoiFound() throws Exception {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");

		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//hide, and then show with 'update' text
		verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, false);
		verify(mockActionMenu).setActionVisible(Action.CREATE_OR_UPDATE_DOI, true);
		verify(mockActionMenu).setActionText(Action.CREATE_OR_UPDATE_DOI, UPDATE_DOI_FOR + EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(entityBundle.getEntity().getClass())));

	}

	@Test
	public void testOnSelectChallengeTeam() {
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).createChallenge(any(Challenge.class), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CREATE_CHALLENGE);
		verify(mockSelectTeamModal).show();
		
		//now simulate that a team was selected
		ArgumentCaptor<CallbackP> teamSelectedCallback = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockSelectTeamModal).configure(teamSelectedCallback.capture());
		teamSelectedCallback.getValue().invoke(SELECTED_TEAM_ID);
		
		ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
		verify(mockChallengeClient).createChallenge(captor.capture(), any(AsyncCallback.class));
		verify(mockPlaceChanger).goTo(new Synapse(entityId, null, EntityArea.CHALLENGE, null) );
		verify(mockView).showInfo(DisplayConstants.CHALLENGE_CREATED);
		Challenge c = captor.getValue();
		assertNull(c.getId());
		assertEquals(SELECTED_TEAM_ID, c.getParticipantTeamId());
	}
	@Test
	public void testCreateChallengeFailure(){
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		String error = "an error";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockChallengeClient).createChallenge(any(Challenge.class), any(AsyncCallback.class));
		controller.onAction(Action.CREATE_CHALLENGE);
		//now simulate that a challenge team was selected
		ArgumentCaptor<CallbackP> teamSelectedCallback = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockSelectTeamModal).configure(teamSelectedCallback.capture());
		teamSelectedCallback.getValue().invoke(SELECTED_TEAM_ID);
		
		verify(mockChallengeClient).createChallenge(any(Challenge.class), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(error);
	}
	

	@Test
	public void testConfigureChallengeNotFound() throws Exception {
		// note that the currentArea is null (project settings)
		currentEntityArea = null;
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		entityBundle.setEntity(new Project());
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//initially hide, then show
		InOrder inOrder = inOrder(mockActionMenu);
		inOrder.verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, false);
		inOrder.verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, true);
		verify(mockActionMenu, never()).setToolsButtonIcon(anyString(), any(IconType.class));
	}

	@Test
	public void testConfigureChallengeFoundProjectSettingsMenu() throws Exception {
		// project settings menu
		currentEntityArea = null;
		entityBundle.setEntity(new Project());
		AsyncMockStubber.callSuccessWith(new Challenge()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu, never()).setActionVisible(Action.DELETE_CHALLENGE, true);
	}
	
	@Test
	public void testConfigureChallengeFound() throws Exception {
		// currentArea is on the challenge tab
		currentEntityArea = EntityArea.CHALLENGE;
		entityBundle.setEntity(new Project());
		AsyncMockStubber.callSuccessWith(new Challenge()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.DELETE_CHALLENGE, true);
	}
	
	@Test
	public void testConfigureCreateChallengeActionWikiArea() throws Exception {
		//SWC-3876:  if tools menu is set up for wiki commands, do not show the Run Challenge command (even in alpha mode)
		currentEntityArea = EntityArea.WIKI;
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		entityBundle.setEntity(new Project());
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, false);
		verify(mockActionMenu, never()).setActionVisible(Action.CREATE_CHALLENGE, true);
	}
	
	@Test
	public void testConfigureChallengeFoundNonEditable() throws Exception {
		entityBundle.setEntity(new Project());
		permissions.setCanEdit(false);
		AsyncMockStubber.callSuccessWith(new Challenge()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		//initially hide, never show
		verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, false);
		verify(mockActionMenu, never()).setActionVisible(Action.CREATE_CHALLENGE, true);
	}
	
	@Test
	public void testGetChallengeError() throws Exception {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		entityBundle.setEntity(new Project());
		String error = "an error";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.CREATE_CHALLENGE, false);
		verify(mockView).showErrorMessage(error);
	}
	
	@Test
	public void testFolderDeletionPrompt() {
		/*
		 *  The user must be shown a confirm dialog before a delete.  Confirm is signaled via the Callback.invoke()
		 *  in this case we do not want to confirm.
		 */
		AsyncMockStubber.callNoInvovke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		Folder f = new Folder();
		f.setName("Test");
		entityBundle.setEntity(f);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		String display = ARE_YOU_SURE_YOU_WANT_TO_DELETE+"Folder \"Test\"?" + DELETE_FOLDER_EXPLANATION;
		// the call under tests
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDeleteDialog(eq(display), any(Callback.class));
		// should not make it to the pre-flight check
		verify(mockPreflightController, never()).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
	}
	
	@Test
	public void testNotFolderDeletionPrompt() {
		/*
		 *  The user must be shown a confirm dialog before a delete.  Confirm is signaled via the Callback.invoke()
		 *  in this case we do not want to confirm.
		 */
		AsyncMockStubber.callNoInvovke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		Project p = new Project();
		p.setName("Test");
		entityBundle.setEntity(p);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		String display = ARE_YOU_SURE_YOU_WANT_TO_DELETE+"Project \"Test\"?";
		String folderDisplay = display + DELETE_FOLDER_EXPLANATION;
		// the call under tests
		controller.onAction(Action.DELETE_ENTITY);
		verify(mockView).showConfirmDeleteDialog(eq(display), any(Callback.class));
		verify(mockView, times(0)).showConfirmDeleteDialog(eq(folderDisplay), any(Callback.class));
		// should not make it to the pre-flight check
		verify(mockPreflightController, never()).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
	}
	
	@Test
	public void testConfigureManageAccessRequirements(){
		entityBundle.setEntity(new Folder());
		entityBundle.setRootWikiId("7890");
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		verify(mockActionMenu).setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, false);
		verify(mockActionMenu).setActionVisible(Action.APPROVE_USER_ACCESS, false);
		
		verify(mockActionMenu, never()).setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
		verify(mockIsACTMemberAsyncHandler, atLeastOnce()).isACTActionAvailable(callbackPCaptor.capture());
		List<CallbackP<Boolean>> isACTCallbacks = callbackPCaptor.getAllValues();
		for (CallbackP<Boolean> isACTCallback : isACTCallbacks) {
			isACTCallback.invoke(false);	
		}
		verify(mockActionMenu, never()).setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
		verify(mockActionMenu, never()).setActionVisible(Action.APPROVE_USER_ACCESS, true);
		
		for (CallbackP<Boolean> isACTCallback : isACTCallbacks) {
			isACTCallback.invoke(true);	
		}
		
		verify(mockActionMenu).setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
		verify(mockActionMenu).setActionVisible(Action.APPROVE_USER_ACCESS, true);
		verify(mockActionMenu).setActionListener(Action.MANAGE_ACCESS_REQUIREMENTS, controller);
		verify(mockActionMenu).setACTDividerVisible(true);
	}

	@Test
	public void testOnManageAccessRequirements(){
		entityBundle.setEntity(new Folder());
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		controller.onAction(Action.MANAGE_ACCESS_REQUIREMENTS);
		verify(mockPlaceChanger).goTo(any(AccessRequirementsPlace.class));
	}
	
	@Test
	public void testUploadNewFileEntity(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		String folderId = "syn1292";
		Entity parentFolder = new Folder();
		parentFolder.setId(folderId);
		entityBundle.setEntity(parentFolder);
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.UPLOAD_FILE);
		boolean isEntity = true;
		Entity currentFileEntity = null;
		CallbackP<String> fileHandleIdCallback = null;
		verify(mockUploader).configure(
				DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, 
				currentFileEntity, 
				folderId,
				fileHandleIdCallback,
				isEntity);
		verify(mockUploader).setUploaderLinkNameVisible(true);
		verify(mockUploader).show();
	}
	
	@Test
	public void testCreateFolder(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CREATE_FOLDER);
		verify(mockAddFolderDialogWidget).show(entityId);
	}
	@Test
	public void testUploadTable(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntityAndUpload(any(EntityBundle.class), anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.UPLOAD_TABLE);
		verify(mockUploadTableModalWidget).configure(entityId, null);
		verify(mockUploadTableModalWidget).showModal(any(WizardCallback.class));
	}
	@Test
	public void testAddTable(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.ADD_TABLE);
		verify(mockCreateTableViewWizard).configure(entityId, TableType.table);
		verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
	}
	@Test
	public void testAddFileView(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.ADD_FILE_VIEW);
		verify(mockCreateTableViewWizard).configure(entityId, TableType.files);
		verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
	}
	@Test
	public void testAddProjectView(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.ADD_PROJECT_VIEW);
		verify(mockCreateTableViewWizard).configure(entityId, TableType.projects);
		verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
	}
	@Test
	public void testCreateExternalDockerRepo(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		controller.onAction(Action.CREATE_EXTERNAL_DOCKER_REPO);
		verify(mockAddExternalRepoModal).configuration(eq(entityId), any(Callback.class));
		verify(mockAddExternalRepoModal).show();
	}
	
	@Test
	public void testDeleteChallengeCancelConfirm(){
		AsyncMockStubber.callNoInvovke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		controller.onAction(Action.DELETE_CHALLENGE);
		
		verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		// should not make it to the pre-flight check
		verify(mockPreflightController, never()).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
	}
	
	@Test
	public void testDeleteChallengeConfirmed(){
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		controller.onAction(Action.DELETE_CHALLENGE);
		
		verify(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		verify(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		verify(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testDeleteChallengeFailure(){
		String error = "unable to delete challenge";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDeleteDialog(anyString(), any(Callback.class));
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		controller.configure(mockActionMenu, entityBundle, true, wikiPageId, currentEntityArea);
		
		controller.onAction(Action.DELETE_CHALLENGE);
		
		verify(mockChallengeClient).deleteChallenge(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(error);
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}
}
