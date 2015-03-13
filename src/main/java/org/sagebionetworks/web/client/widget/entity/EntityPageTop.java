package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.events.EntityDeletedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableRowHeader;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.ProjectAreaState;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter  {

	private EntityPageTopView view;
	private AuthenticationController authenticationController;
	private EntitySchemaCache schemaCache;
	private EntityTypeProvider entityTypeProvider;
	private IconsImageBundle iconsImageBundle;
	private EntityUpdatedHandler entityUpdateHandler;
	private GlobalApplicationState globalApplicationState;
	private EntityBundle bundle;
	private String entityTypeDisplay;
	private EventBus bus;
	private Long versionNumber;
	private Synapse.EntityArea area;
	private String areaToken;
	private EntityHeader projectHeader;
	private AreaChangeHandler areaChangedHandler;
	private ProjectAreaState projectAreaState;
	private QueryTokenProvider queryTokenProvider;
	
	public static final String TABLE_QUERY_PREFIX = "query/";
	public static final String TABLE_ROW_PREFIX = "row/";
	public static final String TABLE_ROW_VERSION_DELIMITER = "/rowversion/";
	
	@Inject
	public EntityPageTop(EntityPageTopView view, 
			AuthenticationController authenticationController,
			EntitySchemaCache schemaCache,
			EntityTypeProvider entityTypeProvider,
			IconsImageBundle iconsImageBundle,
			GlobalApplicationState globalApplicationState,
			EventBus bus,
			QueryTokenProvider queryTokenProvider) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.schemaCache = schemaCache;
		this.entityTypeProvider = entityTypeProvider;
		this.iconsImageBundle = iconsImageBundle;
		this.bus = bus;
		this.globalApplicationState = globalApplicationState;	
		this.queryTokenProvider = queryTokenProvider;
		this.projectAreaState = new ProjectAreaState();
		view.setPresenter(this);
	}

    /**
     * Update the bundle attached to this EntityPageTop. Consider calling refresh()
     * to notify an attached view.
     *
     * @param bundle
     */
    public void configure(EntityBundle bundle, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea area, String areaToken) {
    	// reset state for newly visited project
    	boolean isNewProject = projectHeader.getId().equals(projectAreaState.getProjectId());
    	if(!isNewProject) {
    		projectAreaState = new ProjectAreaState();
    		projectAreaState.setProjectId(projectHeader.getId());
    	}
    	
    	this.bundle = bundle;
    	this.versionNumber = versionNumber;
    	this.projectHeader = projectHeader;
    	this.area = area;
    	this.areaToken = areaToken;
    	
    	String entityId = bundle.getEntity().getId();
    	boolean isTable = bundle.getEntity() instanceof TableEntity;
    	boolean isProject = entityId.equals(projectAreaState.getProjectId());
    	
    	// For non-project file-tab entities, record them as the last file area place 
    	if(!isProject && !isTable && area != EntityArea.WIKI) {
    		EntityHeader lastFileAreaEntity = new EntityHeader();
    		lastFileAreaEntity.setId(entityId);
    		lastFileAreaEntity.setVersionNumber(versionNumber);
    		projectAreaState.setLastFileAreaEntity(lastFileAreaEntity);
    	}
    	
    	// record last wiki state
    	if(area == EntityArea.WIKI) {
    		projectAreaState.setLastWikiSubToken(areaToken);
    	}
    	
    	// record last table state
    	if(isTable) {
    		EntityHeader lastTableAreaEntity = new EntityHeader();
    		lastTableAreaEntity.setId(entityId);
    		projectAreaState.setLastTableAreaEntity(lastTableAreaEntity);
    	}
    	
    	// default area is the base wiki page if we are navigating to the project
    	if(area == null && isProject) {
    		projectAreaState.setLastWikiSubToken(null);
    	}
    	
    	// clear out file or table state if we go back to root
    	if(area == EntityArea.FILES && isProject) {
    		projectAreaState.setLastFileAreaEntity(null);
    	}

    	// clear out table state if we go back to root
    	if(area == EntityArea.TABLES && isProject) {
    		projectAreaState.setLastTableAreaEntity(null);
    	}
	}
    
	@SuppressWarnings("unchecked")
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
		sendDetailsToView(area, areaToken, projectHeader);
	}
		
	@Override
	public void fireEntityUpdatedEvent() {
		EntityUpdatedEvent event = new EntityUpdatedEvent();
		entityUpdateHandler.onPersistSuccess(event);
		bus.fireEvent(event);
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

	@Override
	public ImageResource getIconForType(String typeString) {
		EntityType type = entityTypeProvider.getEntityTypeForString(typeString);
		// try class name as some references are short names, some class names
		if(type == null)
			type = entityTypeProvider.getEntityTypeForClassName(typeString);
		if(type == null) {
			return DisplayUtils.getSynapseIconForEntity(null, IconSize.PX16, iconsImageBundle);
		}
		return DisplayUtils.getSynapseIconForEntityType(type, IconSize.PX16, iconsImageBundle);
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

	@Override
	public void refreshArea(Synapse.EntityArea area, String areaToken) {
		globalApplicationState.getPlaceChanger().goTo(new Synapse(bundle.getEntity().getId(), null, area, areaToken));
	}

	@Override
	public void gotoProjectArea(EntityArea area, EntityArea currentArea) {
		String entityId = projectHeader.getId();
		String areaToken = null;
		Long versionNumber = null;
		
		boolean overrideCache = false;
		// return to root for file and tables
		if((currentArea == EntityArea.FILES && area == EntityArea.FILES) 
				|| (bundle.getEntity() instanceof TableEntity && area == EntityArea.TABLES))
			overrideCache = true;
		
		if(!overrideCache) {
			if(area == EntityArea.WIKI) {
				areaToken = projectAreaState.getLastWikiSubToken();
			} else if(area == EntityArea.FILES && projectAreaState.getLastFileAreaEntity() != null) {
				entityId = projectAreaState.getLastFileAreaEntity().getId();
				versionNumber = projectAreaState.getLastFileAreaEntity().getVersionNumber();
			} else if(area == EntityArea.TABLES && projectAreaState.getLastTableAreaEntity() != null) {
				entityId = projectAreaState.getLastTableAreaEntity().getId();
			}
		}

		if(!entityId.equals(projectHeader.getId())) area = null; // don't specify area in place for non-project entities
		globalApplicationState.getPlaceChanger().goTo(new Synapse(entityId, versionNumber, area, areaToken));
	}

	@Override
	public boolean isPlaceChangeForArea(EntityArea targetTab) {
		boolean isProject = bundle.getEntity().getId().equals(projectAreaState.getProjectId());		
		if(targetTab == EntityArea.ADMIN && !isProject) {
			// admin area clicked outside of project requires goto
			return true;
		} else if(targetTab == EntityArea.FILES) {
			// files area clicked in non-project entity requires goto root of files
			// files area clicked with last-file-state requires goto
			if(!isProject || projectAreaState.getLastFileAreaEntity() != null) {				
				return true;				
			}
		} else if(targetTab == EntityArea.WIKI) {
			if(!isProject || (isProject && projectAreaState.getLastWikiSubToken() != null)) {
				// wiki area clicked in non-project entity requires goto
				// wiki area with defined subtoken requires goto (can not guarantee that subpage is loaded loaded)
				return true;							
			}
		} else if(targetTab == EntityArea.TABLES) {
			// tables area clicked in non-project entity requires goto root of tables
			// tables area clicked with last-table-state requires goto
			if(!isProject || projectAreaState.getLastTableAreaEntity() != null)
				return true;
		}
		return false;		
	}

	/**
	 * Handle entity deleted event
	 */
	@Override
	public void entityDeleted(EntityDeletedEvent event) {
		if (event != null && event.getDeletedId() != null && projectAreaState != null) {			
			if(projectAreaState.getLastTableAreaEntity() != null && event.getDeletedId().equals(projectAreaState.getLastTableAreaEntity().getId())) {
				// remove table state
				projectAreaState.setLastTableAreaEntity(null);
			} else if(projectAreaState.getLastFileAreaEntity() != null && event.getDeletedId().equals(projectAreaState.getLastFileAreaEntity().getId())) { 
				// remove file state
				projectAreaState.setLastFileAreaEntity(null);
			}
		}
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
				setArea(EntityArea.TABLES, TABLE_QUERY_PREFIX + token);
			}
		}
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

	
	/*
	 * Private Methods
	 */
	private void sendDetailsToView(Synapse.EntityArea area, String areaToken, EntityHeader projectHeader) {		
		ObjectSchema schema = schemaCache.getSchemaEntity(bundle.getEntity());
		entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(schema);
		view.setEntityBundle(bundle, getUserProfile(), entityTypeDisplay, versionNumber, area, areaToken, projectHeader);
	}
	
	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		return (sessionData==null ? null : sessionData.getProfile());		
	}

}
