package org.sagebionetworks.web.client.widget.entity.tabs;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import java.util.Map;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
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
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.provenance.v2.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.refresh.EntityRefreshAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class FilesTab {

  Tab tab;
  FilesTabView view;
  BasicTitleBar titleBar;
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
  ModifiedCreatedByWidget modifiedCreatedBy;

  Map<String, String> configMap;

  EntityBundle projectBundle;
  Throwable projectBundleLoadError;
  String projectEntityId;
  EntityBundle entityBundle;
  CallbackP<String> entitySelectedCallback;
  ProvenanceWidget provWidget;
  AddToDownloadListV2 addToDownloadListWidget;

  @Inject
  public FilesTab(Tab tab, PortalGinInjector ginInjector) {
    this.tab = tab;
    this.ginInjector = ginInjector;
    tab.configure(
      "Files",
      "file",
      "Organize your data by uploading files into a directory structure built in the Files section.",
      WebConstants.DOCS_URL + "Files-and-Folders.2048458885.html",
      EntityArea.FILES
    );
    tab.configureOrientationBanner(
      "Files",
      "Getting Started With Files",
      "Synapse files can be created by uploading content from your local computer or linking to digital files on the web. You can annotate files with custom metadata, embed files into Synapse wiki pages, or associate them with a DOI.",
      null,
      null,
      "Learn More About Files",
      "https://help.synapse.org/docs/Managing-Files-and-Folders.2058846522.html"
    );
  }

  public void lazyInject() {
    if (view == null) {
      this.view = ginInjector.getFilesTabView();
      this.titleBar = ginInjector.getBasicTitleBar();
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
      this.discussionThreadListWidget =
        ginInjector.getDiscussionThreadListWidget();
      this.addToDownloadListWidget = ginInjector.getAddToDownloadListV2();
      tab.setContent(view.asWidget());
      previewWidget.addStyleName("min-height-200");
      view.setTitlebar(titleBar.asWidget());
      view.setFolderTitlebar(folderTitleBar.asWidget());
      view.setBreadcrumb(breadcrumb.asWidget());
      view.setFileBrowser(filesBrowser.asWidget());
      view.setPreview(previewWidget.asWidget());
      view.setMetadata(metadata.asWidget());
      view.setWikiPage(wikiPageWidget.asWidget());
      view.setSynapseAlert(synAlert.asWidget());
      view.setModifiedCreatedBy(modifiedCreatedBy);
      view.setDiscussionThreadListWidget(discussionThreadListWidget.asWidget());
      view.setAddToDownloadListWidget(addToDownloadListWidget.asWidget());
      view.setFilesTab(this);
      discussionThreadListWidget.setThreadIdClickedCallback(bundle ->
        globalApplicationState
          .getPlaceChanger()
          .goTo(
            TopicUtils.getThreadPlace(bundle.getProjectId(), bundle.getId())
          )
      );

      configMap =
        org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget.getDefaultWidgetDescriptor();
      initBreadcrumbLinkClickedHandler();
    }
  }

  public void initBreadcrumbLinkClickedHandler() {
    CallbackP<Place> breadcrumbClicked = place -> {
      // if this is the project id, then just reconfigure from the project bundle
      Synapse synapse = (Synapse) place;
      String entityId = synapse.getEntityId();
      entitySelectedCallback.invoke(entityId);
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
      view.setFileFolderUIVisible(false);
      view.setWikiPageWidgetVisible(false);
      view.setFileBrowserVisible(false);
      view.clearRefreshAlert();
      breadcrumb.clear();
      view.setProvenanceVisible(false);
      modifiedCreatedBy.setVisible(false);
      view.setDiscussionThreadListWidgetVisible(false);
      filesBrowser.clear();
    }
  }

  public void setProject(
    String projectEntityId,
    EntityBundle projectBundle,
    Throwable projectBundleLoadError
  ) {
    this.projectEntityId = projectEntityId;
    this.projectBundle = projectBundle;
    this.projectBundleLoadError = projectBundleLoadError;
  }

  public void configure(EntityBundle targetEntityBundle, Long versionNumber) {
    lazyInject();
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
    tab.setEntityNameAndPlace(
      title,
      new Synapse(projectEntityId, null, EntityArea.FILES, null)
    );
  }

  public void showError(Throwable error) {
    resetView();
    synAlert.handleException(error);
  }

  public Long getVersionNumber(Entity entity) {
    boolean isVersionable = entity instanceof Versionable;
    return isVersionable ? ((Versionable) entity).getVersionNumber() : null;
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
      // short circuit. redirect to target entity
      Reference ref = ((Link) bundle.getEntity()).getLinksTo();
      // go to link target
      String entityId = ref.getTargetId();
      Long shownVersionNumber = ref.getTargetVersionNumber();
      globalApplicationState
        .getPlaceChanger()
        .goTo(new Synapse(entityId, shownVersionNumber, null, null));
      return;
    }

    Entity currentEntity = bundle.getEntity();
    final String currentEntityId = currentEntity.getId();
    boolean isFile = currentEntity instanceof FileEntity;
    boolean isFolder = currentEntity instanceof Folder;
    boolean isProject = currentEntity instanceof Project;

    EntityRefreshAlert entityRefreshAlert = ginInjector.getEntityRefreshAlert();
    view.setRefreshAlert(entityRefreshAlert.asWidget());
    entityRefreshAlert.configure(currentEntity.getId());

    if (!(isFile || isFolder)) {
      // configure based on the project bundle
      showProjectLevelUI();
    } else {
      breadcrumb.configure(bundle.getPath(), EntityArea.FILES);
    }

    view.showLoading(false);
    // Preview
    view.setPreviewVisible(isFile && !bundle.getFileHandles().isEmpty());

    // File title bar
    view.setFileTitlebarVisible(isFile);
    if (isFile) {
      titleBar.configure(bundle, tab.getEntityActionMenu());

      previewWidget.configure(bundle);
      discussionThreadListWidget.configure(currentEntityId, null, null);
      view.setDiscussionText(currentEntity.getName());
    }
    view.setDiscussionThreadListWidgetVisible(isFile);
    view.setFolderTitlebarVisible(isFolder);
    if (isFolder) {
      folderTitleBar.configure(bundle, tab.getEntityActionMenu());
    }

    // Metadata
    boolean isFileOrFolder = isFile || isFolder;
    view.setFileFolderUIVisible(isFileOrFolder);
    if (isFileOrFolder) {
      metadata.configure(bundle, versionNumber, tab.getEntityActionMenu());
    }
    Boolean isCurrentVersion = bundle.getEntity() instanceof VersionableEntity
      ? ((VersionableEntity) bundle.getEntity()).getIsLatestVersion()
      : null;
    if (isCurrentVersion == null) {
      isCurrentVersion = true;
    }
    tab.configureEntityActionController(
      bundle,
      isCurrentVersion,
      null,
      addToDownloadListWidget
    );

    EntityArea area = isProject ? EntityArea.FILES : null;
    tab.setEntityNameAndPlace(
      bundle.getEntity().getName(),
      new Synapse(currentEntityId, versionNumber, area, null)
    );

    // File Browser
    boolean isFilesBrowserVisible = isProject || isFolder;
    view.setFileBrowserVisible(isFilesBrowserVisible);
    if (isFilesBrowserVisible) {
      filesBrowser.configure(currentEntityId);
      // the action menu is added to the title bar if Folder, to the browser if Project
      if (isProject) {
        filesBrowser.setActionMenu(tab.getEntityActionMenu());
        filesBrowser.setAddToDownloadListWidget(addToDownloadListWidget);
      } else {
        view.setAddToDownloadListWidget(addToDownloadListWidget);
      }
    }

    // Provenance
    configMap.put(
      WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY,
      DisplayUtils.createEntityVersionString(currentEntityId, versionNumber)
    );
    view.setProvenanceVisible(isFile);
    if (isFile) {
      if (DisplayUtils.isInTestWebsite(ginInjector.getCookieProvider())) {
        provWidget = ginInjector.getProvenanceRendererV2();
        view.setProvenance(provWidget.asWidget());
        provWidget.configure(configMap);
      } else {
        org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget provWidget = ginInjector.getProvenanceRenderer();
        view.setProvenance(provWidget.asWidget());
        provWidget.configure(configMap);
      }
    }
    // Created By and Modified By
    modifiedCreatedBy.configure(currentEntity.getId(), versionNumber);

    // Wiki Page
    boolean isWikiPageVisible = !isProject;
    view.setWikiPageWidgetVisible(isWikiPageVisible);
    if (isWikiPageVisible) {
      final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
      final WikiPageWidget.Callback wikiCallback = new WikiPageWidget.Callback() {
        @Override
        public void pageUpdated() {
          ginInjector
            .getEventBus()
            .fireEvent(
              new EntityUpdatedEvent(entityBundle.getEntity().getId())
            );
        }

        @Override
        public void noWikiFound() {
          view.setWikiPageWidgetVisible(false);
        }
      };
      wikiPageWidget.configure(
        new WikiPageKey(
          currentEntityId,
          ObjectType.ENTITY.toString(),
          bundle.getRootWikiId(),
          versionNumber
        ),
        canEdit,
        wikiCallback
      );
      CallbackP<String> wikiReloadHandler = wikiPageId ->
        wikiPageWidget.configure(
          new WikiPageKey(
            currentEntityId,
            ObjectType.ENTITY.toString(),
            wikiPageId,
            versionNumber
          ),
          canEdit,
          wikiCallback
        );
      wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
    }
  }

  public void onExpand() {
    if (provWidget != null) {
      configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, "600");
      provWidget.configure(configMap);
    }
  }

  public void onExpandClosed() {
    if (provWidget != null) {
      configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, "200");
      provWidget.configure(configMap);
    }
  }

  public void setEntitySelectedCallback(
    CallbackP<String> entitySelectedCallback
  ) {
    this.entitySelectedCallback = entitySelectedCallback;
  }

  public Tab asTab() {
    return tab;
  }
}
