package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayConstants;
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
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

public class TablesTab implements TablesTabView.Presenter, QueryChangeHandler {

	public static final String TABLE_QUERY_PREFIX = "query/";

	Tab tab;
	TablesTabView view;
	TableListWidget tableListWidget;
	BasicTitleBar tableTitleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	boolean annotationsShown;
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
	public static final String TABLES_HELP = "Build structured queryable data that can be described by a schema using the Tables.";
	public static final String TABLES_HELP_URL = WebConstants.DOCS_URL + "tables.html";
	Long version;

	//TODO: add action menu to view!
	
	@Inject
	public TablesTab(Tab tab, PortalGinInjector ginInjector) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure(DisplayConstants.TABLES, TABLES_HELP, TABLES_HELP_URL, EntityArea.TABLES);
	}

	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getTablesTabView();
			this.tableListWidget = ginInjector.getTableListWidget();
			this.tableTitleBar = ginInjector.getBasicTitleBar();
			this.breadcrumb = ginInjector.getBreadcrumb();
			this.metadata = ginInjector.getEntityMetadata();
			this.queryTokenProvider = ginInjector.getQueryTokenProvider();
			this.synAlert = ginInjector.getStuAlert();
			this.modifiedCreatedBy = ginInjector.getModifiedCreatedByWidget();

			view.setBreadcrumb(breadcrumb.asWidget());
			view.setTableList(tableListWidget.asWidget());
			view.setTitlebar(tableTitleBar.asWidget());
			view.setEntityMetadata(metadata.asWidget());
			view.setSynapseAlert(synAlert.asWidget());
			view.setModifiedCreatedBy(modifiedCreatedBy);
			tab.setContent(view.asWidget());

			tableListWidget.setTableClickedCallback(new CallbackP<EntityHeader>() {
				@Override
				public void invoke(EntityHeader entityHeader) {
					areaToken = null;
					entitySelectedCallback.invoke(entityHeader.getId());
					// selected a table/view, show title info immediately
					tableTitleBar.configure(entityHeader);

					List<LinkData> links = new ArrayList<LinkData>();
					Place projectPlace = new Synapse(projectEntityId, null, EntityArea.TABLES, null);
					links.add(new LinkData(DisplayConstants.TABLES, EntityTypeUtils.getIconTypeForEntityClassName(TableEntity.class.getName()), projectPlace));
					breadcrumb.configure(links, entityHeader.getName());

					view.setBreadcrumbVisible(true);
					view.setTitlebarVisible(true);
				}
			});
			initBreadcrumbLinkClickedHandler();
			configMap = ProvenanceWidget.getDefaultWidgetDescriptor();
		}
	}

	public void setEntitySelectedCallback(CallbackP<String> entitySelectedCallback) {
		this.entitySelectedCallback = entitySelectedCallback;
	}

	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {
		ginInjector.getEventBus().fireEvent(event);
	}

	public void initBreadcrumbLinkClickedHandler() {
		CallbackP<Place> breadcrumbClicked = new CallbackP<Place>() {
			public void invoke(Place place) {
				// if this is the project id, then just reconfigure from the project bundle
				Synapse synapse = (Synapse) place;
				String entityId = synapse.getEntityId();
				entitySelectedCallback.invoke(entityId);
			};
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

	public void configure(EntityBundle entityBundle, Long versionNumber, String areaToken) {
		lazyInject();
		this.areaToken = areaToken;
		synAlert.clear();
		setTargetBundle(entityBundle, versionNumber);
	}

	public void showProjectLevelUI() {
		String title = projectEntityId;
		if (projectBundle != null) {
			title = projectBundle.getEntity().getName();
		} else {
			showError(projectBundleLoadError);
		}
		tab.setEntityNameAndPlace(title, new Synapse(projectEntityId, null, EntityArea.TABLES, null));
		tab.showTab(true);
	}

	public void resetView() {
		if (view != null) {
			synAlert.clear();
			view.setEntityMetadataVisible(false);
			view.setBreadcrumbVisible(false);
			view.setTableListVisible(false);
			view.setTitlebarVisible(false);
			view.clearActionMenuContainer();
			view.clearTableEntityWidget();
			modifiedCreatedBy.setVisible(false);
			view.setTableUIVisible(false);
		}
	}

	public void showError(Throwable error) {
		resetView();
		synAlert.handleException(error);
	}

	public void setTargetBundle(EntityBundle bundle, Long versionNumber) {
		this.entityBundle = bundle;
		Entity entity = bundle.getEntity();
		boolean isTable = entity instanceof Table;
		boolean isProject = entity instanceof Project;
		boolean isVersionSupported = EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity(), ginInjector.getCookieProvider());
		version = isVersionSupported ? versionNumber : null;
		view.setEntityMetadataVisible(isTable);
		view.setBreadcrumbVisible(isTable);
		view.setTableListVisible(isProject);
		view.setTitlebarVisible(isTable);
		view.clearActionMenuContainer();
		view.clearTableEntityWidget();
		modifiedCreatedBy.setVisible(false);
		view.setTableUIVisible(isTable);
		if (isTable) {
			updateVersionAndAreaToken(entity.getId(), version, areaToken);
			breadcrumb.configure(bundle.getPath(), EntityArea.TABLES);
			tableTitleBar.configure(bundle);
			modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
			v2TableWidget = ginInjector.createNewTableEntityWidget();
			view.setTableEntityWidget(v2TableWidget.asWidget());
			v2TableWidget.configure(bundle, version, bundle.getPermissions().getCanCertifiedUserEdit(), this, tab.getEntityActionMenu());
			boolean isCurrentVersion = version == null;
			tab.configureEntityActionController(bundle, isCurrentVersion, null);
		} else if (isProject) {
			areaToken = null;
			tableListWidget.configure(bundle);
			showProjectLevelUI();
		}
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

	private void updateVersionAndAreaToken(String entityId, Long versionNumber, String areaToken) {
		boolean isVersionSupported = EntityActionControllerImpl.isVersionSupported(entityBundle.getEntity(), ginInjector.getCookieProvider());
		Long newVersion = isVersionSupported ? versionNumber : null;
		Synapse newPlace = new Synapse(entityId, newVersion, EntityArea.TABLES, areaToken);
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

	public Query getQueryString() {
		if (areaToken != null && areaToken.startsWith(TABLE_QUERY_PREFIX)) {
			String token = areaToken.substring(TABLE_QUERY_PREFIX.length(), areaToken.length());
			if (token != null) {
				return queryTokenProvider.tokenToQuery(token);
			}
		}
		return null;
	}
}
