package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowser implements EntityTreeBrowserView.Presenter, SynapseWidgetPresenter {
	
	private EntityTreeBrowserView view;
	private SearchServiceAsync searchService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private IconsImageBundle iconsImageBundle;
	private AutoGenFactory entityFactory;
	private EntityEditor entityEditor;
	
	private String currentSelection;
	
	private final int MAX_FOLDER_LIMIT = 500;
	
	@Inject
	public EntityTreeBrowser(EntityTreeBrowserView view,
			SearchServiceAsync searchService,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			IconsImageBundle iconsImageBundle,
			AutoGenFactory entityFactory, EntityEditor entityEditor) {
		this.view = view;		
		this.searchService = searchService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.iconsImageBundle = iconsImageBundle;
		this.entityFactory = entityFactory;
		this.entityEditor = entityEditor;
		
		view.setPresenter(this);
	}	
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
	}

	/**
	 * Configure tree view with given entityId's children as start set
	 * @param entityId
	 */
	public void configure(String entityId, final boolean sort) {
		getFolderChildren(entityId, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setRootEntities(result, sort);
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());
			}
		});
	}
	
	/**
	 * Configure tree view to be filled initially with the given headers.
	 * @param rootEntities
	 */
	public void configure(List<EntityHeader> rootEntities, boolean sort) {
		view.setRootEntities(rootEntities, sort);
	}
	
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}
	
	@Override
	public void getFolderChildren(String entityId, final AsyncCallback<List<EntityHeader>> asyncCallback) {
		List<EntityHeader> headers = new ArrayList<EntityHeader>();		
		
		searchService.searchEntities("entity", Arrays
				.asList(new WhereCondition[] { 
						new WhereCondition("parentId", WhereOperator.EQUALS, entityId)
						}), 1, MAX_FOLDER_LIMIT, null,
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
					asyncCallback.onSuccess(headers);
				}
				@Override
				public void onFailure(Throwable caught) {
					DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());				
					asyncCallback.onFailure(caught);
				}
			});					
	}

	@Override
	public void setSelection(String id) {
		currentSelection = id;
		fireEntitySelectedEvent();
	}	
	
	public String getSelected() {
		return currentSelection;
	}
		
	@SuppressWarnings("unchecked")
	public void addEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.addHandler(EntitySelectedEvent.getType(), handler);		
	}

	@SuppressWarnings("unchecked")
	public void removeEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.removeHandler(EntitySelectedEvent.getType(), handler);
	}
	
	@SuppressWarnings("unchecked")
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);		
	}

	@SuppressWarnings("unchecked")
	public void removeEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.removeHandler(EntityUpdatedEvent.getType(), handler);
	}
	
	@Override
	public int getMaxLimit() {
		return MAX_FOLDER_LIMIT;
	}

	@Override
	public void deleteEntity(final EntityTreeModel entityModel) {
		final String entityId = entityModel.getId();
		synapseClient.deleteEntityById(entityId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Deleted", "Synapse id " + entityId + " was successfully deleted.");
				view.removeEntity(entityModel);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_DELETE_FAILURE);
				}
			}
		});
	}

	/**
	 * Show links if true
	 * @param makeLinks Make the labels entity links if true 
	 */
	public void setMakeLinks(boolean makeLinks) {
		view.setMakeLinks(makeLinks);
	}

	/**
	 * Show the right click menu
	 * @param showContextMenu
	 */
	public void setShowContextMenu(boolean showContextMenu) {
		view.setShowContextMenu(showContextMenu);
	}

	
	/*
	 * Private Methods
	 */
	private void fireEntitySelectedEvent() {
		handlerManager.fireEvent(new EntitySelectedEvent());
	}

	@Override
	public ImageResource getIconForType(String type) {
		if(type == null) return null;
		EntityType entityType;
		if(type.startsWith("org.")) entityType = entityTypeProvider.getEntityTypeForClassName(type); 			
		else entityType = entityTypeProvider.getEntityTypeForString(type);
		if (entityType == null) return null;
		return DisplayUtils.getSynapseIconForEntityClassName(entityType.getClassName(), DisplayUtils.IconSize.PX16, iconsImageBundle);
	}

	@Override
	public void onEdit(String entityId) {
		int mask = ENTITY | PERMISSIONS;
		synapseClient.getEntityBundle(entityId, mask, new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport transport) {				
				EntityBundle bundle = null;
				try {
					bundle = nodeModelCreator.createEntityBundle(transport);										
					entityEditor.editEntity(bundle, false);					
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_LOAD);
				}
				globalApplicationState.getPlaceChanger().goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}			
		});		
	}

}
