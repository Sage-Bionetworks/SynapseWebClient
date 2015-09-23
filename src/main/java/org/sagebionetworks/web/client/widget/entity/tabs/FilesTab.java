package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

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
	
	boolean annotationsShown, fileHistoryShown;
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
			WikiPageWidget wikiPageWidget
			) {
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
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.setTabClickedCallback(onClickCallback);
	}
	
	public void configure(EntityBundle bundle, final EntityUpdatedHandler handler) {
		boolean isFile = bundle.getEntity() instanceof FileEntity;
		boolean isFolder = bundle.getEntity() instanceof Folder;
		boolean isProject = bundle.getEntity() instanceof Project;
		
		breadcrumb.configure(bundle.getPath(), EntityArea.FILES);
		
		if (bundle.getEntity() instanceof FileEntity) {
			fileTitleBar.configure(bundle);
			fileTitleBar.asWidget().setVisible(true);
		} else {
			fileTitleBar.asWidget().setVisible(false);
		}
		
		final String entityId = bundle.getEntity().getId();
		boolean isVersionable = bundle.getEntity() instanceof Versionable;
		final Long versionNumber = isVersionable ? ((Versionable)bundle.getEntity()).getVersionNumber() : null;
		
		tab.setPlace(new Synapse(entityId, versionNumber, EntityArea.FILES, null));
		
		fileTitleBar.setEntityUpdatedHandler(handler);
		metadata.setEntityUpdatedHandler(handler);
		filesBrowser.setEntityUpdatedHandler(handler);
		
		previewWidget.configure(bundle);
		controller.configure(actionMenu, bundle, bundle.getRootWikiId(), handler);
		
		boolean isFilesBrowserVisible = isProject || isFolder;
		view.setFileBrowserVisible(isFilesBrowserVisible);
		if (isFilesBrowserVisible) {
			filesBrowser.configure(entityId, bundle.getPermissions().getCanCertifiedUserAddChild(), bundle.getPermissions().getIsCertifiedUser());	
		}
		
		view.setProgrammaticClientsVisible(isFile);
		if (isFile) {
			view.configureProgrammaticClients(entityId, versionNumber);	
		}
		
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(bundle.getEntity().getId(), versionNumber));
		
		boolean isProvVisible = !(isProject || isFolder);
		view.setProvenanceVisible(isProvVisible);
		if (isProvVisible){
			provWidget.configure(null, configMap, null, null);	
		}
		view.configureModifiedAndCreatedWidget(bundle.getEntity());
		
		final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		
		view.setWikiPageWidgetVisible(true);
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
			
		wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), bundle.getRootWikiId(), versionNumber), canEdit, wikiCallback, true);
		
		metadata.setFileHistoryVisible(isFile);
		
		CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
			@Override
			public void invoke(String wikiPageId) {
				wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiPageId, versionNumber), canEdit, wikiCallback, true);
			}
		};
		wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
	}
	
	public Tab asTab(){
		return tab;
	}
}
