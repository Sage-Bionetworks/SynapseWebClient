package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_PATH;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_REFERENCEDBY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;

import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class EntityPresenter extends AbstractActivity implements EntityView.Presenter {
		
	private Synapse place;
	private EntityView view;
	private PlaceChanger placeChanger;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private NodeServiceAsync nodeService;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private String entityId;
	
	@Inject
	public EntityPresenter(EntityView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			NodeServiceAsync nodeService, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeService = nodeService;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.placeChanger = globalApplicationState.getPlaceChanger();
	
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(Synapse place) {
		this.place = place;
		this.view.setPresenter(this);		
		
		// token maps directly to entity id
		this.entityId = place.toToken();
		
		refresh();
	}

	@Override
	public PlaceChanger getPlaceChanger() {
		return placeChanger;
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	@Override
	public void refresh() {
		// We want the entity, permissions and path.
		// TODO : add REFERENCED_BY
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | ENTITY_REFERENCEDBY;
		synapseClient.getEntityBundle(entityId, mask, new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport transport) {
				
				EntityBundle bundle = null;
				try {
					bundle = nodeModelCreator.createEntityBundle(transport);
					
					// Redirect if Entity is a Link
					if(bundle.getEntity() instanceof Link) {
						entityId = ((Link)bundle.getEntity()).getLinksTo();
						refresh();
						return;
					} 					
					
					view.setEntityBundle(bundle);					
				} catch (RestServiceException ex) {					
					onFailure(null);					
					placeChanger.goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN));
				}				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, placeChanger, authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_LOAD);
				}
				placeChanger.goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}			
		});

	}
	
}
