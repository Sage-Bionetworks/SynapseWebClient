package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventHandler;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.table.EntityRefCollectionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.EntityId2BundleCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.ChangeSynapsePlaceEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.file.ProjectTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DatasetsTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tabs;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;

public class EntityPageTop implements SynapseWidgetPresenter, IsWidget {

  private final EntityPageTopView view;
  private EntityBundle currentTargetEntityBundle, projectBundle, filesEntityBundle, tablesEntityBundle, dockerEntityBundle, datasetsEntityBundle;
  private Throwable projectBundleLoadError;
  private Entity entity;
  private final SynapseJavascriptClient synapseJavascriptClient;

  private Synapse.EntityArea area;
  private String initialAreaToken;
  private String wikiAreaToken, tablesAreaToken, discussionAreaToken, dockerAreaToken, datasetsAreaToken;
  private Long currentTargetVersionNumber, filesVersionNumber, tablesVersionNumber, datasetsVersionNumber;
  private EntityHeader projectHeader;

  private final Tabs tabs;
  private final WikiTab wikiTab;
  private final FilesTab filesTab;
  private final TablesTab tablesTab;
  private final DatasetsTab datasetsTab;
  private final ChallengeTab challengeTab;
  private final DiscussionTab discussionTab;
  private final DockerTab dockerTab;
  private final ProjectTitleBar projectTitleBar;
  private final EntityMetadata projectMetadata;
  private final SynapseClientAsync synapseClient;
  // how many tabs have been marked as visible
  private int visibleTabCount;

  private final EntityActionController projectActionController;
  private final EntityActionMenu projectActionMenu;
  private final PlaceChanger placeChanger;
  private final CookieProvider cookies;
  private final EventBus eventBus;
  private final EntityId2BundleCache entityId2BundleCache;
  public boolean pushTabUrlToBrowserHistory = false;
  public static final EntityBundleRequest ALL_PARTS_REQUEST =
    new EntityBundleRequest();
  private int countTabContentChecked;

  static {
    ALL_PARTS_REQUEST.setIncludeEntity(true);
    ALL_PARTS_REQUEST.setIncludeEntityPath(true);
    ALL_PARTS_REQUEST.setIncludeAnnotations(true);
    ALL_PARTS_REQUEST.setIncludePermissions(true);
    ALL_PARTS_REQUEST.setIncludeEntityPath(true);
    ALL_PARTS_REQUEST.setIncludeHasChildren(true);
    ALL_PARTS_REQUEST.setIncludeFileHandles(true);
    ALL_PARTS_REQUEST.setIncludeRootWikiId(true);
    ALL_PARTS_REQUEST.setIncludeDOIAssociation(true);
    ALL_PARTS_REQUEST.setIncludeFileName(true);
    ALL_PARTS_REQUEST.setIncludeBenefactorACL(true);
    ALL_PARTS_REQUEST.setIncludeTableBundle(true);
    ALL_PARTS_REQUEST.setIncludeAccessControlList(true);
    ALL_PARTS_REQUEST.setIncludeBenefactorACL(true);
    ALL_PARTS_REQUEST.setIncludeThreadCount(true);
    ALL_PARTS_REQUEST.setIncludeRestrictionInformation(true);
  }

  @Inject
  public EntityPageTop(
    EntityPageTopView view,
    SynapseClientAsync synapseClient,
    Tabs tabs,
    ProjectTitleBar projectTitleBar,
    EntityMetadata projectMetadata,
    WikiTab wikiTab,
    FilesTab filesTab,
    DatasetsTab datasetsTab,
    TablesTab tablesTab,
    ChallengeTab challengeTab,
    DiscussionTab discussionTab,
    DockerTab dockerTab,
    EntityActionController projectActionController,
    EntityActionMenu projectActionMenu,
    CookieProvider cookies,
    SynapseJavascriptClient synapseJavascriptClient,
    GlobalApplicationState globalAppState,
    EntityId2BundleCache entityId2BundleCache,
    EventBus eventBus
  ) {
    this.view = view;
    this.synapseClient = synapseClient;
    fixServiceEntryPoint(synapseClient);
    this.tabs = tabs;
    this.wikiTab = wikiTab;
    this.filesTab = filesTab;
    this.datasetsTab = datasetsTab;
    this.tablesTab = tablesTab;
    this.challengeTab = challengeTab;
    this.discussionTab = discussionTab;
    this.dockerTab = dockerTab;
    this.projectTitleBar = projectTitleBar;
    this.projectMetadata = projectMetadata;
    this.projectActionController = projectActionController;
    this.projectActionMenu = projectActionMenu;
    this.cookies = cookies;
    this.synapseJavascriptClient = synapseJavascriptClient;
    this.placeChanger = globalAppState.getPlaceChanger();
    this.entityId2BundleCache = entityId2BundleCache;
    this.eventBus = eventBus;

    initTabs();
    view.setTabs(tabs.asWidget());
    view.setProjectMetadata(projectMetadata.asWidget());
    view.setProjectTitleBar(projectTitleBar.asWidget());
    projectActionMenu.addControllerWidget(projectActionController.asWidget());
    view.setProjectActionMenu(projectActionMenu.asWidget());

    view.getEventBinder().bindEventHandlers(this, eventBus);
  }

  /**
   * Event fired by ButtonLinkWidget.
   *
   * @param event
   */
  @EventHandler
  public void onChangeSynapsePlace(ChangeSynapsePlaceEvent event) {
    Synapse place = event.getPlace();
    if (entity.getId().equals(place.getEntityId())) {
      area = place.getArea();
      setCurrentAreaToken(place.getAreaToken());
      pushTabUrlToBrowserHistory = true;
      reconfigureCurrentArea();
    } else {
      // SWC-4234: if the project has not changed, then don't change places.
      EntityBundle bundle1 = entityId2BundleCache.get(entity.getId());
      EntityBundle bundle2 = entityId2BundleCache.get(place.getEntityId());
      if (bundle1 != null && bundle2 != null) {
        EntityHeader projectHeader1 = DisplayUtils.getProjectHeader(
          bundle1.getPath()
        );
        EntityHeader projectHeader2 = DisplayUtils.getProjectHeader(
          bundle2.getPath()
        );
        if (projectHeader1 != null && projectHeader1.equals(projectHeader2)) {
          // skip full reload - use the same logic as clicking on an entity in the same tab (in the same
          // project)
          EntityArea newArea = getAreaForEntity(bundle2.getEntity());
          getEntitySelectedCallback(newArea).invoke(place.getEntityId());
          return;
        }
      }

      placeChanger.goTo(place);
    }
  }

  public CallbackP<String> getEntitySelectedCallback(final EntityArea newArea) {
    return newEntityId -> {
      area = newArea;
      // always the current version from tab entity click
      Long version = null;
      // SWC-4023: clear out the initial area tokens when a new entity is selected
      clearAreaTokens();
      // SWC-3919: on tab entity click, push tab url to browser history
      pushTabUrlToBrowserHistory = true;
      configureEntity(newEntityId, version);
    };
  }

  private void clearAreaTokens() {
    tablesAreaToken = null;
    datasetsAreaToken = null;
    dockerAreaToken = null;
    discussionAreaToken = null;
    wikiAreaToken = null;
  }

  private void initTabs() {
    tabs.addTab(wikiTab.asTab());
    tabs.addTab(filesTab.asTab());
    tabs.addTab(datasetsTab.asTab());
    tabs.addTab(tablesTab.asTab());
    tabs.addTab(challengeTab.asTab());
    tabs.addTab(discussionTab.asTab());
    tabs.addTab(dockerTab.asTab());

    filesTab.setEntitySelectedCallback(
      getEntitySelectedCallback(EntityArea.FILES)
    );
    tablesTab.setEntitySelectedCallback(
      getEntitySelectedCallback(EntityArea.TABLES)
    );
    datasetsTab.setEntitySelectedCallback(
      getEntitySelectedCallback(EntityArea.DATASETS)
    );
    dockerTab.setEntitySelectedCallback(
      getEntitySelectedCallback(EntityArea.DOCKER)
    );

    // lazy init tabs, and show project information (if set)
    wikiTab.setTabClickedCallback(tab -> {
      area = EntityArea.WIKI;
      configureWikiTab();
    });
    challengeTab.setTabClickedCallback(tab -> {
      area = EntityArea.CHALLENGE;
      configureChallengeTab();
    });

    discussionTab.setTabClickedCallback(tab -> {
      // SWC-4078: if already on tab, reset to top level thread list.
      if (EntityArea.DISCUSSION.equals(area)) {
        discussionAreaToken = null;
        discussionTab.asTab().setContentStale(true);
      }

      area = EntityArea.DISCUSSION;
      configureDiscussionTab();
    });
    filesTab.setTabClickedCallback(tab -> {
      // SWC-4078: if already on tab, reset to project level.
      if (EntityArea.FILES.equals(area)) {
        filesEntityBundle = projectBundle;
        filesTab.asTab().setContentStale(true);
      }
      area = EntityArea.FILES;
      configureFilesTab();
    });

    datasetsTab.setTabClickedCallback(tab -> {
      // SWC-4078: if already on tab, reset to project level.
      if (EntityArea.DATASETS.equals(area)) {
        datasetsEntityBundle = projectBundle;
        datasetsTab.asTab().setContentStale(true);
      }

      area = EntityArea.DATASETS;
      configureDatasetsTab();
    });

    tablesTab.setTabClickedCallback(tab -> {
      // SWC-4078: if already on tab, reset to project level.
      if (EntityArea.TABLES.equals(area)) {
        tablesEntityBundle = projectBundle;
        tablesTab.asTab().setContentStale(true);
      }

      area = EntityArea.TABLES;
      configureTablesTab();
    });
    dockerTab.setTabClickedCallback(tab -> {
      // SWC-4078: if already on tab, reset to project level.
      if (EntityArea.DOCKER.equals(area)) {
        dockerEntityBundle = projectBundle;
        dockerTab.asTab().setContentStale(true);
      }

      area = EntityArea.DOCKER;
      configureDockerTab();
    });
  }

  /**
   * Update the bundle attached to this EntityPageTop.
   */
  public void configure(
    EntityBundle targetEntityBundle,
    Long versionNumber,
    EntityHeader projectHeader,
    Synapse.EntityArea initArea,
    String areaToken
  ) {
    this.currentTargetEntityBundle = targetEntityBundle;
    pushTabUrlToBrowserHistory = false;
    this.projectHeader = projectHeader;
    this.area = initArea;
    this.initialAreaToken = areaToken;
    wikiAreaToken = null;
    datasetsAreaToken = null;
    tablesAreaToken = null;
    discussionAreaToken = null;
    dockerAreaToken = null;
    this.entity = targetEntityBundle.getEntity();
    setTargetVersion(versionNumber);

    // note: the files/tables/wiki/discussion/docker tabs rely on the project bundle, so they are
    // configured later
    configureProject();
  }

  private void setTargetVersion(Long versionNumber) {
    if (EntityActionControllerImpl.isVersionSupported(entity)) {
      this.currentTargetVersionNumber = versionNumber;
    } else {
      this.currentTargetVersionNumber = null;
    }
  }

  public void initDefaultTabPlaces() {
    // initialize each tab place
    if (projectHeader != null) {
      String projectName = projectHeader.getName();
      String projectId = projectHeader.getId();
      Long versionNumber = null;

      wikiTab
        .asTab()
        .setEntityNameAndPlace(
          projectName,
          new Synapse(projectId, versionNumber, EntityArea.WIKI, wikiAreaToken)
        );
      filesTab
        .asTab()
        .setEntityNameAndPlace(
          projectName,
          new Synapse(projectId, versionNumber, EntityArea.FILES, null)
        );
      datasetsTab
        .asTab()
        .setEntityNameAndPlace(
          projectName,
          new Synapse(
            projectId,
            versionNumber,
            EntityArea.DATASETS,
            datasetsAreaToken
          )
        );
      tablesTab
        .asTab()
        .setEntityNameAndPlace(
          projectName,
          new Synapse(
            projectId,
            versionNumber,
            EntityArea.TABLES,
            tablesAreaToken
          )
        );
      challengeTab
        .asTab()
        .setEntityNameAndPlace(
          projectName,
          new Synapse(projectId, versionNumber, EntityArea.CHALLENGE, null)
        );
      discussionTab
        .asTab()
        .setEntityNameAndPlace(
          projectName,
          new Synapse(
            projectId,
            versionNumber,
            EntityArea.DISCUSSION,
            discussionAreaToken
          )
        );
      dockerTab
        .asTab()
        .setEntityNameAndPlace(
          projectName,
          new Synapse(
            projectId,
            versionNumber,
            EntityArea.DOCKER,
            dockerAreaToken
          )
        );
    }
  }

  public void configureProject() {
    view.setProjectLoadingVisible(true);
    hideTabs();
    projectBundle = null;
    projectBundleLoadError = null;
    AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
      @Override
      public void onSuccess(EntityBundle bundle) {
        view.setProjectLoadingVisible(false);
        // by default, all tab entity bundles point to the project entity bundle
        projectBundle =
          filesEntityBundle =
            tablesEntityBundle =
              datasetsEntityBundle = dockerEntityBundle = bundle;
        projectTitleBar.configure(projectBundle);
        projectMetadata.configure(projectBundle, null, projectActionMenu);
        view.setProjectUIVisible(true);
        initAreaToken();
        showSelectedTabs();
        updateEntityBundle(
          currentTargetEntityBundle,
          currentTargetVersionNumber
        );
      }

      @Override
      public void onFailure(Throwable caught) {
        projectTitleBar.clearState();
        view.setProjectLoadingVisible(false);
        view.setProjectUIVisible(false);
        projectBundleLoadError = caught;
        updateEntityBundle(
          currentTargetEntityBundle,
          currentTargetVersionNumber
        );
        tabs.setNavTabsVisible(false);
      }
    };
    if (
      projectHeader
        .getId()
        .equals(currentTargetEntityBundle.getEntity().getId())
    ) {
      callback.onSuccess(currentTargetEntityBundle);
    } else {
      synapseJavascriptClient.getEntityBundleFromCache(
        projectHeader.getId(),
        callback
      );
    }
  }

  public void configureEntity(String entityId, final Long version) {
    AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
      @Override
      public void onSuccess(EntityBundle bundle) {
        updateEntityBundle(bundle, version);
      }

      @Override
      public void onFailure(Throwable caught) {
        view.showErrorMessage(caught.getMessage());
      }
    };
    if (
      entityId != null &&
      projectBundle != null &&
      entityId.equals(projectBundle.getEntity().getId())
    ) {
      callback.onSuccess(projectBundle);
    } else {
      if (version == null) {
        synapseJavascriptClient.getEntityBundleFromCache(entityId, callback);
      } else {
        synapseJavascriptClient.getEntityBundleForVersion(
          entityId,
          version,
          ALL_PARTS_REQUEST,
          callback
        );
      }
    }
  }

  private List<EntityArea> getVisibleTabs() {
    List<EntityArea> visibleTabs = new ArrayList<EntityArea>();
    if (wikiTab.asTab().isTabListItemVisible()) {
      visibleTabs.add(EntityArea.WIKI);
    }
    if (filesTab.asTab().isTabListItemVisible()) {
      visibleTabs.add(EntityArea.FILES);
    }
    if (datasetsTab.asTab().isTabListItemVisible()) {
      visibleTabs.add(EntityArea.DATASETS);
    }
    if (tablesTab.asTab().isTabListItemVisible()) {
      visibleTabs.add(EntityArea.TABLES);
    }
    if (challengeTab.asTab().isTabListItemVisible()) {
      visibleTabs.add(EntityArea.CHALLENGE);
    }
    if (dockerTab.asTab().isTabListItemVisible()) {
      visibleTabs.add(EntityArea.DOCKER);
    }
    visibleTabs.add(EntityArea.DISCUSSION);
    return visibleTabs;
  }

  public void checkForTabVisibility() {
    // SWC-5808: Guard against the case where the current area is hidden.  May be hidden because it has no content, and the user does not have permission to create content.
    List<EntityArea> visibleTabs = getVisibleTabs();
    if (!visibleTabs.contains(area)) {
      area = visibleTabs.get(0);
      setCurrentAreaToken(null);
      pushTabUrlToBrowserHistory = true;
      reconfigureCurrentArea();
    }
  }

  public void reconfigureCurrentArea() {
    switch (area) {
      case FILES:
        configureFilesTab();
        tabs.showTab(filesTab.asTab(), pushTabUrlToBrowserHistory);
        break;
      case WIKI:
        configureWikiTab();
        tabs.showTab(wikiTab.asTab(), pushTabUrlToBrowserHistory);
        break;
      case DATASETS:
        configureDatasetsTab();
        tabs.showTab(datasetsTab.asTab(), pushTabUrlToBrowserHistory);
        break;
      case TABLES:
        configureTablesTab();
        tabs.showTab(tablesTab.asTab(), pushTabUrlToBrowserHistory);
        break;
      case CHALLENGE:
        configureChallengeTab();
        tabs.showTab(challengeTab.asTab(), pushTabUrlToBrowserHistory);
        break;
      case DISCUSSION:
        configureDiscussionTab();
        tabs.showTab(discussionTab.asTab(), pushTabUrlToBrowserHistory);
        break;
      case DOCKER:
        configureDockerTab();
        tabs.showTab(dockerTab.asTab(), pushTabUrlToBrowserHistory);
        break;
      default:
    }
    pushTabUrlToBrowserHistory = false;
    // when tab reconfigured, scroll to the top
    view.scrollToTop();
  }

  public void updateEntityBundle(EntityBundle bundle, Long version) {
    this.currentTargetEntityBundle = bundle;
    entity = bundle.getEntity();
    setTargetVersion(version);
    // Redirect if Entity is a Link
    if (entity instanceof Link) {
      Reference ref = ((Link) bundle.getEntity()).getLinksTo();
      if (ref != null) {
        placeChanger.goTo(
          new Synapse(
            ref.getTargetId(),
            ref.getTargetVersionNumber(),
            null,
            null
          )
        );
      } else {
        // show error and then allow entity bundle to go to view
        view.showErrorMessage(DisplayConstants.ERROR_NO_LINK_DEFINED);
      }
    } else if (entity instanceof Project) {
      switch (area) {
        case FILES:
          fileChanged(bundle, currentTargetVersionNumber);
          break;
        case DATASETS:
          datasetChanged(bundle, currentTargetVersionNumber);
          break;
        case TABLES:
          tableChanged(bundle, currentTargetVersionNumber);
          break;
        case DOCKER:
          dockerChanged(bundle);
          break;
        default:
      }
    } else {
      if (entity instanceof FileEntity || entity instanceof Folder) {
        fileChanged(bundle, currentTargetVersionNumber);
      } else if (entity instanceof EntityRefCollectionView) {
        datasetChanged(bundle, currentTargetVersionNumber);
      } else if (entity instanceof Table) {
        tableChanged(bundle, currentTargetVersionNumber);
      } else if (entity instanceof DockerRepository) {
        dockerChanged(bundle);
      }
    }
    reconfigureCurrentArea();
  }

  private void dockerChanged(EntityBundle bundle) {
    dockerEntityBundle = bundle;
    area = EntityArea.DOCKER;
    dockerTab.asTab().setContentStale(true);
  }

  private void datasetChanged(EntityBundle bundle, Long version) {
    datasetsEntityBundle = bundle;
    datasetsVersionNumber = version;
    area = EntityArea.DATASETS;
    datasetsTab.asTab().setContentStale(true);
  }

  private void tableChanged(EntityBundle bundle, Long version) {
    tablesEntityBundle = bundle;
    tablesVersionNumber = version;
    area = EntityArea.TABLES;
    tablesTab.asTab().setContentStale(true);
  }

  private void fileChanged(EntityBundle bundle, Long version) {
    filesEntityBundle = bundle;
    filesVersionNumber = version;
    area = EntityArea.FILES;
    filesTab.asTab().setContentStale(true);
  }

  public void showSelectedTabs() {
    visibleTabCount = 0;
    countTabContentChecked = 0;
    // SWC-3137: show all tabs, until project display settings state persists. Challenge is still
    // dependent on content.
    // always show the discussion tab
    getTabVisibilityCallback(EntityArea.DISCUSSION, discussionTab.asTab())
      .onSuccess(true);
    if (
      projectBundle == null ||
      projectBundle.getPermissions() == null ||
      projectBundle.getPermissions().getCanEdit()
    ) {
      // if user can edit, then show other tabs
      getTabVisibilityCallback(EntityArea.WIKI, wikiTab.asTab())
        .onSuccess(true);
      getTabVisibilityCallback(EntityArea.FILES, filesTab.asTab())
        .onSuccess(true);
      getTabVisibilityCallback(EntityArea.DATASETS, datasetsTab.asTab())
        .onSuccess(true);
      getTabVisibilityCallback(EntityArea.TABLES, tablesTab.asTab())
        .onSuccess(true);
      getTabVisibilityCallback(EntityArea.DOCKER, dockerTab.asTab())
        .onSuccess(true);
    } else {
      // otherwise only show the tabs only if content is present.
      synapseJavascriptClient.isWiki(
        projectHeader.getId(),
        getTabVisibilityCallback(EntityArea.WIKI, wikiTab.asTab())
      );
      synapseJavascriptClient.isFileOrFolder(
        projectHeader.getId(),
        getTabVisibilityCallback(EntityArea.FILES, filesTab.asTab())
      );
      synapseJavascriptClient.isEntityRefCollectionView(
        projectHeader.getId(),
        getTabVisibilityCallback(EntityArea.DATASETS, datasetsTab.asTab())
      );
      synapseJavascriptClient.isTable(
        projectHeader.getId(),
        getTabVisibilityCallback(EntityArea.TABLES, tablesTab.asTab())
      );
      synapseJavascriptClient.isDocker(
        projectHeader.getId(),
        getTabVisibilityCallback(EntityArea.DOCKER, dockerTab.asTab())
      );
    }
    synapseClient.isChallenge(
      projectHeader.getId(),
      getTabVisibilityCallback(EntityArea.CHALLENGE, challengeTab.asTab())
    );
  }

  public AsyncCallback<Boolean> getTabVisibilityCallback(
    final EntityArea entityArea,
    final Tab tab
  ) {
    return new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        view.showErrorMessage(caught.getMessage());
        tabContentChecked();
      }

      @Override
      public void onSuccess(Boolean isContent) {
        if (isContent) {
          visibleTabCount++;
        }
        if (visibleTabCount > 1) {
          tabs.setNavTabsVisible(true);
        }

        tab.setTabListItemVisible(isContent);
        tabContentChecked();
      }

      private void tabContentChecked() {
        countTabContentChecked++;
        if (countTabContentChecked >= EntityArea.values().length) {
          checkForTabVisibility();
        }
      }
    };
  }

  public void hideTabs() {
    tabs.setNavTabsVisible(false);
    wikiTab.asTab().setTabListItemVisible(false);
    filesTab.asTab().setTabListItemVisible(false);
    filesTab.resetView();
    datasetsTab.asTab().setTabListItemVisible(false);
    datasetsTab.resetView();
    tablesTab.asTab().setTabListItemVisible(false);
    tablesTab.resetView();
    challengeTab.asTab().setTabListItemVisible(false);
    discussionTab.asTab().setTabListItemVisible(false);
    dockerTab.asTab().setTabListItemVisible(false);
  }

  /**
   * Based on tab visibility, pick the area that should be displayed when no area is given.
   *
   * @return
   */
  public EntityArea getDefaultProjectArea() {
    if (wikiTab.asTab().isTabListItemVisible()) {
      return EntityArea.WIKI;
    }
    if (filesTab.asTab().isTabListItemVisible()) {
      return EntityArea.FILES;
    }
    if (datasetsTab.asTab().isTabListItemVisible()) {
      return EntityArea.DATASETS;
    }
    if (tablesTab.asTab().isTabListItemVisible()) {
      return EntityArea.TABLES;
    }
    if (discussionTab.asTab().isTabListItemVisible()) {
      return EntityArea.DISCUSSION;
    }
    if (challengeTab.asTab().isTabListItemVisible()) {
      return EntityArea.CHALLENGE;
    }
    if (dockerTab.asTab().isTabListItemVisible()) {
      return EntityArea.DOCKER;
    }
    return EntityArea.WIKI;
  }

  private EntityArea getAreaForEntity(Entity entity) {
    EntityArea area = null;
    if (entity instanceof Project) {
      area = getDefaultProjectArea();
    } else if (entity instanceof EntityRefCollectionView) {
      area = EntityArea.DATASETS;
    } else if (entity instanceof Table) {
      area = EntityArea.TABLES;
    } else if (entity instanceof DockerRepository) {
      area = EntityArea.DOCKER;
    } else { // if (entity instanceof FileEntity || entity instanceof Folder, or any other entity type)
      area = EntityArea.FILES;
    }
    return area;
  }

  public void initAreaToken() {
    // set area, if undefined
    if (area == null) {
      area = getAreaForEntity(entity);
    }
    setCurrentAreaToken(initialAreaToken);

    if (projectBundle != null) {
      String wikiId = getWikiPageId(
        wikiAreaToken,
        projectBundle.getRootWikiId()
      );
      projectActionController.configure(
        projectActionMenu,
        projectBundle,
        true,
        wikiId,
        null,
        // The project action controller doesn't surface download functionality
        null
      );
    }

    initDefaultTabPlaces();

    // set all content stale
    filesTab.asTab().setContentStale(true);
    wikiTab.asTab().setContentStale(true);
    datasetsTab.asTab().setContentStale(true);
    tablesTab.asTab().setContentStale(true);
    challengeTab.asTab().setContentStale(true);
    discussionTab.asTab().setContentStale(true);
    dockerTab.asTab().setContentStale(true);
  }

  public void setCurrentAreaToken(String token) {
    // set area token
    switch (area) {
      case WIKI:
        wikiAreaToken = token;
        wikiTab.asTab().setContentStale(true);
        break;
      case DATASETS:
        datasetsAreaToken = token;
        datasetsTab.asTab().setContentStale(true);
        break;
      case TABLES:
        tablesAreaToken = token;
        tablesTab.asTab().setContentStale(true);
        break;
      case DISCUSSION:
        discussionAreaToken = token;
        discussionTab.asTab().setContentStale(true);
        break;
      case DOCKER:
        if (DisplayUtils.isInTestWebsite(cookies)) {
          dockerAreaToken = token;
          dockerTab.asTab().setContentStale(true);
        }
        break;
      default:
    }
  }

  public void clearState() {
    view.clear();
    wikiTab.clear();
    this.entity = null;
  }

  @Override
  public Widget asWidget() {
    if (entity != null) {
      return view.asWidget();
    }
    return null;
  }

  public void configureDatasetsTab() {
    if (datasetsTab.asTab().isContentStale()) {
      datasetsTab.setProject(
        projectHeader.getId(),
        projectBundle,
        projectBundleLoadError
      );
      datasetsTab.configure(
        datasetsEntityBundle,
        datasetsVersionNumber,
        datasetsAreaToken
      );
      datasetsTab.asTab().setContentStale(false);
    }
  }

  public void configureTablesTab() {
    if (tablesTab.asTab().isContentStale()) {
      tablesTab.setProject(
        projectHeader.getId(),
        projectBundle,
        projectBundleLoadError
      );
      tablesTab.configure(
        tablesEntityBundle,
        tablesVersionNumber,
        tablesAreaToken
      );
      tablesTab.asTab().setContentStale(false);
    }
  }

  public void configureFilesTab() {
    if (filesTab.asTab().isContentStale()) {
      filesTab.setProject(
        projectHeader.getId(),
        projectBundle,
        projectBundleLoadError
      );
      filesTab.configure(filesEntityBundle, filesVersionNumber);
      filesTab.asTab().setContentStale(false);
    }
  }

  public void fireEntityUpdatedEvent() {
    eventBus.fireEvent(new EntityUpdatedEvent());
  }

  public void configureWikiTab() {
    if (wikiTab.asTab().isContentStale()) {
      final boolean isWikiTabShown = area == EntityArea.WIKI;
      boolean canEdit = false;
      String wikiId = null;

      if (projectBundle != null) {
        canEdit = projectBundle.getPermissions().getCanCertifiedUserEdit();
        wikiId = getWikiPageId(wikiAreaToken, projectBundle.getRootWikiId());
      }

      final WikiPageWidget.Callback callback = new WikiPageWidget.Callback() {
        @Override
        public void pageUpdated() {
          fireEntityUpdatedEvent();
        }

        @Override
        public void noWikiFound() {
          if (
            isWikiTabShown &&
            projectBundle.getRootWikiId() != null &&
            !projectBundle.getRootWikiId().equals(wikiAreaToken)
          ) {
            // attempted to load a wiki, but it was not found. Show a message, and redirect to the root.
            view.showInfo(
              "Wiki not found (id=" +
              wikiAreaToken +
              "), loading root wiki page instead."
            );
            wikiTab.asTab().setContentStale(true);
            wikiAreaToken = projectBundle.getRootWikiId();
            configureWikiTab();
          }
        }
      };
      wikiTab.configure(
        projectHeader.getId(),
        projectHeader.getName(),
        projectBundle,
        wikiId,
        canEdit,
        callback
      );
      if (isWikiTabShown) {
        tabs.showTab(wikiTab.asTab(), false);
      }

      wikiTab.asTab().setContentStale(false);
    }

    // on configure of wiki tab, always update the entity action controller with the correct wiki page
    if (projectBundle != null) {
      entity = projectBundle.getEntity();
    }
  }

  public void configureChallengeTab() {
    if (challengeTab.asTab().isContentStale()) {
      String projectId = projectHeader.getId();
      challengeTab.configure(projectId, projectHeader.getName(), projectBundle);
      challengeTab.asTab().setContentStale(false);
    }
    challengeTab.updateActionMenuCommands();
  }

  public void configureDiscussionTab() {
    if (discussionTab.asTab().isContentStale()) {
      String projectId = projectHeader.getId();
      boolean canModerate = false;
      if (projectBundle != null) {
        canModerate = projectBundle.getPermissions().getCanModerate();
      }
      discussionTab.configure(
        projectId,
        projectHeader.getName(),
        projectBundle,
        discussionAreaToken,
        canModerate
      );
      discussionTab.asTab().setContentStale(false);
    }
    discussionTab.updateActionMenuCommands();
  }

  public void configureDockerTab() {
    if (dockerTab.asTab().isContentStale()) {
      dockerTab.setProject(
        projectHeader.getId(),
        projectBundle,
        projectBundleLoadError
      );
      dockerTab.configure(dockerEntityBundle, dockerAreaToken);
      dockerTab.asTab().setContentStale(false);
    }
  }

  public String getWikiPageId(String areaToken, String rootWikiId) {
    String wikiPageId = rootWikiId;
    if (DisplayUtils.isDefined(areaToken)) wikiPageId = areaToken;
    return wikiPageId;
  }

  // for testing
  public String getTablesAreaToken() {
    return tablesAreaToken;
  }

  public String getDatasetsAreaToken() {
    return datasetsAreaToken;
  }
}
