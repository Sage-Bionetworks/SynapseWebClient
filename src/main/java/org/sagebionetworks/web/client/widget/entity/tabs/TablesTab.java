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

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TablesTab implements TablesTabView.Presenter{
	
	public static final String TABLE_QUERY_PREFIX = "query/";
	public static final String TABLE_ROW_PREFIX = "row/";
	public static final String TABLE_ROW_VERSION_DELIMITER = "/rowversion/";
	
	Tab tab;
	TablesTabView view;
	TableListWidget tableListWidget;
	BasicTitleBar tableTitleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	boolean annotationsShown;
	QueryChangeHandler qch;
	EntityUpdatedHandler handler;
	QueryTokenProvider queryTokenProvider;
	Entity entity;
	EntityBundle projectBundle;
	String areaToken;
	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	PortalGinInjector ginInjector;
	
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
			PortalGinInjector ginInjector
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
		
		view.setBreadcrumb(breadcrumb.asWidget());
		view.setTableList(tableListWidget.asWidget());
		view.setTitlebar(tableTitleBar.asWidget());
		view.setEntityMetadata(metadata.asWidget());
		view.setSynapseAlert(synAlert.asWidget());
		tab.configure("Tables", view.asWidget());
		
		qch = new QueryChangeHandler() {			
			@Override
			public void onQueryChange(Query newQuery) {
				setTableQuery(newQuery);				
			}

			@Override
			public Query getQueryString() {
				return getTableQuery();
			}

			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				if (handler != null) {
					handler.onPersistSuccess(event);
				}
			}
		};
		
		tableListWidget.setTableClickedCallback(new CallbackP<String>() {
			@Override
			public void invoke(String entityId) {
				areaToken = null;
				getTargetBundle(entityId);
			}
		});
		initBreadcrumbLinkClickedHandler();
	}
	
	public void initBreadcrumbLinkClickedHandler() {
		CallbackP<Place> breadcrumbClicked = new CallbackP<Place>() {
			public void invoke(Place place) {
				//if this is the project id, then just reconfigure from the project bundle
				Synapse synapse = (Synapse)place;
				String entityId = synapse.getEntityId();
				if (entityId.equals(projectBundle.getEntity().getId())) {
					setTargetBundle(projectBundle);
					tab.showTab();
				} else {
					getTargetBundle(entityId);
				}
			};
		};
		breadcrumb.setLinkClickedHandler(breadcrumbClicked);
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void configure(Entity entity, EntityBundle projectBundle, EntityUpdatedHandler handler, String areaToken) {
		this.areaToken = areaToken;
		this.projectBundle = projectBundle;
		this.handler = handler;
		metadata.setEntityUpdatedHandler(handler);
		
		boolean isTable = entity instanceof TableEntity;
		if (!isTable) {
			//configure based on project
			setTargetBundle(projectBundle);
		} else {
			getTargetBundle(entity.getId());
		}
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
		view.clearModifiedAndCreatedWidget();
		if (isTable) {
			breadcrumb.configure(bundle.getPath(), EntityArea.TABLES);
			metadata.setEntityBundle(bundle, null);
			tableTitleBar.configure(bundle);
			view.configureModifiedAndCreatedWidget(entity);
			ActionMenuWidget actionMenu = initActionMenu(bundle);
			
			TableEntityWidget v2TableWidget = ginInjector.createNewTableEntityWidget();
			view.setTableEntityWidget(v2TableWidget.asWidget());
			v2TableWidget.configure(bundle, bundle.getPermissions().getCanCertifiedUserEdit(), qch, actionMenu);
		} else if (isProject) {
			areaToken = null;
			tableListWidget.configure(bundle);
			tab.setPlace(new Synapse(entity.getId(), null, EntityArea.TABLES, areaToken));
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
	
	public void getTargetBundle(String entityId) {
		synAlert.clear();
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS  | DOI | TABLE_DATA;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				setTargetBundle(bundle);
				tab.setPlace(new Synapse(entity.getId(), null, null, areaToken));
				tab.showTab();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}			
		};
		
		synapseClient.getEntityBundle(entityId, mask, callback);
	}
	
	public Tab asTab(){
		return tab;
	}
	
	public void setTableQuery(Query newQuery) {
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
	
	public Query getTableQuery() {
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
