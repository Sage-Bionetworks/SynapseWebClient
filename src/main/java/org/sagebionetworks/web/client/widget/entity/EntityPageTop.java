package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter  {
		
	private EntityPageTopView view;
	private NodeServiceAsync nodeService;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private EntitySchemaCache schemaCache;	
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityTypeProvider entityTypeProvider;
	private IconsImageBundle iconsImageBundle;
	
	private EntityBundle bundle;
	private boolean readOnly;
	private String entityTypeDisplay; 
	private EventBus bus;
	private String rStudioUrl;
	
	@Inject
	public EntityPageTop(EntityPageTopView view, NodeServiceAsync service,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			EntitySchemaCache schemaCache,
			JSONObjectAdapter jsonObjectAdapter,
			EntityTypeProvider entityTypeProvider,
			IconsImageBundle iconsImageBundle,
			EventBus bus) {
		this.view = view;
		this.nodeService = service;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.schemaCache = schemaCache;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityTypeProvider = entityTypeProvider;
		this.iconsImageBundle = iconsImageBundle;
		this.bus = bus;
		view.setPresenter(this);
	}	

    /**
     * Update the bundle attached to this EntityPageTop. Consider calling refresh()
     * to notify an attached view.
     * 
     * @param bundle
     */
    public void setBundle(EntityBundle bundle, boolean readOnly) {
    	this.bundle = bundle;
    	this.readOnly = readOnly;
//    	if(bundle != null){
//    		// get current user profile
//    		synapseClient.getUserProfile(new AsyncCallback<String>() {
//    			@Override
//    			public void onSuccess(String userProfileJson) {
//    				try {
//    					UserProfile profile = nodeModelCreator.createEntity(userProfileJson, UserProfile.class);
//   						rStudioUrl = profile.getRStudioUrl();    						    					
//    					view.setRStudioUrlReady();
//    				} catch (RestServiceException e) {
//						onFailure(e);
//					}    				
//    			}
//    			@Override
//    			public void onFailure(Throwable caught) {
//    				// error retrieving user profile
//    				DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());    					    				
//    			}
//    		});
//
//    	}
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
			return asWidget(bundle);
		} 
		return null;
	}	
	
	public Widget asWidget(EntityBundle bundle) {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	@Override
	public void refresh() {
		sendDetailsToView(bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanEdit());
	}	

	@Override
	public void fireEntityUpdatedEvent() {
		bus.fireEvent(new EntityUpdatedEvent());
	}
	
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {		
		bus.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	@Override
	public boolean isLocationable() {
		if(bundle.getEntity() instanceof Locationable) {
			return true;
		}
		return false;
	}

//	@Override
//	public String getRstudioUrlBase() {
//		return rStudioUrl;
//	}
//	
//	@Override
//	public String getRstudioUrl() {		
//		String url = getRstudioUrlBase(); 
//		UserData userData = authenticationController.getLoggedInUser();
//		if(url != null && userData != null) {
//			url += "#synapse:" + userData.getToken() + ";get:" + bundle.getEntity().getId();			
//		} 
//		return url;
//	}
//	
//	@Override
//	public void saveRStudioUrlBase(final String value) {
//		rStudioUrl = value;
//		
//		// get current user profile		
//		synapseClient.getUserProfile(new AsyncCallback<String>() {
//			@Override
//			public void onSuccess(String userProfileJson) {
//				try {
//					UserProfile profile = nodeModelCreator.createEntity(userProfileJson, UserProfile.class);
//					profile.setRStudioUrl(value);		
//					JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
//					profile.writeToJSONObject(adapter);
//					// save update user profile
//					synapseClient.updateUserProfile(adapter.toJSONString(), new AsyncCallback<Void>() {
//						@Override
//						public void onSuccess(Void result) {
//							view.showInfo(DisplayConstants.LABEL_UPDATED, DisplayConstants.TEXT_USER_PROFILE_UPDATED);
//						}
//						@Override
//						public void onFailure(Throwable caught) {
//							view.showErrorMessage(DisplayConstants.ERROR_USER_PROFILE_SAVE);
//						}
//					});					
//				} catch (JSONObjectAdapterException e) {
//					onFailure(e);
//				} catch (RestServiceException e) {
//					onFailure(e);
//				}
//			}
//			@Override
//			public void onFailure(Throwable caught) {
//				view.showErrorMessage(DisplayConstants.ERROR_USER_PROFILE_SAVE);
//			}
//		});
//		
//	}
	
	@Override 
	public boolean isLoggedIn() {
		return authenticationController.getLoggedInUser() != null;
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

	@Override
	public void loadShortcuts(int offset, int limit, final AsyncCallback<PaginatedResults<EntityHeader>> callback) {
		PaginatedResults<EntityHeader> references = null;
		if(offset == 0) {
			 callback.onSuccess(bundle.getReferencedBy());			 
		} else {
			synapseClient.getEntityReferencedBy(bundle.getEntity().getId(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					PaginatedResults<EntityHeader> paginatedResults = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
					callback.onSuccess(paginatedResults);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}		
	}	
	
	/*
	 * Private Methods
	 */
	private void sendDetailsToView(boolean isAdmin, boolean canEdit) {
		ObjectSchema schema = schemaCache.getSchemaEntity(bundle.getEntity());
		entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(schema);
		view.setEntityBundle(bundle, entityTypeDisplay, isAdmin, canEdit, readOnly);
	}

}
