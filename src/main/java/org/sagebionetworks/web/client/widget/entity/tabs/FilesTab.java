package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;

import com.google.gwt.user.client.ui.Widget;
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
	boolean annotationsShown, fileHistoryShown;
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
			PreviewWidget previewWidget
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
		
		view.setFileTitlebar(fileTitleBar.asWidget());
		view.setFolderTitlebar(folderTitleBar.asWidget());
		view.setBreadcrumb(breadcrumb.asWidget());
		view.setFileDescription(Widget w);
		view.setFileBrowser(filesBrowser.asWidget());
		view.setPreview(previewWidget.asWidget());
		view.setProvenance(Widget w);
		view.setModifiedAndCreated(Widget w);
		view.setMetadata(metadata.asWidget());
		view.setActionMenu(Widget w);
		view.setWikiPage(Widget w);
		
		
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
		
	}
	
	public void showTab() {
		tab.showTab();
	}
	
	public void hideTab() {
		tab.hideTab();
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.setTabClickedCallback(onClickCallback);
	}
	
	public void configure(EntityBundle bundle, EntityUpdatedHandler handler) {
		breadcrumb.configure(bundle.getPath(), EntityArea.FILES);
		String entityId = bundle.getEntity().getId();
		Long versionNumber = null;
		if (bundle.getEntity() instanceof Versionable) {
			versionNumber = ((Versionable)bundle.getEntity()).getVersionNumber();
		}
		tab.setPlace(new Synapse(entityId, versionNumber, EntityArea.FILES, null));
		
		fileTitleBar.setEntityUpdatedHandler(handler);
		metadata.setEntityUpdatedHandler(handler);
		filesBrowser.setEntityUpdatedHandler(handler);
		
		previewWidget.configure(bundle);
		controller.configure(actionMenu, bundle, bundle.getRootWikiId(), handler);
		filesBrowser.configure(entityId, bundle.getPermissions().getCanCertifiedUserAddChild(), bundle.getPermissions().getIsCertifiedUser());
		view.configureProgrammaticClients(entityId, versionNumber);
	}
	
	public Tab asTab(){
		return tab;
	}
	
	public void setFileHistoryVisible(boolean isVisible) {
		metadata.setFileHistoryVisible(isVisible);
	}

}
