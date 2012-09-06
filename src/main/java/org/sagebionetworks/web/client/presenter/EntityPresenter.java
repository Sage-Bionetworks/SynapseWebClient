package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.CHILD_COUNT;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_PATH;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_REFERENCEDBY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.UNMET_ACCESS_REQUIREMENTS;

import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
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
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private NodeServiceAsync nodeService;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private String entityId;
	private Long versionNumber;
	private boolean readOnly = false;
	
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
		this.entityId = place.getEntityId();
		this.versionNumber = place.getVersionNumber();

		// asking for a specific version puts you into read only mode
		readOnly = versionNumber == null ? false : true;
		
		refresh();
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	@Override
	public void refresh() {
		
		// Hide the view panel contents until async callback completes
		view.showLoading();
		
		// We want the entity, permissions and path.
		// TODO : add REFERENCED_BY
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | 
		ENTITY_PATH | ENTITY_REFERENCEDBY | CHILD_COUNT |
			ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS;
		AsyncCallback<EntityBundleTransport> callback = new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport transport) {
				
				EntityBundle bundle = null;
				try {
					bundle = nodeModelCreator.createEntityBundle(transport);
					
					// Redirect if Entity is a Link
					if(bundle.getEntity() instanceof Link) {
						Reference ref = ((Link)bundle.getEntity()).getLinksTo();
						entityId = null;
						if(ref != null){
							entityId = ref.getTargetId();
							versionNumber = ref.getTargetVersionNumber();
						}
						refresh();
						return;
					} 					
					
					view.setEntityBundle(bundle, readOnly);					
				} catch (RestServiceException ex) {					
					onFailure(null);					
					globalApplicationState.getPlaceChanger().goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN));
				}				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
					view.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_LOAD);
				}
				globalApplicationState.getPlaceChanger().goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}			
		};
		
		if(versionNumber == null) {
			synapseClient.getEntityBundle(entityId, mask, callback);
		} else {
			synapseClient.getEntityBundleForVersion(entityId, versionNumber, mask, callback);
		}

	}
	
}
