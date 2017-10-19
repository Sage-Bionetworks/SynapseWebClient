package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.CONTAINER;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.PROJECT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialog;
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
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerImpl implements EntityActionController, ActionListener, EntityActionControllerView.Presenter {
	
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
	AnnotationsRendererWidget annotationsRendererWidget;
	EditAnnotationsDialog editAnnotationsDialog;
	
	EntityBundle entityBundle;
	String wikiPageId, parentWikiPageId;
	Entity entity;
	UserEntityPermissions permissions;
	String enityTypeDisplay;
	boolean isUserAuthenticated;
	boolean isCurrentVersion;
	ActionMenuWidget actionMenu;
	EntityUpdatedHandler entityUpdateHandler;
	UploadDialogWidget uploader;
	WikiMarkdownEditor wikiEditor;
	ProvenanceEditorWidget provenanceEditor;
	StorageLocationWidget storageLocationEditor;
	EvaluationEditorModal evalEditor;
	CookieProvider cookies;
	ChallengeClientAsync challengeClient;
	SelectTeamModal selectTeamModal;
	ApproveUserAccessModal approveUserAccessModal;
	UserProfileClientAsync userProfileClient;
	PortalGinInjector ginInjector;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	AddFolderDialogWidget addFolderDialogWidget;
	boolean isShowingVersion = false;
	
	@Inject
	public EntityActionControllerImpl(EntityActionControllerView view,
			PreflightController preflightController,
			PortalGinInjector ginInjector,
			AuthenticationController authenticationController,
			CookieProvider cookies,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler) {
		super();
		this.view = view;
		this.ginInjector = ginInjector;
		this.preflightController = preflightController;
		this.authenticationController = authenticationController;
		this.cookies = cookies;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		view.setPresenter(this);
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
	
	private ChallengeClientAsync getChallengeClient() {
		if (challengeClient == null) {
			challengeClient = ginInjector.getChallengeClientAsync();
		}
		return challengeClient;
	}
	private SynapseClientAsync getSynapseClient() {
		if (synapseClient == null) {
			synapseClient = ginInjector.getSynapseClientAsync();
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
	
	private UploadDialogWidget getUploadDialogWidget() {
		if (uploader == null) {
			uploader = ginInjector.getUploadDialogWidget();
			view.addWidget(uploader.asWidget());
		}
		return uploader;
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
			EntityBundle entityBundle, boolean isCurrentVersion, String wikiPageId, EntityArea currentArea,  EntityUpdatedHandler handler) {
		this.entityBundle = entityBundle;
		this.wikiPageId = wikiPageId;
		this.entityUpdateHandler = handler;
		this.permissions = entityBundle.getPermissions();
		this.actionMenu = actionMenu;
		this.entity = entityBundle.getEntity();
		this.isUserAuthenticated = authenticationController.isLoggedIn();
		this.isCurrentVersion = isCurrentVersion;
		this.enityTypeDisplay = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(entityBundle.getEntity().getClass()));
		this.currentArea = currentArea;

		// hide all commands by default
		for (Action action : Action.values()) {
			actionMenu.setActionVisible(action, false);	
		}
		view.hideAnnotations();
		view.hideFileHistory();
		
		if (!isUserAuthenticated) {
			if (permissions.getCanPublicRead()) {
				configureAnnotations();
				configureFileHistory();
			}
		} else {
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
			configureCreateDOI();
			configureEditProjectMetadataAction();
			configureEditFileMetadataAction();
			configureAddEvaluationAction();
			configureCreateChallenge();
			configureApproveUserAccess();
			configureManageAccessRequirements();
			configureTableCommands();
			configureAddFolder();
			configureUploadNewFileEntity();
		}
	}
	
	private void configureApproveUserAccess() {
		// TODO: remove APPROVE_USER_ACCESS command (after new ACT feature is released, where the system supports the workflow)
		actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, false);
		actionMenu.setActionEnabled(Action.APPROVE_USER_ACCESS, false);	
		actionMenu.setActionListener(Action.APPROVE_USER_ACCESS, this);
		actionMenu.setACTDividerVisible(false);
		if (authenticationController.isLoggedIn() && !EntityArea.WIKI.equals(currentArea)) {
			isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
				@Override
				public void invoke(Boolean isACT) {
					if (isACT) {
						actionMenu.setActionVisible(Action.APPROVE_USER_ACCESS, true);
						actionMenu.setActionEnabled(Action.APPROVE_USER_ACCESS, true);
						actionMenu.setACTDividerVisible(true);
					}
				}
			});
		}
	}

	private void configureManageAccessRequirements() {
		actionMenu.setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, false);
		actionMenu.setActionEnabled(Action.MANAGE_ACCESS_REQUIREMENTS, false);	
		actionMenu.setActionListener(Action.MANAGE_ACCESS_REQUIREMENTS, this);
		actionMenu.setACTDividerVisible(false);
		if (authenticationController.isLoggedIn() && !EntityArea.WIKI.equals(currentArea)) {
			isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
				@Override
				public void invoke(Boolean isACT) {
					if (isACT) {
						actionMenu.setActionVisible(Action.MANAGE_ACCESS_REQUIREMENTS, true);
						actionMenu.setActionEnabled(Action.MANAGE_ACCESS_REQUIREMENTS, true);
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
				view.showInfo(DisplayConstants.CHALLENGE_CREATED, "");
				// go to challenge tab
				Place gotoPlace = new Synapse(entity.getId(), null, EntityArea.ADMIN, null);
				getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	private void configureProvenance() {
		if(entityBundle.getEntity() instanceof FileEntity || entityBundle.getEntity() instanceof Table) {
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.EDIT_PROVENANCE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_PROVENANCE, this);
			actionMenu.setActionText(Action.EDIT_PROVENANCE, "Edit "+enityTypeDisplay + " Provenance");
		} else {
			actionMenu.setActionVisible(Action.EDIT_PROVENANCE, false);
			actionMenu.setActionEnabled(Action.EDIT_PROVENANCE, false);
		}
	}
	
	private boolean isContainerOnFilesTab() {
		return (entityBundle.getEntity() instanceof Folder || 
				(entityBundle.getEntity() instanceof Project && EntityArea.FILES.equals(currentArea)));
	}
	
	private void configureChangeStorageLocation() {
		if(entityBundle.getEntity() instanceof Folder || 
				(entityBundle.getEntity() instanceof Project && currentArea == null)){
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.CHANGE_STORAGE_LOCATION, permissions.getCanEdit());
			actionMenu.setActionText(Action.CHANGE_STORAGE_LOCATION, "Change "+enityTypeDisplay + " Storage Location");
			actionMenu.setActionListener(Action.CHANGE_STORAGE_LOCATION, this);
		} else {
			actionMenu.setActionVisible(Action.CHANGE_STORAGE_LOCATION, false);
			actionMenu.setActionEnabled(Action.CHANGE_STORAGE_LOCATION, false);
		}
	}
	
	private void configureCreateDOI() {
		boolean canEdit = permissions.getCanEdit();
		actionMenu.setActionVisible(Action.CREATE_DOI, false);
		actionMenu.setActionEnabled(Action.CREATE_DOI, false);
		if (canEdit && !isTopLevelProjectToolsMenu()) {
			actionMenu.setActionListener(Action.CREATE_DOI, this);
			if (entityBundle.getDoi() == null) {
				//show command if not returned, thus not in existence
				actionMenu.setActionVisible(Action.CREATE_DOI, true);
				actionMenu.setActionEnabled(Action.CREATE_DOI, true);
				actionMenu.setActionText(Action.CREATE_DOI, "Create DOI for  "+enityTypeDisplay);
			}
		}
	}
	
	private void configureCreateChallenge() {
		actionMenu.setActionVisible(Action.CREATE_CHALLENGE, false);
		actionMenu.setActionEnabled(Action.CREATE_CHALLENGE, false);
		boolean canEdit = permissions.getCanEdit();
		if(entityBundle.getEntity() instanceof Project && canEdit && (DisplayUtils.isInTestWebsite(cookies) || EntityArea.ADMIN.equals(currentArea))) {
			actionMenu.setActionListener(Action.CREATE_CHALLENGE, this);
			//find out if this project has a challenge
			getChallengeClient().getChallengeForProject(entity.getId(), new AsyncCallback<Challenge>() {
				@Override
				public void onSuccess(Challenge result) {
					// challenge found, do nothing
				}
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof NotFoundException) {
						actionMenu.setActionVisible(Action.CREATE_CHALLENGE, true);
						actionMenu.setActionEnabled(Action.CREATE_CHALLENGE, true);
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
				view.showInfo(DisplayConstants.DOI_REQUEST_SENT_TITLE, DisplayConstants.DOI_REQUEST_SENT_MESSAGE);
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	
	private void onCreateChallenge() {
		getSelectTeamModal().show();
	}
	
	private void configureTableCommands() {
		if(entityBundle.getEntity() instanceof Table ) {
			boolean canEditResults = entityBundle.getPermissions().getCanCertifiedUserEdit();
			actionMenu.setActionVisible(Action.UPLOAD_TABLE_DATA, canEditResults);
			actionMenu.setActionText(Action.UPLOAD_TABLE_DATA, "Upload Data to " + enityTypeDisplay);
			actionMenu.setActionVisible(Action.EDIT_TABLE_DATA, canEditResults);
			actionMenu.setActionVisible(Action.DOWNLOAD_TABLE_QUERY_RESULTS, true);
			actionMenu.setActionText(Action.DOWNLOAD_TABLE_QUERY_RESULTS, "Download " + enityTypeDisplay + " Data");
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
			actionMenu.setActionEnabled(Action.UPLOAD_NEW_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.UPLOAD_NEW_FILE, this);
		}else{
			actionMenu.setActionVisible(Action.UPLOAD_NEW_FILE, false);
			actionMenu.setActionEnabled(Action.UPLOAD_NEW_FILE, false);
		}
	}

	private boolean isTopLevelProjectToolsMenu() {
		return entityBundle.getEntity() instanceof Project && currentArea != null;
	}
	
	private boolean isWikiableConfig() {
		if (entityBundle.getEntity() instanceof Project) {
			return EntityArea.WIKI.equals(currentArea);
		} 
		return isWikiableType(entityBundle.getEntity());
	}
	
	private void configureUploadNewFileEntity() {
		if(isContainerOnFilesTab()){
			actionMenu.setActionVisible(Action.UPLOAD_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionEnabled(Action.UPLOAD_FILE, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.UPLOAD_FILE, this);
		}else{
			actionMenu.setActionVisible(Action.UPLOAD_FILE, false);
			actionMenu.setActionEnabled(Action.UPLOAD_FILE, false);
		}
	}
	
	private void configureAddFolder() {
		if(isContainerOnFilesTab()){
			actionMenu.setActionVisible(Action.CREATE_FOLDER, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionEnabled(Action.CREATE_FOLDER, permissions.getCanCertifiedUserEdit());
			actionMenu.setActionListener(Action.CREATE_FOLDER, this);
		}else{
			actionMenu.setActionVisible(Action.CREATE_FOLDER, false);
			actionMenu.setActionEnabled(Action.CREATE_FOLDER, false);
		}
	}
	
	private void configureEditWiki(){
		if(isWikiableConfig()){
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.EDIT_WIKI_PAGE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_WIKI_PAGE, this);
			actionMenu.setActionText(Action.EDIT_WIKI_PAGE, EDIT_WIKI_PREFIX+enityTypeDisplay+WIKI);
		}else{
			actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, false);
			actionMenu.setActionEnabled(Action.EDIT_WIKI_PAGE, false);
		}
	}
	
	private void configureViewWikiSource(){
		//only visible if entity may have a wiki, and user can't Edit the wiki
		if(isWikiableConfig()){
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, !permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.VIEW_WIKI_SOURCE, !permissions.getCanEdit());
			actionMenu.setActionListener(Action.VIEW_WIKI_SOURCE, this);
		}else{
			actionMenu.setActionVisible(Action.VIEW_WIKI_SOURCE, false);
			actionMenu.setActionEnabled(Action.VIEW_WIKI_SOURCE, false);
		}
	}

	private void configureReorderWikiSubpages() {
		if(isWikiableConfig() && entityBundle.getEntity() instanceof Project){
			// shown if there's more than one page
			getSynapseClient().getV2WikiHeaderTree(entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), new AsyncCallback<List<V2WikiHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
					actionMenu.setActionEnabled(Action.REORDER_WIKI_SUBPAGES, false);
				}
				public void onSuccess(List<V2WikiHeader> wikiHeaders) {
					boolean isMoreThanOne = wikiHeaders.size() > 1;
					actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, isMoreThanOne && permissions.getCanEdit());
					actionMenu.setActionEnabled(Action.REORDER_WIKI_SUBPAGES, isMoreThanOne && permissions.getCanEdit());
				};
			});
		} else{
			actionMenu.setActionVisible(Action.REORDER_WIKI_SUBPAGES, false);
			actionMenu.setActionEnabled(Action.REORDER_WIKI_SUBPAGES, false);
		}
	}
	
	private void configureAddWikiSubpage(){
		if(isWikiableConfig() && entityBundle.getEntity() instanceof Project){
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.ADD_WIKI_SUBPAGE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.ADD_WIKI_SUBPAGE, this);
		}else{
			actionMenu.setActionVisible(Action.ADD_WIKI_SUBPAGE, false);
			actionMenu.setActionEnabled(Action.ADD_WIKI_SUBPAGE, false);
		}
	}

	
	private void configureMove(){
		if(isMovableType(entityBundle.getEntity()) ){
			actionMenu.setActionVisible(Action.MOVE_ENTITY, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.MOVE_ENTITY, permissions.getCanEdit());
			actionMenu.setActionText(Action.MOVE_ENTITY, MOVE_PREFIX+enityTypeDisplay);
			actionMenu.setActionListener(Action.MOVE_ENTITY, this);
		}else{
			actionMenu.setActionVisible(Action.MOVE_ENTITY, false);
			actionMenu.setActionEnabled(Action.MOVE_ENTITY, false);
		}
	}
	
	private void configureLink(){
		if(isLinkType(entityBundle.getEntity()) && !isTopLevelProjectToolsMenu()){
			actionMenu.setActionVisible(Action.CREATE_LINK, true);
			actionMenu.setActionEnabled(Action.CREATE_LINK, true);
			actionMenu.setActionListener(Action.CREATE_LINK, this);
			actionMenu.setActionText(Action.CREATE_LINK, "Save Link to "+enityTypeDisplay);
		}else{
			actionMenu.setActionVisible(Action.CREATE_LINK, false);
			actionMenu.setActionEnabled(Action.CREATE_LINK, false);
		}
	}
	
	private void configureAnnotations(){
		if (isTopLevelProjectToolsMenu()) {
			actionMenu.setActionVisible(Action.SHOW_ANNOTATIONS, false);
		} else {
			actionMenu.setActionVisible(Action.SHOW_ANNOTATIONS, true);
			actionMenu.setActionEnabled(Action.SHOW_ANNOTATIONS, true);
			actionMenu.setActionListener(Action.SHOW_ANNOTATIONS, this);
		}
	}
	
	private void configureFileHistory(){
		if(entityBundle.getEntity() instanceof FileEntity){
			actionMenu.setActionVisible(Action.SHOW_FILE_HISTORY, true);
			actionMenu.setActionEnabled(Action.SHOW_FILE_HISTORY, true);
			actionMenu.setActionListener(Action.SHOW_FILE_HISTORY, this);
		}else{
			actionMenu.setActionVisible(Action.SHOW_FILE_HISTORY, false);
			actionMenu.setActionEnabled(Action.SHOW_FILE_HISTORY, false);
		}
	}
	
	private void configureSubmit(){
		if(isSubmittableType(entityBundle.getEntity())){
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, true);
			actionMenu.setActionEnabled(Action.SUBMIT_TO_CHALLENGE, true);
			actionMenu.setActionListener(Action.SUBMIT_TO_CHALLENGE, this);
			actionMenu.setActionText(Action.SUBMIT_TO_CHALLENGE, "Submit "+enityTypeDisplay+" to Challenge");
		}else{
			actionMenu.setActionVisible(Action.SUBMIT_TO_CHALLENGE, false);
			actionMenu.setActionEnabled(Action.SUBMIT_TO_CHALLENGE, false);
		}
	}
	
	private void configureEditProjectMetadataAction(){
		if(entityBundle.getEntity() instanceof Project && currentArea == null){
			actionMenu.setActionVisible(Action.EDIT_PROJECT_METADATA, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.EDIT_PROJECT_METADATA, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_PROJECT_METADATA, this);
		}else{
			actionMenu.setActionVisible(Action.EDIT_PROJECT_METADATA, false);
			actionMenu.setActionEnabled(Action.EDIT_PROJECT_METADATA, false);
		}
	}
	
	private void configureAddEvaluationAction(){
		if(entityBundle.getEntity() instanceof Project && DisplayUtils.isInTestWebsite(cookies)){
			actionMenu.setActionVisible(Action.ADD_EVALUATION_QUEUE, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.ADD_EVALUATION_QUEUE, permissions.getCanEdit());
			actionMenu.setActionListener(Action.ADD_EVALUATION_QUEUE, this);
		}else{
			actionMenu.setActionVisible(Action.ADD_EVALUATION_QUEUE, false);
			actionMenu.setActionEnabled(Action.ADD_EVALUATION_QUEUE, false);
		}
	}
	
	private void configureEditFileMetadataAction(){
		if(entityBundle.getEntity() instanceof FileEntity){
			actionMenu.setActionVisible(Action.EDIT_FILE_METADATA, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.EDIT_FILE_METADATA, permissions.getCanEdit());
			actionMenu.setActionListener(Action.EDIT_FILE_METADATA, this);
		} else{
			actionMenu.setActionVisible(Action.EDIT_FILE_METADATA, false);
			actionMenu.setActionEnabled(Action.EDIT_FILE_METADATA, false);
		}
	}
	
	private void configureRenameAction(){
		if(isRenameOnly(entityBundle.getEntity()) && !(entityBundle.getEntity() instanceof DockerRepository)) {
			actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, permissions.getCanEdit());
			actionMenu.setActionEnabled(Action.CHANGE_ENTITY_NAME, permissions.getCanEdit());
			actionMenu.setActionText(Action.CHANGE_ENTITY_NAME, RENAME_PREFIX+enityTypeDisplay);
			actionMenu.setActionListener(Action.CHANGE_ENTITY_NAME, this);
		} else {
			actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, false);
			actionMenu.setActionEnabled(Action.CHANGE_ENTITY_NAME, false);
		}
	}
	
	private void configureDeleteAction(){
		if (isTopLevelProjectToolsMenu()) {
			actionMenu.setActionVisible(Action.DELETE_ENTITY, false);
		} else {
			actionMenu.setActionVisible(Action.DELETE_ENTITY, permissions.getCanDelete());
			actionMenu.setActionEnabled(Action.DELETE_ENTITY, permissions.getCanDelete());
			actionMenu.setActionText(Action.DELETE_ENTITY, DELETE_PREFIX+enityTypeDisplay);
			actionMenu.setActionListener(Action.DELETE_ENTITY, this);
		}
	}
	private void configureDeleteWikiAction(){
		if(isWikiableConfig() && entityBundle.getEntity() instanceof Project){
			actionMenu.setActionVisible(Action.DELETE_WIKI_PAGE, permissions.getCanDelete());
			actionMenu.setActionEnabled(Action.DELETE_WIKI_PAGE, permissions.getCanDelete());
			actionMenu.setActionListener(Action.DELETE_WIKI_PAGE, this);
		} else {
			actionMenu.setActionVisible(Action.DELETE_WIKI_PAGE, false);
			actionMenu.setActionEnabled(Action.DELETE_WIKI_PAGE, false);
		}
	}
	
	private void configureShareAction(){
		if (isTopLevelProjectToolsMenu()) {
			actionMenu.setActionVisible(Action.SHARE, false);
		} else {
			actionMenu.setActionEnabled(Action.SHARE, true);
			actionMenu.setActionVisible(Action.SHARE, true);
			actionMenu.setActionListener(Action.SHARE, this);
			actionMenu.setActionText(Action.SHARE, enityTypeDisplay + " Sharing Settings");
			GlobalApplicationState globalAppState = getGlobalApplicationState();
			if(PublicPrivateBadge.isPublic(entityBundle.getBenefactorAcl(), globalAppState.getPublicPrincipalIds())){
				actionMenu.setActionIcon(Action.SHARE, IconType.GLOBE);
			}else{
				actionMenu.setActionIcon(Action.SHARE, IconType.LOCK);
			}	
		}
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
		case ADD_EVALUATION_QUEUE:
			onAddEvaluationQueue();
			break;
		case CREATE_CHALLENGE:
			onCreateChallenge();
			break;
		case APPROVE_USER_ACCESS:
			onApproveUserAccess();
			break;
		case MANAGE_ACCESS_REQUIREMENTS:
			onManageAccessRequirements();
			break;
		case SHOW_ANNOTATIONS :
			onShowAnnotations();
			break;
		case SHOW_FILE_HISTORY : 
			onShowFileHistory();
			break;
		case UPLOAD_FILE :
			onUploadNewFileEntity();
			break;
		case CREATE_FOLDER :
			onCreateFolder();
			break;
		default:
			break;
		}
	}
	
	private void onUploadNewFileEntity() {
		preflightController.checkUploadToEntity(this.entityBundle, new Callback() {
			@Override
			public void invoke() {
				UploadDialogWidget uploader = getUploadDialogWidget();
				uploader.configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, null,
						entityBundle.getEntity().getId(), entityUpdateHandler, null, true);
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
				Place gotoPlace = new Synapse(entity.getId(), null, EntityArea.ADMIN, null);
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
	
	private AnnotationsRendererWidget getAnnotationsRendererWidget() {
		if (annotationsRendererWidget == null) {
			annotationsRendererWidget = ginInjector.getAnnotationsRendererWidget();
			view.setAnnotationsRendererWidget(annotationsRendererWidget);
		}
		return annotationsRendererWidget;
	}
	
	private EditAnnotationsDialog getEditAnnotationsDialog() {
		if (editAnnotationsDialog == null) {
			editAnnotationsDialog = ginInjector.getEditAnnotationsDialog();
		}
		return editAnnotationsDialog;
	}
	
	private void onShowAnnotations() {
		// configure and show annotations
		if (entityBundle.getPermissions().getCanCertifiedUserEdit()) {
			// show editor, if user passes preflight test
			preflightController.checkUploadToEntity(entityBundle, new Callback() {
				@Override
				public void invoke() {
					getEditAnnotationsDialog().configure(entityBundle, entityUpdateHandler);
				}
			});
		} else {
			// show renderer
			getAnnotationsRendererWidget().configure(entityBundle);
			view.showAnnotations();
		}
	}
	
	private void onShowFileHistory() {
		FileHistoryWidget fileHistoryWidget = ginInjector.getFileHistoryWidget();
		view.setFileHistoryWidget(fileHistoryWidget);
		// configure and show file history
		fileHistoryWidget.setEntityBundle(entityBundle, getVersion(), isCurrentVersion);
		fileHistoryWidget.setEntityUpdatedHandler(entityUpdateHandler);
		view.showFileHistory();
	}
	
	private void postChangeStorageLocation() {
		getStorageLocationWidget().configure(this.entityBundle, entityUpdateHandler);
		getStorageLocationWidget().show();
	}
	
	private void onEditProvenance() {
		getProvenanceEditorWidget().configure(this.entityBundle, entityUpdateHandler);
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
		getUploadDialogWidget().configure(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, entityBundle.getEntity(), null, entityUpdateHandler, null, true);
		getUploadDialogWidget().disableMultipleFileUploads();
		getUploadDialogWidget().setUploaderLinkNameVisible(false);
		getUploadDialogWidget().show();
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
				view.showInfo(DisplayConstants.TEXT_LINK_SAVED, DisplayConstants.TEXT_LINK_SAVED);
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
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
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
		getWikiMarkdownEditor().configure(key, new CallbackP<WikiPage>() {
			@Override
			public void invoke(WikiPage param) {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
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
	                view.showInfo("'" + name + "' Page Added", "");
	                Synapse newPlace = new Synapse(entityBundle.getEntity().getId(), getVersion(), EntityArea.WIKI, result.getId());
	                getGlobalApplicationState().getPlaceChanger().goTo(newPlace);
	            }
	            @Override
	            public void onFailure(Throwable caught) {
	                if(!DisplayUtils.handleServiceException(caught, getGlobalApplicationState(), authenticationController.isLoggedIn(), view))
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
		getRenameEntityModalWidget().onRename(this.entity, new Callback() {
			@Override
			public void invoke() {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
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
		getEditFileMetadataModalWidget().configure((FileEntity)entityBundle.getEntity(), originalFileHandle, new Callback() {
			@Override
			public void invoke() {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
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
		getEditProjectMetadataModalWidget().configure((Project)entityBundle.getEntity(), canChangeSettings, new Callback() {
			@Override
			public void invoke() {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
		});
	}
	
	public void onDeleteWiki() {
		final WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		// Get the wiki page title and parent wiki id.  Go to the parent wiki if this delete is successful.
		getSynapseJavascriptClient().getV2WikiPage(key, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage page) {
				// Confirm the delete with the user.
				parentWikiPageId = page.getParentWikiId();
				onDeleteWikiGetHeaderTree(key, page.getTitle());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public void onDeleteWikiGetHeaderTree(WikiPageKey key, final String currentPageTitle) {
		getSynapseClient().getV2WikiHeaderTree(key.getOwnerObjectId(), key.getOwnerObjectType(), new AsyncCallback<List<V2WikiHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
			public void onSuccess(List<V2WikiHeader> wikiHeaders) {
				view.showDeleteWikiModal(wikiPageId, getWikiHeaderMap(wikiHeaders), getWikiChildrenMap(wikiHeaders));		
			};
		});
	}
	public Map<String, V2WikiHeader> getWikiHeaderMap(List<V2WikiHeader> wikiHeaders) {
		Map<String, V2WikiHeader> id2Header = new HashMap<>();
		for (V2WikiHeader v2WikiHeader : wikiHeaders) {
			if (v2WikiHeader.getParentId() == null) {
				//update the root wiki title to be the entity name
				v2WikiHeader.setTitle(entityBundle.getEntity().getName());
			}
			id2Header.put(v2WikiHeader.getId(), v2WikiHeader);
		}
		return id2Header;
	}
	
	public Map<String, List<V2WikiHeader>> getWikiChildrenMap(List<V2WikiHeader> wikiHeaders) {
		Map<String, List<V2WikiHeader>> id2Children = new HashMap<>();
		for (V2WikiHeader v2WikiHeader : wikiHeaders) {
			if (v2WikiHeader.getParentId() != null) {
				List<V2WikiHeader> children = id2Children.get(v2WikiHeader.getParentId());
				if (children == null) {
					children = new ArrayList<>();
					id2Children.put(v2WikiHeader.getParentId(), children);
				}
				children.add(v2WikiHeader);
			}
		}
		return id2Children;
	}
	
	@Override
	public void onConfirmDeleteWiki() {
		postConfirmedDeleteWiki(parentWikiPageId);
	}

	/**
	 * Called after the user has confirmed the delete of the entity.
	 */
	public void postConfirmedDeleteWiki(final String parentWikiId) {
		WikiPageKey key = new WikiPageKey(this.entityBundle.getEntity().getId(), ObjectType.ENTITY.name(), wikiPageId);
		getSynapseClient().deleteV2WikiPage(key, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DELETED, THE + WIKI + WAS_SUCCESSFULLY_DELETED);
				getGlobalApplicationState().getPlaceChanger().goTo(new Synapse(entityBundle.getEntity().getId(), null, EntityArea.WIKI, parentWikiId));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	
	@Override
	public void onDeleteEntity() {
		// Confirm the delete with the user. Mention that everything inside folder will also be deleted if this is a folder entity.
		String display = ARE_YOU_SURE_YOU_WANT_TO_DELETE+this.enityTypeDisplay+" "+this.entity.getName()+"?";
		if (this.entity instanceof Folder) {
			display += DELETE_FOLDER_EXPLANATION;
		}
		
		view.showConfirmDialog(CONFIRM_DELETE_TITLE,display, new Callback() {
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
				view.showInfo(DELETED, THE + enityTypeDisplay + WAS_SUCCESSFULLY_DELETED); 
				// Go to entity's parent
				Place gotoPlace = createDeletePlace();
				getGlobalApplicationState().getPlaceChanger().goTo(gotoPlace);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, getGlobalApplicationState(),isUserAuthenticated, view)) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);			
				}
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
			else gotoPlace = new Synapse(parentId);
		} else {
			gotoPlace = new Profile(authenticationController.getCurrentUserPrincipalId());
		}
		return gotoPlace;
	}
	
	@Override
	public void onShare() {
		getAccessControlListModalWidget().configure(entity, permissions.getCanChangePermissions());
		this.getAccessControlListModalWidget().showSharing(new Callback() {
			@Override
			public void invoke() {
				entityUpdateHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
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
