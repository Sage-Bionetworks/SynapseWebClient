package org.sagebionetworks.web.client.widget.entity.tabs;

import static org.sagebionetworks.repo.model.EntityBundle.*;

import java.util.HashMap;
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
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class FilesTab implements FilesTabView.Presenter{
	Tab tab;
	FilesTabView view;
	FileTitleBar fileTitleBar;
	BasicTitleBar folderTitleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	FilesBrowser filesBrowser;
	PreviewWidget previewWidget;
	WikiPageWidget wikiPageWidget;
	EntityUpdatedHandler handler;
	PortalGinInjector ginInjector;
	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	Entity currentEntity;
	String currentEntityId;
	Long currentVersionNumber;
	boolean annotationsShown, fileHistoryShown;
	
	private static int WIDGET_HEIGHT_PX = 270;
	Map<String,String> configMap;
	
	CallbackP<Boolean> showProjectInfoCallack;
	EntityBundle projectBundle;
	Throwable projectBundleLoadError;
	String projectEntityId;
	
	@Inject
	public FilesTab(FilesTabView view, 
			Tab tab,
			FileTitleBar fileTitleBar,
			BasicTitleBar folderTitleBar,
			Breadcrumb breadcrumb,
			EntityMetadata metadata,
			FilesBrowser filesBrowser,
			PreviewWidget previewWidget,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalApplicationState
			) {
		this.view = view;
		this.tab = tab;
		this.fileTitleBar = fileTitleBar;
		this.folderTitleBar = folderTitleBar;
		this.breadcrumb = breadcrumb;
		this.metadata = metadata;
		this.filesBrowser = filesBrowser;
		this.previewWidget = previewWidget;
		this.wikiPageWidget = wikiPageWidget;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		this.ginInjector = ginInjector;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
		
		previewWidget.setHeight(WIDGET_HEIGHT_PX + "px");
		view.setFileTitlebar(fileTitleBar.asWidget());
		view.setFolderTitlebar(folderTitleBar.asWidget());
		view.setBreadcrumb(breadcrumb.asWidget());
		view.setFileBrowser(filesBrowser.asWidget());
		view.setPreview(previewWidget.asWidget());
		view.setMetadata(metadata.asWidget());
		view.setWikiPage(wikiPageWidget.asWidget());
		view.setSynapseAlert(synAlert.asWidget());
		
		tab.configure("Files", view.asWidget());
		
		configMap = new HashMap<String,String>();
		configMap.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, Integer.toString(1));		
		configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(WIDGET_HEIGHT_PX-84));
		EntitySelectedHandler entitySelectedHandler = new EntitySelectedHandler() {
			@Override
			public void onSelection(EntitySelectedEvent event) {
				getTargetBundleAndDisplay(event.getSelectedEntityId(), null);
			}
		};
		filesBrowser.setEntitySelectedHandler(entitySelectedHandler);
		initBreadcrumbLinkClickedHandler();
	}

	public void initBreadcrumbLinkClickedHandler() {
		CallbackP<Place> breadcrumbClicked = new CallbackP<Place>() {
			public void invoke(Place place) {
				//if this is the project id, then just reconfigure from the project bundle
				Synapse synapse = (Synapse)place;
				String entityId = synapse.getEntityId();
				Long versionNumber = synapse.getVersionNumber();
				if (entityId.equals(projectEntityId)) {
				    currentVersionNumber = null;
				    showProjectLevelUI();
				    tab.showTab();
				} else {
				    getTargetBundleAndDisplay(entityId, versionNumber);
				}
			};
		};
		breadcrumb.setLinkClickedHandler(breadcrumbClicked);
	}
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void setShowProjectInfoCallback(CallbackP<Boolean> callback) {
		showProjectInfoCallack = callback;
		tab.addTabClickedCallback(new CallbackP<Tab>() {
			@Override
			public void invoke(Tab param) {
				boolean isProject = currentEntity instanceof Project;
				showProjectInfoCallack.invoke(isProject);
			}
		});
	}
	
	public void resetView() {
		synAlert.clear();
		view.setFileTitlebarVisible(false);
		view.setFolderTitlebarVisible(false);
		view.setPreviewVisible(false);
		view.setMetadataVisible(false);
		view.setWikiPageWidgetVisible(false);
		view.setFileBrowserVisible(false);
		view.clearActionMenuContainer();
		breadcrumb.clear();
		view.clearModifiedAndCreatedWidget();
		view.setProgrammaticClientsVisible(false);
		view.setProvenanceVisible(false);
	}
	
	public void setProject(String projectEntityId, EntityBundle projectBundle, Throwable projectBundleLoadError) {
		this.projectEntityId = projectEntityId;
		this.projectBundle = projectBundle;
		this.projectBundleLoadError = projectBundleLoadError;
	}
	
	public void configure(Entity targetEntity, EntityUpdatedHandler handler, Long versionNumber) {
		this.handler = handler;
		synAlert.clear();
		fileTitleBar.setEntityUpdatedHandler(handler);
		metadata.setEntityUpdatedHandler(handler);
		filesBrowser.setEntityUpdatedHandler(handler);
		
		//reset view
		resetView();
		
		boolean isFile = targetEntity instanceof FileEntity;
		boolean isFolder = targetEntity instanceof Folder;
		
		tab.setPlace(new Synapse(currentEntityId, currentVersionNumber, null, null));
		//if we are not being configured with a file or folder, then project level should be shown
		if (!(isFile || isFolder)) {
			//configure based on the project bundle
			showProjectLevelUI();
		} else {
			getTargetBundleAndDisplay(targetEntity.getId(), versionNumber);
		}
	}
	
	public void showProjectLevelUI() {
		tab.setPlace(new Synapse(projectEntityId, null, EntityArea.FILES, null));
		if (projectBundle != null) {
			setTargetBundle(projectBundle);	
		} else {
			showError(projectBundleLoadError);
		}
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
	  
	public void getTargetBundleAndDisplay(String entityId, Long versionNumber) {
		//only ask for it if we are showing a different entity/version
		if (equal(currentEntityId,entityId) && equal(currentVersionNumber, versionNumber)) {
			return;
		}
		
		currentEntityId = entityId;
		currentVersionNumber = versionNumber;
		synAlert.clear();
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS | FILE_HANDLES | ROOT_WIKI_ID | DOI | FILE_NAME;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				if (bundle.getEntity() instanceof Link) {
					//short circuit.  redirect to target entity
					Reference ref = ((Link)bundle.getEntity()).getLinksTo();
					//go to link target
					String entityId = ref.getTargetId();
					Long versionNumber = ref.getTargetVersionNumber();
					globalApplicationState.getPlaceChanger().goTo(new Synapse(entityId, versionNumber, null, null));
					return;
				}
				
				setTargetBundle(bundle);
				tab.showTab();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showError(caught);
				tab.setPlace(new Synapse(currentEntityId, currentVersionNumber, null, null));
				tab.showTab();
			}	
		};
		if (versionNumber == null) {
			synapseClient.getEntityBundle(entityId, mask, callback);
		} else {
			synapseClient.getEntityBundleForVersion(entityId, versionNumber, mask, callback);
		}
	}
	
	public void setTargetBundle(EntityBundle bundle) {
		EntityPresenter.filterToDownloadARs(bundle);
		currentEntity = bundle.getEntity();
		currentEntityId = currentEntity.getId();
		boolean isFile = currentEntity instanceof FileEntity;
		boolean isFolder = currentEntity instanceof Folder;
		boolean isProject = currentEntity instanceof Project;
		
		showProjectInfoCallack.invoke(isProject);
		
		//Breadcrumb
		breadcrumb.configure(bundle.getPath(), EntityArea.FILES);
		view.clearActionMenuContainer();
		//Preview
		view.setPreviewVisible(isFile);
		//File title bar
		view.setFileTitlebarVisible(isFile);
		if (isFile) {
			fileTitleBar.configure(bundle);
			previewWidget.configure(bundle);
		}
		
		view.setFolderTitlebarVisible(isFolder);
		if (isFolder) {
			folderTitleBar.configure(bundle);
		}
		
		//Metadata
		boolean isMetadataVisible = isFile || isFolder;
		view.setMetadataVisible(isMetadataVisible);
		if (isMetadataVisible) {
			initActionMenu(bundle);
			metadata.setEntityBundle(bundle, currentVersionNumber);
			//File History
			metadata.setFileHistoryVisible(isFile && currentVersionNumber != null);	
		}
		EntityArea area = isProject ? EntityArea.FILES : null;
		tab.setPlace(new Synapse(currentEntityId, currentVersionNumber, area, null));
		
		//File Browser
		boolean isFilesBrowserVisible = isProject || isFolder;
		view.setFileBrowserVisible(isFilesBrowserVisible);
		if (isFilesBrowserVisible) {
			filesBrowser.configure(currentEntityId, bundle.getPermissions().getCanCertifiedUserAddChild(), bundle.getPermissions().getIsCertifiedUser());	
		}
		
		//Programmatic Clients
		view.setProgrammaticClientsVisible(isFile);
		if (isFile) {
			view.configureProgrammaticClients(currentEntityId, currentVersionNumber);	
		}

		//Provenance
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(currentEntityId, currentVersionNumber));
		view.setProvenanceVisible(isFile);
		if (isFile){
			ProvenanceWidget provWidget = ginInjector.getProvenanceRenderer();
			view.setProvenance(provWidget.asWidget());
			provWidget.configure(null, configMap, null, null);
		}
		//Created By and Modified By
		view.configureModifiedAndCreatedWidget(currentEntity);
		
		//Wiki Page
		boolean isWikiPageVisible = !isProject;
		view.setWikiPageWidgetVisible(isWikiPageVisible);
		if (isWikiPageVisible) {
			final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
			final WikiPageWidget.Callback wikiCallback = new WikiPageWidget.Callback() {
					@Override
					public void pageUpdated() {
						handler.onPersistSuccess(new EntityUpdatedEvent());
					}
					@Override
					public void noWikiFound() {
						view.setWikiPageWidgetVisible(false);
					}
				};
			wikiPageWidget.configure(new WikiPageKey(currentEntityId, ObjectType.ENTITY.toString(), bundle.getRootWikiId(), currentVersionNumber), canEdit, wikiCallback, false);
			CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
				@Override
				public void invoke(String wikiPageId) {
					wikiPageWidget.configure(new WikiPageKey(currentEntityId, ObjectType.ENTITY.toString(), wikiPageId, currentVersionNumber), canEdit, wikiCallback, false);
				}
			};
			wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
		}
	}
	
	public void initActionMenu(EntityBundle bundle) {
		ActionMenuWidget actionMenu = ginInjector.createActionMenuWidget();
		view.setActionMenu(actionMenu.asWidget());
		final EntityActionController controller = ginInjector.createEntityActionController();
		actionMenu.addControllerWidget(controller.asWidget());
		
		annotationsShown = false;
		actionMenu.addActionListener(Action.TOGGLE_ANNOTATIONS, new ActionListener() {
			@Override
			public void onAction(Action action) {
				annotationsShown = !annotationsShown;
				controller.onAnnotationsToggled(annotationsShown);
				FilesTab.this.metadata.setAnnotationsVisible(annotationsShown);
			}
		});
		fileHistoryShown = false;
		actionMenu.addActionListener(Action.TOGGLE_FILE_HISTORY, new ActionListener() {
			@Override
			public void onAction(Action action) {
				fileHistoryShown = !fileHistoryShown;
				controller.onFileHistoryToggled(fileHistoryShown);
				FilesTab.this.metadata.setFileHistoryVisible(fileHistoryShown);
			}
		});
		controller.configure(actionMenu, bundle, bundle.getRootWikiId(), handler);
	}
	
	/**
	 * Return the entity currently being shown by this tab.
	 * @return
	 */
	public Entity getCurrentEntity() {
		return currentEntity;
	}
	
	public Tab asTab(){
		return tab;
	}
}
