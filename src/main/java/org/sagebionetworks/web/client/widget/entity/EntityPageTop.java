package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.events.EntityDeletedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.tabs.AdminTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tabs;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableRowHeader;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter, IsWidget  {
	private EntityPageTopView view;
	private AuthenticationController authenticationController;
	private EntityUpdatedHandler entityUpdateHandler;
	private EntityBundle bundle;
	private String entityTypeDisplay;
	private Long versionNumber;
	private Synapse.EntityArea area;
	private String areaToken;
	private EntityHeader projectHeader;
	private AreaChangeHandler areaChangedHandler;
	private QueryTokenProvider queryTokenProvider;
	
	public static final String TABLE_QUERY_PREFIX = "query/";
	public static final String TABLE_ROW_PREFIX = "row/";
	public static final String TABLE_ROW_VERSION_DELIMITER = "/rowversion/";
	private Tabs tabs;
	private WikiTab wikiTab;
	private FilesTab filesTab;
	private TablesTab tablesTab;
	private AdminTab adminTab;
	private EntityMetadata projectMetadata;
	
	@Inject
	public EntityPageTop(EntityPageTopView view, 
			AuthenticationController authenticationController,
			QueryTokenProvider queryTokenProvider,
			Tabs tabs,
			EntityMetadata projectMetadata,
			WikiTab wikiTab,
			FilesTab filesTab,
			TablesTab tablesTab,
			AdminTab adminTab) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.queryTokenProvider = queryTokenProvider;
		this.tabs = tabs;
		this.wikiTab = wikiTab;
		this.filesTab = filesTab;
		this.tablesTab = tablesTab;
		this.adminTab = adminTab;
		this.projectMetadata = projectMetadata;
		
		initTabs();
		view.setTabs(tabs.asWidget());
		view.setProjectMetadata(projectMetadata.asWidget());
		view.setPresenter(this);
		
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				fireEntityUpdatedEvent();
			}
		};
		projectMetadata.setEntityUpdatedHandler(handler);
	}
	
	private void initTabs() {
		tabs.addTab(wikiTab.asTab());
		tabs.addTab(filesTab.asTab());
		tabs.addTab(tablesTab.asTab());
		tabs.addTab(adminTab.asTab());
	}

    /**
     * Update the bundle attached to this EntityPageTop. 
     *
     * @param bundle
     */
    public void configure(EntityBundle bundle, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea area, String areaToken) {
    	this.bundle = bundle;
    	this.versionNumber = versionNumber;
    	this.projectHeader = projectHeader;
    	this.area = area;
    	this.areaToken = areaToken;
    	
    	entityTypeDisplay = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(bundle.getEntity().getClass()));
    	
    	//configure each tab
    	configureWikiTab();
    	configureFilesTab();
    	configureTablesTab();
    	configureAdminTab();

    	//set area, if undefined
		if (area == null) {
			if (bundle.getEntity() instanceof Project) {
				area = EntityArea.WIKI;
			} else {
				area = EntityArea.FILES;
			}
		}
		
    	//go to the tab corresponding to the area stated
		if (area == EntityArea.WIKI) {
			tabs.showTab(wikiTab.asTab());
		} else if (area == EntityArea.FILES) {
			tabs.showTab(filesTab.asTab());
		} else if (area == EntityArea.TABLES) {
			tabs.showTab(tablesTab.asTab());
		} else if (area == EntityArea.ADMIN) {
			tabs.showTab(adminTab.asTab());
		}
		
		projectMetadata.setEntityBundle(bundle, versionNumber);
		view.setEntityBundle(bundle, getUserProfile(), entityTypeDisplay, versionNumber, area, areaToken, projectHeader, getWikiPageId(area, areaToken, bundle.getRootWikiId()));
	}
    
    public void clearState() {
		view.clear();
		// remove handlers
		this.bundle = null;
	}

	@Override
	public Widget asWidget() {
		if(bundle != null) {
			view.setPresenter(this);
			return view.asWidget();
		}
		return null;
	}

	@Override
	public void refresh() {
		configure(bundle, versionNumber, projectHeader, area, areaToken);
	}
	public void configureTablesTab() {
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				fireEntityUpdatedEvent();
			}
		};
		
		QueryChangeHandler qch = new QueryChangeHandler() {			
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
				fireEntityUpdatedEvent();
			}
		};
		tablesTab.configure(bundle, handler, qch);
		
	}
	public void configureFilesTab() {
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				fireEntityUpdatedEvent();
			}
		};
		
		filesTab.configure(bundle, handler);
	}
	
	public void configureWikiTab() {
		String wikiPageId = getWikiPageId(area, areaToken, bundle.getRootWikiId());
		final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		final WikiPageWidget.Callback callback = new WikiPageWidget.Callback() {
			@Override
			public void pageUpdated() {
				fireEntityUpdatedEvent();
			}
			@Override
			public void noWikiFound() {
				//if wiki area not specified and no wiki found, show Files tab instead for projects 
				// Note: The fix for SWC-1785 was to set this check to area == null.  Prior to this change it was area != WIKI.
				if(area == null) {							
					tabs.showTab(filesTab.asTab());
				}
			}
		};
		wikiTab.configure(projectHeader.getId(), wikiPageId, 
				canEdit, callback, true);
		
		
		CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
			@Override
			public void invoke(String wikiPageId) {
				view.configureProjectActionMenu(bundle, wikiPageId);
				wikiTab.configure(projectHeader.getId(), wikiPageId, 
						canEdit, callback, true);
			}
		};
		wikiTab.setWikiReloadHandler(wikiReloadHandler);
	}
	
	public void configureAdminTab() {
		String projectId = projectHeader.getId();
		adminTab.configure(projectId);
	}
		
	@Override
	public void fireEntityUpdatedEvent() {
		EntityUpdatedEvent event = new EntityUpdatedEvent();
		entityUpdateHandler.onPersistSuccess(event);
	}

	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		entityUpdateHandler = handler;
	}
	
	@Override 
	public boolean isLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	@Override
	public String createEntityLink(String id, String version, String display) {
		return DisplayUtils.createEntityLink(id, version, display);
	}

	public void setAreaChangeHandler(AreaChangeHandler handler) {
		this.areaChangedHandler = handler;
	}
	
	@Override
	public void setArea(EntityArea area, String areaToken) {
		this.area = area;
		this.areaToken = areaToken;
		if(areaChangedHandler != null) areaChangedHandler.areaChanged(area, areaToken);
	}

	public void replaceArea(EntityArea area, String areaToken){
		this.area = area;
		this.areaToken = areaToken;
		if(areaChangedHandler != null) areaChangedHandler.replaceArea(area, areaToken);
	}

	@Override
	public void setTableRow(TableRowHeader rowHeader) {
		if(rowHeader != null && rowHeader.getRowId() != null) {
			String rowStr = rowHeader.getRowId();
			if(rowHeader.getVersion() != null) rowStr += TABLE_ROW_VERSION_DELIMITER + rowHeader.getVersion();
			setArea(EntityArea.TABLES, TABLE_ROW_PREFIX + rowStr);			
		}
	}

	@Override
	public TableRowHeader getTableRowHeader() {		
		if(areaToken != null && areaToken.startsWith(TABLE_ROW_PREFIX)) {
			TableRowHeader rowHeader = new TableRowHeader();
			String rowHeaderStr = areaToken.substring(TABLE_ROW_PREFIX.length(), areaToken.length());
			if(rowHeaderStr.contains(TABLE_ROW_VERSION_DELIMITER)) {					
				String[] versionParts = rowHeaderStr.split(TABLE_ROW_VERSION_DELIMITER);
				if(versionParts.length == 2) {
					rowHeader.setRowId(versionParts[0]);
					rowHeader.setVersion(versionParts[1]);
				} else {
					return null; // malformed
				}
			} else {
				rowHeader.setRowId(rowHeaderStr);					
			}
			return rowHeader;
		}
		return null;
	}

	@Override
	public void setTableQuery(Query newQuery) {
		if(newQuery != null){
			String token = queryTokenProvider.queryToToken(newQuery);
			if(token != null){
				/*
				 * The first time we set a query in the URL we want to replace 
				 * the current history.  All subsequent changes to the query
				 * should be added to the browser's history.
				 */
				if(areaHasTableQuery()){
					// Adds the new query to the browser's history.
					setArea(EntityArea.TABLES, TABLE_QUERY_PREFIX + token);
				}else{
					// Replace the current entry in the browser's history with the new query.
					replaceArea(EntityArea.TABLES, TABLE_QUERY_PREFIX + token);
				}
			}
		}
	}
	
	private boolean areaHasTableQuery(){
		Query currentQuery = getTableQuery();
		return currentQuery != null;
	}
	
	@Override
	public Query getTableQuery() {
		if(areaToken != null && areaToken.startsWith(TABLE_QUERY_PREFIX)) {
			String token = areaToken.substring(TABLE_QUERY_PREFIX.length(), areaToken.length());
			if(token != null){
				return queryTokenProvider.tokenToQuery(token);
			}
		}
		return null;
	}

	@Override
	public void handleWikiReload(String wikiPageId) {
		if (bundle.getEntity() instanceof Project) {
			setArea(EntityArea.WIKI, wikiPageId);
			view.configureProjectActionMenu(bundle, wikiPageId);
		} else {
			view.configureFileActionMenu(bundle, wikiPageId);
		}
	}
	
	public String getWikiPageId(Synapse.EntityArea area, String areaToken, String rootWikiId) {
		String wikiPageId = rootWikiId;
		if (Synapse.EntityArea.WIKI == area && DisplayUtils.isDefined(areaToken))
			wikiPageId = areaToken;
		return wikiPageId;
	}
	
	
	
	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		return (sessionData==null ? null : sessionData.getProfile());		
	}
}
