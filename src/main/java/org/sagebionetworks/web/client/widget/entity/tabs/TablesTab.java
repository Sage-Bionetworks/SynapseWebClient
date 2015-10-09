package org.sagebionetworks.web.client.widget.entity.tabs;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.HAS_CHILDREN;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.TABLE_DATA;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	EntityUpdatedHandler handler;
	QueryTokenProvider queryTokenProvider;
	Entity entity;
	EntityBundle projectBundle;
	Throwable projectBundleLoadError;
	String projectEntityId;
	String areaToken;
	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	PortalGinInjector ginInjector;
	ModifiedCreatedByWidget modifiedCreatedBy;
	
	CallbackP<Boolean> showProjectInfoCallack;
	
	@Inject
	public TablesTab(
			TablesTabView view,
			Tab t,
			TableListWidget tableListWidget,
			BasicTitleBar tableTitleBar,
			Breadcrumb breadcrumb,
			EntityMetadata metadata,
			QueryTokenProvider queryTokenProvider,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient,
			PortalGinInjector ginInjector,
			ModifiedCreatedByWidget modifiedCreatedBy
			) {
		this.view = view;
		this.tab = t;
		this.tableListWidget = tableListWidget;
		this.tableTitleBar = tableTitleBar;
		this.breadcrumb = breadcrumb;
		this.metadata = metadata;
		this.queryTokenProvider = queryTokenProvider;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		this.ginInjector = ginInjector;
		this.modifiedCreatedBy = modifiedCreatedBy;
		
		view.setBreadcrumb(breadcrumb.asWidget());
		view.setTableList(tableListWidget.asWidget());
		view.setTitlebar(tableTitleBar.asWidget());
		view.setEntityMetadata(metadata.asWidget());
		view.setSynapseAlert(synAlert.asWidget());
		view.setModifiedCreatedBy(modifiedCreatedBy);
		tab.configure("Tables", view.asWidget());
		
		tableListWidget.setTableClickedCallback(new CallbackP<String>() {
			@Override
			public void invoke(String entityId) {
				areaToken = null;
				getTargetBundleAndDisplay(entityId);
			}
		});
		initBreadcrumbLinkClickedHandler();
	}
	
	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {
		if (handler != null) {
			handler.onPersistSuccess(event);
		}
	}
	
	public void initBreadcrumbLinkClickedHandler() {
		CallbackP<Place> breadcrumbClicked = new CallbackP<Place>() {
			public void invoke(Place place) {
				//if this is the project id, then just reconfigure from the project bundle
				Synapse synapse = (Synapse)place;
				String entityId = synapse.getEntityId();
				if (entityId.equals(projectEntityId)) {
				    showProjectLevelUI();
				    tab.showTab();
				} else {
				    getTargetBundleAndDisplay(entityId);
				}
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
	
	public void configure(Entity entity, EntityUpdatedHandler handler, String areaToken) {
		this.entity = entity;
		this.areaToken = areaToken;
		this.handler = handler;
		metadata.setEntityUpdatedHandler(handler);
		synAlert.clear();
		boolean isTable = entity instanceof TableEntity;
		
		if (!isTable) {
			//configure based on project
			showProjectLevelUI();
		} else {
			getTargetBundleAndDisplay(entity.getId());
		}
	}
	
	
	public void showProjectLevelUI() {
		tab.setPlace(new Synapse(projectEntityId, null, EntityArea.TABLES, null));
		if (projectBundle != null) {
			setTargetBundle(projectBundle);	
		} else {
			showError(projectBundleLoadError);
		}
	}
	
	public void resetView() {
		synAlert.clear();
		view.setEntityMetadataVisible(false);
		view.setBreadcrumbVisible(false);
		view.setTableListVisible(false);
		view.setTitlebarVisible(false);
		showProjectInfoCallack.invoke(false);
		view.clearActionMenuContainer();
		view.clearTableEntityWidget();
		modifiedCreatedBy.setVisible(false);
	}
	
	public void showError(Throwable error) {
		resetView();
		synAlert.handleException(error);
	}
	
	public void setTargetBundle(EntityBundle bundle) {
		this.entity = bundle.getEntity();
		boolean isTable = entity instanceof TableEntity;
		boolean isProject = entity instanceof Project;
		view.setEntityMetadataVisible(isTable);
		view.setBreadcrumbVisible(isTable);
		view.setTableListVisible(isProject);
		view.setTitlebarVisible(isTable);
		showProjectInfoCallack.invoke(isProject);
		view.clearActionMenuContainer();
		view.clearTableEntityWidget();
		modifiedCreatedBy.setVisible(false);
		if (isTable) {
			breadcrumb.configure(bundle.getPath(), EntityArea.TABLES);
			metadata.setEntityBundle(bundle, null);
			tableTitleBar.configure(bundle);
			modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
			ActionMenuWidget actionMenu = initActionMenu(bundle);
			
			TableEntityWidget v2TableWidget = ginInjector.createNewTableEntityWidget();
			view.setTableEntityWidget(v2TableWidget.asWidget());
			v2TableWidget.configure(bundle, bundle.getPermissions().getCanCertifiedUserEdit(), this, actionMenu);
		} else if (isProject) {
			areaToken = null;
			tableListWidget.configure(bundle);
		}
	}
	
	public ActionMenuWidget initActionMenu(EntityBundle bundle) {
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
				TablesTab.this.metadata.setAnnotationsVisible(annotationsShown);
			}
		});
		controller.configure(actionMenu, bundle, bundle.getRootWikiId(), handler);
		return actionMenu;
	}
	
	public void getTargetBundleAndDisplay(String entityId) {
		synAlert.clear();
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS  | DOI | TABLE_DATA;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				setTargetBundle(bundle);
				tab.showTab();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showError(caught);
				tab.showTab();
			}			
		};
		tab.setPlace(new Synapse(entityId, null, null, null));
		synapseClient.getEntityBundle(entityId, mask, callback);
	}
	
	public Tab asTab(){
		return tab;
	}
	
	public void onQueryChange(Query newQuery) {
		if(newQuery != null){
			String token = queryTokenProvider.queryToToken(newQuery);
			if(token != null){
				areaToken = TABLE_QUERY_PREFIX + token;
				tab.setPlace(new Synapse(entity.getId(), null, EntityArea.TABLES, areaToken));
				tab.showTab();
			}
		}
	}
	
	public void setShowProjectInfoCallback(CallbackP<Boolean> callback) {
		showProjectInfoCallack = callback;
		tab.addTabClickedCallback(new CallbackP<Tab>() {
			@Override
			public void invoke(Tab param) {
				boolean isProject = entity instanceof Project;
				showProjectInfoCallack.invoke(isProject);
			}
		});

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
	
	public Entity getCurrentEntity() {
		return entity;
	}
}
