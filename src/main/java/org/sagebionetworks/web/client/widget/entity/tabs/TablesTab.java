package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
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
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

public class TablesTab implements TablesTabView.Presenter, QueryChangeHandler{
	
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
	Map<String,String> configMap;
	ActionMenuWidget entityActionMenu;
	CallbackP<String> entitySelectedCallback;
	public static final String TABLES_HELP = "Build structured queryable data that can be described by a schema using the Tables.";
	public static final String TABLES_HELP_URL = WebConstants.DOCS_URL + "tables.html";
	
	@Inject
	public TablesTab(Tab tab,
			PortalGinInjector ginInjector
			) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure(DisplayConstants.TABLES, TABLES_HELP, TABLES_HELP_URL);
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
	
	public void setProject(String projectEntityId, EntityBundle projectBundle, Throwable projectBundleLoadError) {
		this.projectEntityId = projectEntityId;
		this.projectBundle = projectBundle;
		this.projectBundleLoadError = projectBundleLoadError;
	}
	
	public void configure(EntityBundle entityBundle, String areaToken, ActionMenuWidget entityActionMenu) {
		lazyInject();
		this.areaToken = areaToken;
		this.entityActionMenu = entityActionMenu;
		synAlert.clear();
		setTargetBundle(entityBundle);
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
			view.setProvenanceVisible(false);
		}
	}
	
	public void showError(Throwable error) {
		resetView();
		synAlert.handleException(error);
	}
	
	public void setTargetBundle(EntityBundle bundle) {
		this.entityBundle = bundle;
		tab.setEntityNameAndPlace(bundle.getEntity().getName(), new Synapse(bundle.getEntity().getId(), null, EntityArea.TABLES, null));
		Entity entity = bundle.getEntity();
		boolean isTable = entity instanceof Table;
		boolean isProject = entity instanceof Project;
		view.setEntityMetadataVisible(isTable);
		view.setBreadcrumbVisible(isTable);
		view.setTableListVisible(isProject);
		view.setTitlebarVisible(isTable);
		view.clearActionMenuContainer();
		view.clearTableEntityWidget();
		modifiedCreatedBy.setVisible(false);
		view.setProvenanceVisible(isTable);
		
		if (isTable) {
			breadcrumb.configure(bundle.getPath(), EntityArea.TABLES);
			metadata.configure(bundle, null, entityActionMenu);
			tableTitleBar.configure(bundle);
			modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
			v2TableWidget = ginInjector.createNewTableEntityWidget();
			view.setTableEntityWidget(v2TableWidget.asWidget());
			v2TableWidget.configure(bundle, bundle.getPermissions().getCanCertifiedUserEdit(), this, entityActionMenu);
			ProvenanceWidget provWidget = ginInjector.getProvenanceRenderer();
			configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(FilesTab.WIDGET_HEIGHT_PX-84));
			configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(entity.getId(), null));
			view.setProvenance(provWidget);
			provWidget.configure(configMap);
		} else if (isProject) {
			areaToken = null;
			tableListWidget.configure(bundle);
			showProjectLevelUI();
		}
	}
	
	public Tab asTab(){
		return tab;
	}
	
	public void onQueryChange(Query newQuery) {
		if(newQuery != null && tab.isTabPaneVisible()) {
			String token = queryTokenProvider.queryToToken(newQuery);
			if(token != null && !newQuery.equals(v2TableWidget.getDefaultQuery())){
				areaToken = TABLE_QUERY_PREFIX + token;
			} else {
				areaToken = "";
			}
			tab.setEntityNameAndPlace(entityBundle.getEntity().getName(), new Synapse(entityBundle.getEntity().getId(), null, EntityArea.TABLES, areaToken));
			tab.showTab(true);
		}
	}
	
	public Query getQueryString() {
		if(areaToken != null && areaToken.startsWith(TABLE_QUERY_PREFIX)) {
			String token = areaToken.substring(TABLE_QUERY_PREFIX.length(), areaToken.length());
			if(token != null){
				return queryTokenProvider.tokenToQuery(token);
			}
		}
		return null;
	}
}
