package org.sagebionetworks.web.client.widget.entity.tabs;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_NAME;
import static org.sagebionetworks.repo.model.EntityBundle.HAS_CHILDREN;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayUtils;
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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class FilesTab implements FilesTabView.Presenter{
	Tab tab;
	FilesTabView view;
	FileTitleBar fileTitleBar;
	BasicTitleBar folderTitleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	
	EntityActionController controller;
	ActionMenuWidget actionMenu;
	FilesBrowser filesBrowser;
	PreviewWidget previewWidget;
	ProvenanceWidget provWidget;
	WikiPageWidget wikiPageWidget;
	EntityUpdatedHandler handler;
	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	
	boolean annotationsShown, fileHistoryShown, isProjectLevelDataShown;
	private static int WIDGET_HEIGHT_PX = 270;
	Map<String,String> configMap;
	
	@Inject
	public FilesTab(FilesTabView view, 
			Tab tab,
			FileTitleBar fileTitleBar,
			BasicTitleBar folderTitleBar,
			Breadcrumb breadcrumb,
			EntityMetadata metadata,
			EntityActionController controller,
			ActionMenuWidget actionMenu,
			FilesBrowser filesBrowser,
			PreviewWidget previewWidget,
			ProvenanceWidget provWidget,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient
			) {
		this.view = view;
		this.tab = tab;
		this.fileTitleBar = fileTitleBar;
		this.folderTitleBar = folderTitleBar;
		this.breadcrumb = breadcrumb;
		this.metadata = metadata;
		this.controller = controller;
		this.actionMenu = actionMenu;
		this.filesBrowser = filesBrowser;
		this.previewWidget = previewWidget;
		this.provWidget = provWidget;
		this.wikiPageWidget = wikiPageWidget;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		
		previewWidget.asWidget().setHeight(WIDGET_HEIGHT_PX + "px");
		view.setFileTitlebar(fileTitleBar.asWidget());
		view.setFolderTitlebar(folderTitleBar.asWidget());
		view.setBreadcrumb(breadcrumb.asWidget());
		view.setFileBrowser(filesBrowser.asWidget());
		view.setPreview(previewWidget.asWidget());
		view.setProvenance(provWidget.asWidget());
		view.setMetadata(metadata.asWidget());
		view.setActionMenu(actionMenu.asWidget());
		view.setWikiPage(wikiPageWidget.asWidget());
		
		tab.configure("Files", view.asWidget());
		actionMenu.addControllerWidget(controller.asWidget());
		
		annotationsShown = false;
		actionMenu.addActionListener(Action.TOGGLE_ANNOTATIONS, new ActionListener() {
			@Override
			public void onAction(Action action) {
				annotationsShown = !annotationsShown;
				FilesTab.this.controller.onAnnotationsToggled(annotationsShown);
				FilesTab.this.metadata.setAnnotationsVisible(annotationsShown);
			}
		});
		fileHistoryShown = false;
		actionMenu.addActionListener(Action.TOGGLE_FILE_HISTORY, new ActionListener() {
			@Override
			public void onAction(Action action) {
				fileHistoryShown = !fileHistoryShown;
				FilesTab.this.controller.onFileHistoryToggled(fileHistoryShown);
				FilesTab.this.metadata.setFileHistoryVisible(fileHistoryShown);
			}
		});
		
		configMap = new HashMap<String,String>();
		configMap.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, Integer.toString(1));		
		configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(WIDGET_HEIGHT_PX-84));
		EntitySelectedHandler entitySelectedHandler = new EntitySelectedHandler() {
			@Override
			public void onSelection(EntitySelectedEvent event) {
				event.getSelectedEntityId();
				getTargetBundle(event.getSelectedEntityId(), null);
			}
		};
		filesBrowser.setEntitySelectedHandler(entitySelectedHandler);
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.setTabClickedCallback(onClickCallback);
	}
	
	public void configure(Entity targetEntity, EntityBundle projectBundle, EntityUpdatedHandler handler) {
		this.handler = handler;
		fileTitleBar.setEntityUpdatedHandler(handler);
		metadata.setEntityUpdatedHandler(handler);
		filesBrowser.setEntityUpdatedHandler(handler);
		
		boolean isFile = targetEntity instanceof FileEntity;
		boolean isFolder = targetEntity instanceof Folder;
		
		//if we are not being configured with a file or folder, then project level should be shown
		isProjectLevelDataShown = !(isFile || isFolder);
		if (isProjectLevelDataShown) {
			//configure based on the project bundle
			setTargetBundle(projectBundle);
		} else {
			getTargetBundle(targetEntity.getId(), getVersionNumber(targetEntity));
		}
	}
	
	public Long getVersionNumber(Entity entity) {
		boolean isVersionable = entity instanceof Versionable;
		return isVersionable ? ((Versionable)entity).getVersionNumber() : null;
	}
	
	public void getTargetBundle(String entityId, Long versionNumber) {
		synAlert.clear();
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS | FILE_HANDLES | ROOT_WIKI_ID | DOI | FILE_NAME;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				setTargetBundle(bundle);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
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
		
		final String entityId = bundle.getEntity().getId();
		final Long versionNumber = getVersionNumber(bundle.getEntity());
		
		boolean isFile = bundle.getEntity() instanceof FileEntity;
		boolean isFolder = bundle.getEntity() instanceof Folder;

		//Breadcrumb
		breadcrumb.configure(bundle.getPath(), EntityArea.FILES);
		
		//action menu
		actionMenu.asWidget().setVisible(isFile || isFolder);
		
		//Preview
		view.setPreviewVisible(isFile);
		//File title bar
		fileTitleBar.asWidget().setVisible(isFile);
		if (isFile) {
			fileTitleBar.configure(bundle);
			previewWidget.configure(bundle);
		}
		
		folderTitleBar.asWidget().setVisible(isFolder);
		if (isFolder) {
			folderTitleBar.configure(bundle);
		}
		
		//Metadata
		metadata.asWidget().setVisible(isFile || isFolder);
		
		//File History
		metadata.setFileHistoryVisible(isFile);
		
		tab.setPlace(new Synapse(entityId, versionNumber, EntityArea.FILES, null));
		  
		
		controller.configure(actionMenu, bundle, bundle.getRootWikiId(), handler);
		
		//File Browser
		boolean isFilesBrowserVisible = isProjectLevelDataShown || isFolder;
		view.setFileBrowserVisible(isFilesBrowserVisible);
		if (isFilesBrowserVisible) {
			filesBrowser.configure(entityId, bundle.getPermissions().getCanCertifiedUserAddChild(), bundle.getPermissions().getIsCertifiedUser());	
		}
		
		//Programmatic Clients
		view.setProgrammaticClientsVisible(isFile);
		if (isFile) {
			view.configureProgrammaticClients(entityId, versionNumber);	
		}

		//Provenance
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(bundle.getEntity().getId(), versionNumber));
		boolean isProvVisible = !(isProjectLevelDataShown || isFolder);
		view.setProvenanceVisible(isProvVisible);
		if (isProvVisible){
			provWidget.configure(null, configMap, null, null);	
		}
		//Created By and Modified By
		view.configureModifiedAndCreatedWidget(bundle.getEntity());
		
		//Wiki Page
		final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		view.setWikiPageWidgetVisible(!isProjectLevelDataShown);
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
		wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), bundle.getRootWikiId(), versionNumber), canEdit, wikiCallback, false, "-files-tab");
		CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
			@Override
			public void invoke(String wikiPageId) {
				wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiPageId, versionNumber), canEdit, wikiCallback, false, "-files-tab");
			}
		};
		wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
	}
	
	public Tab asTab(){
		return tab;
	}
}
