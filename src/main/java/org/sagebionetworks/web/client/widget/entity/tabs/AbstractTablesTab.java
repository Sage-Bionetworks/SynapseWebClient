package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

/**
 * Encapsulates shared logic between tabs that show tables.
 * Currently, we show Datasets and Tables/EntityViews/SubmissionViews in two different tabs.
 */
public abstract class AbstractTablesTab implements TablesTabView.Presenter, QueryChangeHandler {

	public static final String TABLE_QUERY_PREFIX = "query/";

	Tab tab;
	TablesTabView view;
	TableListWidget tableListWidget;
	BasicTitleBar titleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	QueryTokenProvider queryTokenProvider;
	EntityBundle projectBundle;
	EntityBundle entityBundle;
	Throwable projectBundleLoadError;
	String projectEntityId;
	String areaToken;
	StuAlert synAlert;
	PortalGinInjector ginInjector;
	ModifiedCreatedByWidget modifiedCreatedBy;
	TableEntityWidget v2TableWidget;
	Map<String, String> configMap;
	CallbackP<String> entitySelectedCallback;
	Long version;
	WikiPageWidget wikiPageWidget;

	protected abstract EntityArea getTabArea();

	protected abstract String getTabDisplayName();

	protected abstract List<EntityType> getTypesShownInList();

	protected abstract boolean isEntityShownInTab(Entity entity);

	@Inject
	public AbstractTablesTab(Tab tab, PortalGinInjector ginInjector) {
		this.tab = tab;
		this.ginInjector = ginInjector;
	}

	public void configure(EntityBundle entityBundle, Long versionNumber, String areaToken) {
		lazyInject();
		this.areaToken = areaToken;
		synAlert.clear();
		setTargetBundle(entityBundle, versionNumber);
	}


	protected CallbackP<EntityHeader> getTableListWidgetClickedCallback() {
		return entityHeader -> {
			areaToken = null;
			entitySelectedCallback.invoke(entityHeader.getId());
			// selected a table/view, show title info immediately
			titleBar.configure(entityHeader);

			List<LinkData> links = new ArrayList<>();
			Place projectPlace = new Synapse(projectEntityId, null, getTabArea(), null);
			links.add(new LinkData(getTabDisplayName(), EntityTypeUtils.getEntityType(entityHeader), projectPlace));
			breadcrumb.configure(links, entityHeader.getName());

			view.setTitle(getTabDisplayName());
			view.setBreadcrumbVisible(true);
			view.setTitlebarVisible(true);
		};
	}

	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getTablesTabView();
			this.tableListWidget = ginInjector.getTableListWidget();
			this.titleBar = ginInjector.getBasicTitleBar();
			this.breadcrumb = ginInjector.getBreadcrumb();
			this.metadata = ginInjector.getEntityMetadata();
			this.queryTokenProvider = ginInjector.getQueryTokenProvider();
			this.synAlert = ginInjector.getStuAlert();
			this.modifiedCreatedBy = ginInjector.getModifiedCreatedByWidget();
			this.wikiPageWidget = ginInjector.getWikiPageWidget();

			view.setTitle(getTabDisplayName());
			view.setBreadcrumb(breadcrumb.asWidget());
			view.setTableList(tableListWidget.asWidget());
			view.setTitlebar(titleBar.asWidget());
			view.setEntityMetadata(metadata.asWidget());
			view.setSynapseAlert(synAlert.asWidget());
			view.setModifiedCreatedBy(modifiedCreatedBy);
			view.setWikiPage(wikiPageWidget.asWidget());
			tab.setContent(view.asWidget());
			tableListWidget.setTableClickedCallback(getTableListWidgetClickedCallback());
			initBreadcrumbLinkClickedHandler();
			configMap = ProvenanceWidget.getDefaultWidgetDescriptor();
		}
	}

	public void setEntitySelectedCallback(CallbackP<String> entitySelectedCallback) {
		this.entitySelectedCallback = entitySelectedCallback;
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

	public void setProject(String projectEntityId, EntityBundle projectBundle, Throwable projectBundleLoadError) {
		this.projectEntityId = projectEntityId;
		this.projectBundle = projectBundle;
		this.projectBundleLoadError = projectBundleLoadError;
	}

	public void resetView() {
		if (view != null) {
			synAlert.clear();
			view.setEntityMetadataVisible(false);
			view.setBreadcrumbVisible(false);
			view.setTableListVisible(false);
			view.setTitlebarVisible(false);
			view.setWikiPageVisible(false);
			view.clearTableEntityWidget();
			modifiedCreatedBy.setVisible(false);
			view.setTableUIVisible(false);
		}
	}

	public void showError(Throwable error) {
		resetView();
		synAlert.handleException(error);
	}

	public Tab asTab() {
		return tab;
	}

	public void onQueryChange(Query newQuery) {
		if (newQuery != null && tab.isTabPaneVisible()) {
			String token = queryTokenProvider.queryToToken(newQuery);
			Long versionNumber = QueryBundleUtils.getTableVersion(newQuery.getSql());
			String synId = QueryBundleUtils.getTableIdFromSql(newQuery.getSql());
			if (token != null && !newQuery.equals(v2TableWidget.getDefaultQuery())) {
				areaToken = TABLE_QUERY_PREFIX + token;
			} else {
				areaToken = "";
			}
			updateVersionAndAreaToken(synId, versionNumber, areaToken);
			tab.showTab(true);
		}
	}

	public Query getQueryString() {
		if (areaToken != null && areaToken.startsWith(TABLE_QUERY_PREFIX)) {
			String token = areaToken.substring(TABLE_QUERY_PREFIX.length());
			return queryTokenProvider.tokenToQuery(token);
		}
		return null;
	}

	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {
		ginInjector.getEventBus().fireEvent(event);
	}


	protected void updateVersionAndAreaToken(String entityId, Long versionNumber, String areaToken) {
		boolean isVersionSupported = EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity(), ginInjector.getCookieProvider());
		Long newVersion = isVersionSupported ? versionNumber : null;
		Synapse newPlace = new Synapse(entityId, newVersion, getTabArea(), areaToken);
		// SWC-4942: if versions are supported, and the version has changed (the version in the query does
		// not match the entity bundle, for example),
		// then reload the entity bundle (to reconfigure the tools menu and other widgets on the page) by
		// doing a place change to the correct version of the bundle.
		if ((isVersionSupported && !Objects.equals(newVersion, version)) || !entityId.equals(entityBundle.getEntity().getId())) {
			ginInjector.getGlobalApplicationState().getPlaceChanger().goTo(newPlace);
			return;
		}
		metadata.configure(entityBundle, newVersion, tab.getEntityActionMenu());
		tab.setEntityNameAndPlace(entityBundle.getEntity().getName(), newPlace);
		configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(FilesTab.WIDGET_HEIGHT_PX - 84));
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(entityId, newVersion));
		ProvenanceWidget provWidget = ginInjector.getProvenanceRenderer();
		view.setProvenance(provWidget);
		provWidget.configure(configMap);
		version = newVersion;
	}

	public void showProjectLevelUI() {
		String title = projectEntityId;
		if (projectBundle != null) {
			title = projectBundle.getEntity().getName();
		} else {
			showError(projectBundleLoadError);
		}
		tab.setEntityNameAndPlace(title, new Synapse(projectEntityId, null, getTabArea(), null));
		tab.showTab(true);
	}

	public void setTargetBundle(EntityBundle bundle, Long versionNumber) {
		this.entityBundle = bundle;
		Entity entity = bundle.getEntity();
		boolean isShownInTab = isEntityShownInTab(entity);
		boolean isDataset = entity instanceof Dataset;
		boolean isProject = entity instanceof Project;
		boolean isVersionSupported = EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity(), ginInjector.getCookieProvider());
		version = isVersionSupported ? versionNumber : null;
		view.setTitle(getTabDisplayName());
		view.setEntityMetadataVisible(isShownInTab);
		view.setBreadcrumbVisible(isShownInTab);
		view.setTableListVisible(isProject);
		view.setTitlebarVisible(isShownInTab);
		view.clearTableEntityWidget();
		modifiedCreatedBy.setVisible(false);
		view.setTableUIVisible(isShownInTab);
		view.setActionMenu(tab.getEntityActionMenu());
		tab.getEntityActionMenu().setTableDownloadOptionsVisible(isShownInTab);
		boolean isCurrentVersion = version == null;

		if (isDataset && isCurrentVersion) {
			// SWC-5878 - On the current (non-snapshot) version of a dataset, only editors should be able to download
			tab.getEntityActionMenu().setTableDownloadOptionsEnabled(bundle.getPermissions().getCanCertifiedUserEdit());
		} else {
			tab.getEntityActionMenu().setTableDownloadOptionsEnabled(true);
		}

		tab.configureEntityActionController(bundle, isCurrentVersion, null);
		if (isShownInTab) {
			final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();

			updateVersionAndAreaToken(entity.getId(), version, areaToken);
			breadcrumb.configure(bundle.getPath(), getTabArea());
			titleBar.configure(bundle);
			modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
			v2TableWidget = ginInjector.createNewTableEntityWidget();
			view.setTableEntityWidget(v2TableWidget.asWidget());
			v2TableWidget.configure(bundle, version, canEdit, this, tab.getEntityActionMenu());

			// Configure wiki
			view.setWikiPageVisible(true);
			final WikiPageWidget.Callback wikiCallback = new WikiPageWidget.Callback() {
				@Override
				public void pageUpdated() {
					ginInjector.getEventBus().fireEvent(new EntityUpdatedEvent());
				}

				@Override
				public void noWikiFound() {
					view.setWikiPageVisible(false);
				}
			};
			wikiPageWidget.configure(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), bundle.getRootWikiId(), versionNumber), canEdit, wikiCallback);
			CallbackP<String> wikiReloadHandler = new CallbackP<String>() {
				@Override
				public void invoke(String wikiPageId) {
					wikiPageWidget.configure(new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(), wikiPageId, versionNumber), canEdit, wikiCallback);
				}
			};
			wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);

		} else if (isProject) {
			areaToken = null;
			tableListWidget.configure(bundle, getTypesShownInList());
			view.setWikiPageVisible(false);
			showProjectLevelUI();
		}
	}


}
