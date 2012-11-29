package org.sagebionetworks.web.client.widget.entity.children;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.LayerTypeNames;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Preview;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.LayerPreview;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityChildBrowser implements EntityChildBrowserView.Presenter, SynapseWidgetPresenter {
	
	private EntityChildBrowserView view;
	private SynapseClientAsync synapseClient;
	private SearchServiceAsync searchService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private HandlerManager handlerManager = new HandlerManager(this);
	private GlobalApplicationState globalApplicationState;
	private Entity entity;	
	private EntityTypeProvider entityTypeProvider;

	private LayerPreview layerPreview; 
	PreviewData previewData;
	
	@Inject
	public EntityChildBrowser(EntityChildBrowserView view,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			SynapseClientAsync synapseClient, SearchServiceAsync searchService,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.searchService = searchService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
				
		previewData = new PreviewData();
		view.setPresenter(this);
	}	
	
	public Widget asWidget(Entity entity) {		
		view.setPresenter(this);
		this.entity = entity; 		
		
		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entity);
		view.createBrowser(entity, entityType);
		 
		// load preview if has previews
//		if(entity instanceof HasPreviews) {
//			loadPreview();
//		}
				
		return view.asWidget();
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.entity = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return null;
	}
		
	@SuppressWarnings("unchecked")
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<WhereCondition> getProjectContentsWhereContidions() {
		final List<WhereCondition> where = new ArrayList<WhereCondition>();
		where.add(new WhereCondition(DisplayUtils.ENTITY_PARENT_ID_KEY, WhereOperator.EQUALS, entity.getId()));
		return where;
	}

	@Override
	public List<EntityType> getContentsSkipTypes() {
		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(entity);
		
		List<EntityType> ignore = new ArrayList<EntityType>();
		// ignore self type children 
		ignore.add(entityType); 
		// ignore certain types		
		ignore.add(entityTypeProvider.getEntityTypeForClassName(Preview.class.getName()));
		ignore.add(entityTypeProvider.getEntityTypeForClassName(Folder.class.getName()));
		ignore.add(entityTypeProvider.getEntityTypeForClassName(Summary.class.getName()));
		
		return ignore;
	}

	@Override
	public LocationData getMediaLocationData() {
		LocationData location = new LocationData();
		if(entity instanceof Data && ((Data)entity).getType() == LayerTypeNames.M) {			
			List<LocationData> locations = ((Data)entity).getLocations();
			if(locations != null && locations.size() > 0) {
				location = locations.get(0); // send the first location
			}			 				
		}
		return location;
	}
	
	@Override
	public String getReferenceUri(EntityHeader header) {
		String token = header.getId();
//		if(header.getTargetVersionNumber() != null)
//			token += "." + header.getTargetVersionNumber();		
		return DisplayUtils.getSynapseHistoryToken(token);

	}

	@Override
	public void getChildrenHeaders(final AsyncCallback<List<EntityHeader>> callback) {
		List<EntityHeader> headers = new ArrayList<EntityHeader>();		
		final int MAX_LIMIT = 200;

		searchService.searchEntities("entity", Arrays
				.asList(new WhereCondition[] { new WhereCondition("parentId",
						WhereOperator.EQUALS, entity.getId()) }), 1, MAX_LIMIT, null,
				false, new AsyncCallback<List<String>>() {
				@Override
				public void onSuccess(List<String> result) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for(String entityHeaderJson : result) {
						try {
							headers.add(nodeModelCreator.createJSONEntity(entityHeaderJson, EntityHeader.class));
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}
					callback.onSuccess(headers);
				}
				@Override
				public void onFailure(Throwable caught) {
					DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());				
					callback.onFailure(caught);
				}
			});					

	}

	@Override
	public void goToEntity(String selectedId) {
		globalApplicationState.getPlaceChanger().goTo(new Synapse(selectedId));
	}

	@Override
	public void reloadEntity() {
		goToEntity(entity.getId());
	}
	
	/*
	 * Private Methods
	 */
}
