package org.sagebionetworks.web.client.widget.entity.tabs;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

public class FilesTab {
	Tab tab;
	FilesTabView view;
	FileTitleBar fileTitleBar;
	BasicTitleBar folderTitleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	FilesBrowser filesBrowser;
	PreviewWidget previewWidget;
	WikiPageWidget wikiPageWidget;
	PortalGinInjector ginInjector;
	StuAlert synAlert;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	DiscussionThreadListWidget discussionThreadListWidget;
	ActionMenuWidget actionMenu;
	ModifiedCreatedByWidget modifiedCreatedBy;
	
	public static int WIDGET_HEIGHT_PX = 270;
	Map<String,String> configMap;
	
	EntityBundle projectBundle;
	Throwable projectBundleLoadError;
	String projectEntityId;
	EntityBundle entityBundle;
	CallbackP<String> entitySelectedCallback;
	
	@Inject
	public FilesTab(Tab tab, PortalGinInjector ginInjector) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure("Files", "Organize your data by uploading files into a directory structure built in the Files section.", WebConstants.DOCS_URL + "versioning.html");
	}
	
	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getFilesTabView();
			this.fileTitleBar = ginInjector.getFileTitleBar();
			this.folderTitleBar = ginInjector.getBasicTitleBar();
			this.breadcrumb = ginInjector.getBreadcrumb();
			this.metadata = ginInjector.getEntityMetadata();
			this.filesBrowser = ginInjector.getFilesBrowser();
			filesBrowser.setEntityClickedHandler(entitySelectedCallback);
			this.previewWidget = ginInjector.getPreviewWidget();
			this.wikiPageWidget = ginInjector.getWikiPageWidget();
			this.synAlert = ginInjector.getStuAlert();
			this.synapseClient = ginInjector.getSynapseClientAsync();
			fixServiceEntryPoint(synapseClient);
			this.globalApplicationState = ginInjector.getGlobalApplicationState();
			this.modifiedCreatedBy = ginInjector.getModifiedCreatedByWidget();
			this.discussionThreadListWidget = ginInjector.getDiscussionThreadListWidget();
			tab.setContent(view.asWidget());
			previewWidget.addStyleName("min-height-200");
			view.setFileTitlebar(fileTitleBar.asWidget());
			view.setFolderTitlebar(folderTitleBar.asWidget());
			view.setBreadcrumb(breadcrumb.asWidget());
			view.setFileBrowser(filesBrowser.asWidget());
			view.setPreview(previewWidget.asWidget());
			view.setMetadata(metadata.asWidget());
			view.setWikiPage(wikiPageWidget.asWidget());
			view.setSynapseAlert(synAlert.asWidget());
			view.setModifiedCreatedBy(modifiedCreatedBy);
			view.setDiscussionThreadListWidget(discussionThreadListWidget.asWidget());
			discussionThreadListWidget.setThreadIdClickedCallback(new CallbackP<DiscussionThreadBundle>(){
	
				@Override
				public void invoke(DiscussionThreadBundle bundle) {
					globalApplicationState.getPlaceChanger().goTo(TopicUtils.getThreadPlace(bundle.getProjectId(), bundle.getId()));
				}
			});
			
			configMap = ProvenanceWidget.getDefaultWidgetDescriptor();
			initBreadcrumbLinkClickedHandler();
		}
	}
	public void initBreadcrumbLinkClickedHandler() {
		CallbackP<Place> breadcrumbClicked = new CallbackP<Place>() {
			public void invoke(Place place) {
				//if this is the project id, then just reconfigure from the project bundle
				Synapse synapse = (Synapse)place;
				String entityId = synapse.getEntityId();
				entitySelectedCallback.invoke(entityId);
			};
		};
		breadcrumb.setLinkClickedHandler(breadcrumbClicked);
	}
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void resetView() {
		if (view != null) {
			synAlert.clear();
			view.setFileTitlebarVisible(false);
			view.setFolderTitlebarVisible(false);
			view.setPreviewVisible(false);
			view.setMetadataVisible(false);
			view.setWikiPageWidgetVisible(false);
			view.setFileBrowserVisible(false);
			view.clearActionMenuContainer();
			view.clearRefreshAlert();
			breadcrumb.clear();
			view.setProvenanceVisible(false);
			modifiedCreatedBy.setVisible(false);
			view.setDiscussionThreadListWidgetVisible(false);
			filesBrowser.clear();
		}
	}
	
	public void setProject(String projectEntityId, EntityBundle projectBundle, Throwable projectBundleLoadError) {
		this.projectEntityId = projectEntityId;
		this.projectBundle = projectBundle;
		this.projectBundleLoadError = projectBundleLoadError;
	}
	
	public void configure(EntityBundle targetEntityBundle, Long versionNumber, ActionMenuWidget actionMenu) {
		lazyInject();
		this.actionMenu = actionMenu;
		view.showLoading(true);
		setTargetBundle(targetEntityBundle, versionNumber);
	}
	
	public void showProjectLevelUI() {
		String title = projectEntityId;
		if (projectBundle != null) {
			title = projectBundle.getEntity().getName();
		} else {
			showError(projectBundleLoadError);
		}
		tab.setEntityNameAndPlace(title, new Synapse(projectEntityId, null, EntityArea.FILES, null));
	}
	
	public void showError(Throwable error) {
		resetView();
		synAlert.handleException(error);
	}
	
	public Long getVersionNumber(Entity entity) {
		boolean isVersionable = entity instanceof Versionable;
		return isVersionable ? ((Versionable)entity).getVersionNumber() : null;
	}
	  /**
	   * Determines whether two possibly-null objects are equal. Returns:
	  */
	public static boolean equal(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}
	
	public void setTargetBundle(EntityBundle bundle, final Long versionNumber) {
		resetView();
		if (bundle.getEntity() instanceof Link) {
			//short circuit.  redirect to target entity
			Reference ref = ((Link)bundle.getEntity()).getLinksTo();
			//go to link target
			String entityId = ref.getTargetId();
			Long shownVersionNumber = ref.getTargetVersionNumber();
			globalApplicationState.getPlaceChanger().goTo(new Synapse(entityId, shownVersionNumber, null, null));
			return;
		}
		
		Entity currentEntity = bundle.getEntity();
		final String currentEntityId = currentEntity.getId();
		boolean isFile = currentEntity instanceof FileEntity;
		boolean isFolder = currentEntity instanceof Folder;
		boolean isProject = currentEntity instanceof Project;

		RefreshAlert refreshAlert = ginInjector.getRefreshAlert();
		view.setRefreshAlert(refreshAlert.asWidget());
		refreshAlert.configure(currentEntity.getId(), ObjectType.ENTITY);
		
		if (!(isFile || isFolder)) {
			//configure based on the project bundle
			showProjectLevelUI();
		} else {
			breadcrumb.configure(bundle.getPath(), EntityArea.FILES);
		}
		
		view.showLoading(false);
		view.clearActionMenuContainer();
		//Preview
		view.setPreviewVisible(isFile && !bundle.getFileHandles().isEmpty());		
		
		//File title bar
		view.setFileTitlebarVisible(isFile);
		if (isFile) {
			fileTitleBar.configure(bundle);
			previewWidget.configure(bundle);
			discussionThreadListWidget.configure(currentEntityId, null, null);
			view.setDiscussionText(currentEntity.getName());
		}
		view.setDiscussionThreadListWidgetVisible(isFile);
		view.setFolderTitlebarVisible(isFolder);
		if (isFolder) {
			folderTitleBar.configure(bundle);
		}
		
		//Metadata
		boolean isMetadataVisible = isFile || isFolder;
		view.setMetadataVisible(isMetadataVisible);
		if (isMetadataVisible) {
			metadata.configure(bundle, versionNumber, actionMenu);
		}
		EntityArea area = isProject ? EntityArea.FILES : null;
		tab.setEntityNameAndPlace(bundle.getEntity().getName(), new Synapse(currentEntityId, versionNumber, area, null));
		
		//File Browser
		boolean isFilesBrowserVisible = isProject || isFolder;
		view.setFileBrowserVisible(isFilesBrowserVisible);
		if (isFilesBrowserVisible) {
			filesBrowser.configure(currentEntityId);	
		}
		
		//Provenance
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(currentEntityId, versionNumber));
		view.setProvenanceVisible(isFile);
		if (isFile){
			ProvenanceWidget provWidget = ginInjector.getProvenanceRenderer();
			view.setProvenance(provWidget.asWidget());
			provWidget.configure(configMap);
		}
		//Created By and Modified By
		modifiedCreatedBy.configure(currentEntity.getCreatedOn(), currentEntity.getCreatedBy(), 
				currentEntity.getModifiedOn(), currentEntity.getModifiedBy());
		
		//Wiki Page
		boolean isWikiPageVisible = !isProject;
		view.setWikiPageWidgetVisible(isWikiPageVisible);
		if (isWikiPageVisible) {
			final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
			final WikiPageWidget.Callback wikiCallback = new WikiPageWidget.Callback() {
					@Override
					public void pageUpdated() {
						ginInjector.getEventBus().fireEvent(new EntityUpdatedEvent());
					}
					@Override
					public void noWikiFound() {
						view.setWikiPageWidgetVisible(false);
					}
				};
			wikiPageWidget.configure(new WikiPageKey(currentEntityId, ObjectType.ENTITY.toString(), bundle.getRootWikiId(), versionNumber), canEdit, wikiCallback);
			CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
				@Override
				public void invoke(String wikiPageId) {
					wikiPageWidget.configure(new WikiPageKey(currentEntityId, ObjectType.ENTITY.toString(), wikiPageId, versionNumber), canEdit, wikiCallback);
				}
			};
			wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
		}
	}

	public void setEntitySelectedCallback(CallbackP<String> entitySelectedCallback) {
		this.entitySelectedCallback = entitySelectedCallback;
	}
	
	public Tab asTab(){
		return tab;
	}
}
